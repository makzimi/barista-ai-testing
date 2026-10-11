package ru.otus.barista.core.testing.screenshot

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import ru.otus.barista.core.designsystem.theme.BaristaTheme

/**
 * Renders [content] inside [BaristaTheme] and compares it with `src/test/screenshots/<name>.png`
 * (`./gradlew recordRoborazziDebug` writes the golden, `verifyRoborazziDebug` compares against it).
 *
 * Deterministic by construction: the compose clock does not auto-advance (animations stay on their first frame,
 * infinite ones like a progress spinner cannot hang the test) and `LocalInspectionMode` is `true`, as in a preview.
 * Light/dark comes from the Robolectric configuration: add `@Config(qualifiers = "+night")` for dark.
 */
fun ComposeContentTestRule.captureScreenshot(
    name: String,
    content: @Composable () -> Unit,
) {
    mainClock.autoAdvance = false
    setContent {
        CompositionLocalProvider(LocalInspectionMode provides true) {
            BaristaTheme(content = content)
        }
    }
    onRoot().captureRoboImage(
        filePath = "$SCREENSHOTS_DIR/$name.png",
        roborazziOptions = BaristaRoborazziOptions,
    )
    mainClock.autoAdvance = true
}
