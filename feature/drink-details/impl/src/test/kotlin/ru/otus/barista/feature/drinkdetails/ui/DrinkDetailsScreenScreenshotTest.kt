package ru.otus.barista.feature.drinkdetails.ui

import androidx.compose.ui.test.junit4.v2.createComposeRule
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import ru.otus.barista.core.testing.screenshot.captureScreenshot

/**
 * Hand-written screenshots of the drink details screen.
 * Goldens: `src/test/screenshots/DrinkDetailsScreenScreenshotTest.<name>.png`.
 * `captureScreenshot` asserts too: under `verifyRoborazziDebug` it fails when the image differs from the golden.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = RobolectricDeviceQualifiers.Pixel5)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class DrinkDetailsScreenScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `DrinkDetailsScreen WHEN content in light theme SHOULD match golden`() {
        val state = DrinkDetailsPreviewData.cappuccino

        composeRule.captureScreenshot("DrinkDetailsScreenScreenshotTest.content_light") {
            DrinkDetailsScreen(state = state, onAction = {})
        }
    }

    @Test
    @Config(qualifiers = "+night")
    fun `DrinkDetailsScreen WHEN content in dark theme SHOULD match golden`() {
        val state = DrinkDetailsPreviewData.cappuccino

        composeRule.captureScreenshot("DrinkDetailsScreenScreenshotTest.content_dark") {
            DrinkDetailsScreen(state = state, onAction = {})
        }
    }

    @Test
    fun `DrinkDetailsScreen WHEN drink is sold out SHOULD match golden`() {
        val state = DrinkDetailsPreviewData.icedLatteSoldOut

        composeRule.captureScreenshot("DrinkDetailsScreenScreenshotTest.sold_out") {
            DrinkDetailsScreen(state = state, onAction = {})
        }
    }
}
