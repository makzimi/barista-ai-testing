package ru.otus.barista.core.testing.screenshot

import com.github.takahirom.roborazzi.AndroidComposePreviewTester
import com.github.takahirom.roborazzi.ComposePreviewTester
import com.github.takahirom.roborazzi.ComposePreviewTester.TestParameter.JUnit4TestParameter.AndroidPreviewJUnit4TestParameter
import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import com.github.takahirom.roborazzi.captureRoboImage
import com.github.takahirom.roborazzi.composeTestRule
import com.github.takahirom.roborazzi.effectiveRenderScale
import com.github.takahirom.roborazzi.inspectionMode
import com.github.takahirom.roborazzi.manualAdvance
import com.github.takahirom.roborazzi.toRoborazziComposeOptions
import sergio.sastre.composable.preview.scanner.android.AndroidPreviewInfo
import sergio.sastre.composable.preview.scanner.core.preview.ComposablePreview

/**
 * Turns every `@Preview` (in practice every `@ThemePreviews` light + dark pair) of a module into a screenshot test.
 * Wired in the module's `roborazzi { generateComposePreviewRobolectricTests { testerQualifiedClassName = ... } }`.
 *
 * Compared with Roborazzi's default tester it:
 * - skips previews annotated with [DoNotTestPreview];
 * - freezes the compose clock (first frame only) and sets `LocalInspectionMode = true`, so animations and
 *   infinite progress indicators are deterministic;
 * - uses [BaristaRoborazziOptions] and names goldens `<fqcn>.<PreviewFunction>.<previewName>.png`,
 *   e.g. `ru.otus.barista.feature.menu.ui.MenuScreenKt.MenuScreenContentPreview.dark.png`.
 */
@OptIn(ExperimentalRoborazziApi::class)
class BaristaPreviewTester : ComposePreviewTester<AndroidPreviewJUnit4TestParameter> {

    private val defaultTester = AndroidComposePreviewTester()

    override fun options(): ComposePreviewTester.Options = defaultTester.options()

    override fun testParameters(): List<AndroidPreviewJUnit4TestParameter> =
        defaultTester.testParameters().filterNot { it.preview.isExcludedFromScreenshots() }

    override fun test(testParameter: AndroidPreviewJUnit4TestParameter) {
        val preview = testParameter.preview
        val composeTestRule = testParameter.composeTestRule
        val composeOptions = preview
            .toRoborazziComposeOptions(preview.effectiveRenderScale(options().renderScale))
            .builder()
            .composeTestRule(composeTestRule)
            .manualAdvance(composeTestRule, advanceTimeMillis = 0)
            .inspectionMode(true)
            .build()

        preview.captureRoboImage(
            filePath = "$SCREENSHOTS_DIR/${preview.screenshotName()}.png",
            roborazziOptions = BaristaRoborazziOptions,
            roborazziComposeOptions = composeOptions,
        )
    }
}

private fun ComposablePreview<AndroidPreviewInfo>.screenshotName(): String =
    listOf(declaringClass, methodName, previewInfo.name)
        .filter { it.isNotBlank() }
        .joinToString(separator = ".")

private fun ComposablePreview<*>.isExcludedFromScreenshots(): Boolean {
    val declaringMethods = runCatching { Class.forName(declaringClass).declaredMethods }.getOrNull().orEmpty()
    return declaringMethods
        .filter { it.name == methodName }
        .any { method ->
            method.annotations.any { it.annotationClass.simpleName == DoNotTestPreview::class.simpleName }
        }
}
