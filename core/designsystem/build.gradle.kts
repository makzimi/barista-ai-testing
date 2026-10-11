@file:OptIn(com.github.takahirom.roborazzi.ExperimentalRoborazziApi::class)

plugins {
    alias(libs.plugins.barista.android.compose)
    alias(libs.plugins.roborazzi)
}

android {
    namespace = "ru.otus.barista.core.designsystem"
    // Roborazzi: render through the hardware pixel copy for faithful screenshots.
    testOptions.unitTests.all { it.systemProperties["robolectric.pixelCopyRenderMode"] = "hardware" }
}

dependencies {
    testImplementation(project(":core:testing"))
    testImplementation(libs.roborazzi)
    testImplementation(libs.roborazzi.compose)
    testImplementation(libs.roborazzi.junit.rule)
    testImplementation(libs.roborazzi.compose.preview.scanner.support)
    testImplementation(libs.composable.preview.scanner)
}

// Screenshot tests: every @ThemePreviews preview of this module becomes a golden (ComposablePreviewScanner),
// plus the hand-written *ScreenshotTest classes. Goldens are committed in src/test/screenshots.
roborazzi {
    outputDir.set(layout.projectDirectory.dir("src/test/screenshots"))
    generateComposePreviewRobolectricTests {
        enable.set(true)
        packages.set(listOf("ru.otus.barista.core.designsystem"))
        includePrivatePreviews.set(true)
        robolectricConfig.set(mapOf("sdk" to "[34]", "qualifiers" to "RobolectricDeviceQualifiers.Pixel5"))
        testerQualifiedClassName.set("ru.otus.barista.core.testing.screenshot.BaristaPreviewTester")
        // BaristaPreviewTester reads options().scanOptions (packages, includePrivatePreviews) itself.
        useScanOptionParametersInTester.set(true)
    }
}
