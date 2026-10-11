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

// "Moats": every automated guard in one task. Konsist and unit/screenshot tests are wired by later tasks.
tasks.register("moats") {
    group = "verification"
    description = "Runs all architecture and static-analysis guards."
    dependsOn("detekt", "assertModuleGraph")
    // wired by later tasks: ":konsist-test:test", unit tests, verifyRoborazzi
}
