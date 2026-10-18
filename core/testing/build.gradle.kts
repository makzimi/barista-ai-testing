plugins {
    // Compose (not plain library): the screenshot helpers in `screenshot/` are @Composable-aware.
    alias(libs.plugins.barista.android.compose)
}

android {
    namespace = "ru.otus.barista.core.testing"
}

// Consumed by other modules via testImplementation(project(":core:testing")); code lives in src/main on purpose.
dependencies {
    api(project(":core:common"))
    // Fakes of both seams: data-layer repositories (for use case tests) and use cases (for ViewModel tests).
    api(project(":data:api"))
    api(project(":domain:api"))
    api(libs.junit)
    api(libs.kotlinx.coroutines.test)

    // Screenshot testing (package ru.otus.barista.core.testing.screenshot): BaristaTheme + Roborazzi + preview scanner.
    implementation(project(":core:designsystem"))
    implementation(libs.androidx.compose.ui.test.junit4)
    implementation(libs.robolectric)
    implementation(libs.roborazzi)
    implementation(libs.roborazzi.compose)
    implementation(libs.roborazzi.compose.preview.scanner.support)
    implementation(libs.composable.preview.scanner)
}
