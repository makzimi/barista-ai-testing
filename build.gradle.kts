import io.gitlab.arturbosch.detekt.Detekt

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.roborazzi) apply false
    alias(libs.plugins.detekt)
    alias(libs.plugins.module.graph.assert)
}

// Two projects are called `impl` and two `api`: give each a unique group so Gradle never confuses their coordinates.
subprojects {
    group = "ru.otus.barista" + path.substringBeforeLast(':').replace(':', '.')
}

dependencies {
    detektPlugins(libs.detekt.formatting)
    detektPlugins(libs.detekt.compose.rules)
}

detekt {
    toolVersion = libs.versions.detekt.get()
    config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
    buildUponDefaultConfig = true
    autoCorrect = true
    parallel = true
    source.setFrom(
        fileTree(rootDir) {
            include("**/*.kt")
            exclude("**/build/**", "**/.gradle/**")
        },
    )
}

tasks.withType<Detekt>().configureEach {
    jvmTarget = "17"
    reports {
        html.required.set(true)
        xml.required.set(false)
        sarif.required.set(false)
        txt.required.set(false)
        md.required.set(false)
    }
}

moduleGraphAssert {
    maxHeight = 6
    allowed = arrayOf(
        ":app -> .*",
        ":feature:.*:impl -> :feature:.*:api",
        ":feature:.*:impl -> :core:.*",
        ":feature:.*:api -> :core:common",
        ":feature:.*:api -> :core:mvi",
        ":core:.* -> :core:common",
        ":core:designsystem -> :core:mvi",
        ":core:testing -> .*",
    )
    restricted = arrayOf(
        ".* -X> :app",
        ":feature:.*:api -X> :feature:.*:impl",
        ":feature:.*:impl -X> :feature:.*:impl",
        // :core:testing is the one core module allowed to see feature APIs (it builds sample Drinks for tests).
        ":core:(?!testing).* -X> :feature:.*",
    )
    configurations = setOf("api", "implementation")
}

// "Moats": every fast automated guard in one task, cheapest first. Agents, the pre-push hook and CI all run it.
// Screenshot tests are excluded here (see build-logic TestConfig.kt) and run in `screenshots`.
val fastUnitTests = tasks.register("fastUnitTests") {
    group = "verification"
    description = "Unit, integration and Compose UI tests (JVM) of every module with src/test, without screenshot tests."
}

subprojects {
    val projectPath = path
    if (file("src/test").exists() && projectPath != ":konsist-test") {
        listOf("com.android.application", "com.android.library").forEach { pluginId ->
            pluginManager.withPlugin(pluginId) { fastUnitTests.configure { dependsOn("$projectPath:testDebugUnitTest") } }
        }
        pluginManager.withPlugin("org.jetbrains.kotlin.jvm") { fastUnitTests.configure { dependsOn("$projectPath:test") } }
    }
    // Order inside `moats`: detekt -> Konsist -> module graph -> unit tests (compilation still runs in parallel).
    tasks.withType<Test>().configureEach {
        if (path == ":konsist-test:test") mustRunAfter(":detekt") else mustRunAfter(":assertModuleGraph")
    }
}

// The module-graph plugin registers its task after evaluation, hence `matching` instead of `named`.
tasks.matching { it.name == "assertModuleGraph" }.configureEach { mustRunAfter(":konsist-test:test") }

tasks.register("moats") {
    group = "verification"
    description = "All fast guards: detekt, Konsist, module graph, unit/integration/UI tests (no screenshots)."
    dependsOn("detekt", ":konsist-test:test", "assertModuleGraph", fastUnitTests)
}

// Screenshot moat: Roborazzi verify/record in every module that applies the plugin.
tasks.register("screenshots") {
    group = "verification"
    description = "Compares every screenshot test against the committed goldens (verifyRoborazziDebug)."
    dependsOn(subprojects.map { project -> project.tasks.matching { it.name == "verifyRoborazziDebug" } })
}

tasks.register("recordScreenshots") {
    group = "verification"
    description = "Re-records the screenshot goldens (recordRoborazziDebug)."
    dependsOn(subprojects.map { project -> project.tasks.matching { it.name == "recordRoborazziDebug" } })
}

// The pre-push hook runs `moats`; it installs itself on the first app build, like Drinkit's detekt hook.
val installGitHooks = tasks.register("installGitHooks") {
    group = "git hooks"
    description = "Copies scripts/pre-push into the git hooks directory."
    val source = layout.projectDirectory.file("scripts/pre-push").asFile
    val dotGit = layout.projectDirectory.file(".git").asFile
    inputs.file(source)
    doLast {
        // `.git` is a directory in a normal clone and a `gitdir:` pointer file in a worktree.
        val hooksDir = when {
            dotGit.isDirectory -> dotGit.resolve("hooks")
            dotGit.isFile -> {
                val worktreeGitDir = File(dotGit.readText().substringAfter("gitdir:").trim())
                val commonDirPointer = worktreeGitDir.resolve("commondir")
                val commonDir = if (commonDirPointer.isFile) {
                    worktreeGitDir.resolve(commonDirPointer.readText().trim()).canonicalFile
                } else {
                    worktreeGitDir.parentFile.parentFile
                }
                commonDir.resolve("hooks")
            }
            else -> null
        }
        if (hooksDir == null) {
            logger.lifecycle("installGitHooks: no .git found, skipping")
        } else {
            val target = hooksDir.resolve("pre-push")
            if (!target.exists() || target.readText() != source.readText()) {
                hooksDir.mkdirs()
                source.copyTo(target, overwrite = true)
                target.setExecutable(true)
                logger.lifecycle("installGitHooks: installed ${target.path}")
            }
        }
    }
}

project(":app") {
    tasks.matching { it.name == "preBuild" }.configureEach { dependsOn(installGitHooks) }
}
