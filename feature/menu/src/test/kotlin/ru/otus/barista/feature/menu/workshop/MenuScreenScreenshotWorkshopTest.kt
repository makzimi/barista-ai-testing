@file:Suppress("NoUnusedImports", "UnusedImports", "UnusedPrivateProperty")

package ru.otus.barista.feature.menu.workshop

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
import ru.otus.barista.feature.menu.ui.MenuPreviewData
import ru.otus.barista.feature.menu.ui.MenuScreen

/**
 * Live-coding copy of `ui/MenuScreenScreenshotTest` (LIVE_CODING_SCRIPT Part 4).
 * Uncomment one region at a time, then `./gradlew :feature:menu:impl:verifyRoborazziDebug`.
 * The golden names are the reference test's, so an uncommented step is verified against the committed PNGs.
 *
 * `captureScreenshot` = `setContent { BaristaTheme { … } }` + frozen clock + `LocalInspectionMode`
 * + `onRoot().captureRoboImage("src/test/screenshots/<name>.png", BaristaRoborazziOptions)`.
 * Break it live: change a padding in `BaristaBadge`, run verify, open the `<name>_compare.png`
 * that Roborazzi writes to `build/outputs/roborazzi`.
 */
// region Step 1 — also uncomment the runner (JUnit rejects a @RunWith class that has no @Test yet)
// @RunWith(RobolectricTestRunner::class)
// endregion
@Config(sdk = [34], qualifiers = RobolectricDeviceQualifiers.Pixel5)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class MenuScreenScreenshotWorkshopTest {

    @get:Rule
    val composeRule = createComposeRule()

    // region Step 1 — one screenshot by hand: the Content state, light theme
    // @Test
    // fun `MenuScreen WHEN content in light theme SHOULD match golden`() {
    //     val state = MenuPreviewData.content
    //
    //     composeRule.captureScreenshot("MenuScreenScreenshotTest.content_light") {
    //         MenuScreen(state = state, onAction = {})
    //     }
    // }
    // endregion

    // region Step 2 — dark theme is just a Robolectric qualifier
    // @Test
    // @Config(qualifiers = "+night")
    // fun `MenuScreen WHEN content in dark theme SHOULD match golden`() {
    //     val state = MenuPreviewData.content
    //
    //     composeRule.captureScreenshot("MenuScreenScreenshotTest.content_dark") {
    //         MenuScreen(state = state, onAction = {})
    //     }
    // }
    // endregion

    // region Step 3 — font scale 1.5: does the layout survive big text?
    // @Test
    // fun `MenuScreen WHEN content with font scale 1_5 SHOULD match golden`() {
    //     val state = MenuPreviewData.content
    //
    //     composeRule.captureScreenshot("MenuScreenScreenshotTest.content_font_scale_1_5") {
    //         val density = LocalDensity.current
    //         CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale = 1.5f)) {
    //             MenuScreen(state = state, onAction = {})
    //         }
    //     }
    // }
    // endregion
}
