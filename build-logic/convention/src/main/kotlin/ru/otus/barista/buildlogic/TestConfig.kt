package ru.otus.barista.buildlogic

import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.withType

/** Roborazzi tests: hand-written `*ScreenshotTest` classes and the generated `RoborazziPreviewParameterizedTests`. */
private val SCREENSHOT_TEST_PATTERNS = listOf(
    "**/*ScreenshotTest*",
    "**/*ScreenshotWorkshopTest*",
    "**/*RoborazziPreviewParameterizedTests*",
    "**/*Roborazzi*",
)

/** Root tasks that imply `-PexcludeScreenshotTests` / `-PonlyScreenshotTests`, so nobody has to remember the flag. */
private val FAST_TEST_TASKS = setOf("moats", "fastUnitTests")
private val SCREENSHOT_TASKS = setOf("screenshots", "recordScreenshots")

/** The Roborazzi plugin's own tasks (`recordRoborazziDebug`, `verifyRoborazziDebug`, …) set the mode themselves. */
private val ROBORAZZI_TASK = Regex("(record|verify|compare)Roborazzi")
private val ROBORAZZI_MODE_PROPERTIES = listOf(
    "roborazzi.test.record",
    "roborazzi.test.compare",
    "roborazzi.test.verify"
)

/**
 * `-PexcludeScreenshotTests` (implied by `moats` / `fastUnitTests`): unit, integration and Compose UI tests only.
 * `-PonlyScreenshotTests` (implied by `screenshots` / `recordScreenshots`): Roborazzi tests only.
 * A module whose tests are all filtered out must not fail with "no tests found" (Gradle 9 default).
 *
 * Any other test run — the IDE gutter, `./gradlew :feature:menu:testDebugUnitTest --tests "*ScreenshotTest*"` —
 * gets `roborazzi.test.verify=true`: without a mode Roborazzi silently skips the comparison and the test is green
 * whatever the screen looks like. Recording stays explicit (`recordScreenshots` / `recordRoborazziDebug`).
 */
internal fun Project.configureTestFilters() {
    val requestedTasks = gradle.startParameter.taskNames.map { it.substringAfterLast(':') }.toSet()
    val excludeScreenshots = providers.gradleProperty("excludeScreenshotTests").isPresent ||
        requestedTasks.any { it in FAST_TEST_TASKS }
    val onlyScreenshots = providers.gradleProperty("onlyScreenshotTests").isPresent ||
        requestedTasks.any { it in SCREENSHOT_TASKS }
    val roborazziModeChosen = requestedTasks.any { it in SCREENSHOT_TASKS || ROBORAZZI_TASK.containsMatchIn(it) } ||
        ROBORAZZI_MODE_PROPERTIES.any { property ->
            providers.systemProperty(property).isPresent || providers.gradleProperty(property).isPresent
        }

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
        if (!roborazziModeChosen) {
            systemProperty("roborazzi.test.verify", "true")
        }
    }
}
