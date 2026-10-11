plugins {
    alias(libs.plugins.barista.kotlin.library)
}

dependencies {
    testImplementation(libs.konsist)
    testImplementation(libs.junit)
}

tasks.test {
    useJUnit()
    // Konsist reads every module's sources straight from disk, so make them task inputs
    // (otherwise Gradle would report a stale UP-TO-DATE result after a change elsewhere).
    inputs.files(
        fileTree(rootDir) {
            include("**/*.kt")
            exclude("**/build/**", "**/.gradle/**")
        },
    ).withPropertyName("konsistScannedSources")
}
