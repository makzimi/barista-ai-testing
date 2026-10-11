package ru.otus.barista.buildlogic

import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.withType

/** Roborazzi tests: hand-written `*ScreenshotTest` classes and the generated `RoborazziPreviewParameterizedTests`. */
private val SCREENSHOT_TEST_PATTERNS = listOf(
    "**/*ScreenshotTest*",
    "**/*RoborazziPreviewParameterizedTests*",
    "**/*Roborazzi*",
)

/** Root tasks that imply `-PexcludeScreenshotTests` / `-PonlyScreenshotTests`, so nobody has to remember the flag. */
private val FAST_TEST_TASKS = setOf("moats", "fastUnitTests")
private val SCREENSHOT_TASKS = setOf("screenshots", "recordScreenshots")

/**
 * `-PexcludeScreenshotTests` (implied by `moats` / `fastUnitTests`): unit, integration and Compose UI tests only.
 * `-PonlyScreenshotTests` (implied by `screenshots` / `recordScreenshots`): Roborazzi tests only.
 * A module whose tests are all filtered out must not fail with "no tests found" (Gradle 9 default).
 */
internal fun Project.configureTestFilters() {
    val requestedTasks = gradle.startParameter.taskNames.map { it.substringAfterLast(':') }.toSet()
    val excludeScreenshots = providers.gradleProperty("excludeScreenshotTests").isPresent ||
        requestedTasks.any { it in FAST_TEST_TASKS }
    val onlyScreenshots = providers.gradleProperty("onlyScreenshotTests").isPresent ||
        requestedTasks.any { it in SCREENSHOT_TASKS }

    tasks.withType<Test>().configureEach {
        when {
            excludeScreenshots -> {
                exclude(SCREENSHOT_TEST_PATTERNS)
                failOnNoDiscoveredTests.set(false)
            }
            onlyScreenshots -> {
                include(SCREENSHOT_TEST_PATTERNS)
                failOnNoDiscoveredTests.set(false)
            }
        }
    }
}
