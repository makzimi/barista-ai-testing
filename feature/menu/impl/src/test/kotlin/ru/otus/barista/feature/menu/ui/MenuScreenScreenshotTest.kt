package ru.otus.barista.feature.menu.ui

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.unit.Density
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import ru.otus.barista.core.testing.screenshot.captureScreenshot
import ru.otus.barista.feature.menu.presentation.EmptyReason
import ru.otus.barista.feature.menu.presentation.ErrorKind
import ru.otus.barista.feature.menu.presentation.MenuState
import ru.otus.barista.feature.menu.presentation.MenuStatus

/**
 * Hand-written screenshots for the cases previews do not cover (font scale) plus the main states.
 * Goldens: `src/test/screenshots/MenuScreenScreenshotTest.<name>.png`.
 * `captureScreenshot` asserts too: under `verifyRoborazziDebug` it fails when the image differs from the golden.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = RobolectricDeviceQualifiers.Pixel5)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class MenuScreenScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `MenuScreen WHEN content in light theme SHOULD match golden`() {
        val state = MenuPreviewData.content

        composeRule.captureScreenshot("MenuScreenScreenshotTest.content_light") {
            MenuScreen(state = state, onAction = {})
        }
    }

    @Test
    @Config(qualifiers = "+night")
    fun `MenuScreen WHEN content in dark theme SHOULD match golden`() {
        val state = MenuPreviewData.content

        composeRule.captureScreenshot("MenuScreenScreenshotTest.content_dark") {
            MenuScreen(state = state, onAction = {})
        }
    }

    @Test
    fun `MenuScreen WHEN content with font scale 1_5 SHOULD match golden`() {
        val state = MenuPreviewData.content

        composeRule.captureScreenshot("MenuScreenScreenshotTest.content_font_scale_1_5") {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale = 1.5f)) {
                MenuScreen(state = state, onAction = {})
            }
        }
    }

    @Test
    fun `MenuScreen WHEN nothing found SHOULD match golden`() {
        val state = MenuState(status = MenuStatus.Empty(EmptyReason.NOTHING_FOUND), query = "раф")

        composeRule.captureScreenshot("MenuScreenScreenshotTest.empty") {
            MenuScreen(state = state, onAction = {})
        }
    }

    @Test
    fun `MenuScreen WHEN network error SHOULD match golden`() {
        val state = MenuState(status = MenuStatus.Error(ErrorKind.NETWORK))

        composeRule.captureScreenshot("MenuScreenScreenshotTest.error") {
            MenuScreen(state = state, onAction = {})
        }
    }
}
