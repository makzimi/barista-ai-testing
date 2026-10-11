package ru.otus.barista.core.designsystem

import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Preview

/**
 * Multipreview: light + dark. Every screen and major component has at least one `@ThemePreviews` function;
 * the screenshot tests (ComposablePreviewScanner + Roborazzi) turn each of them into two golden images.
 */
@Preview(name = "light", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
annotation class ThemePreviews
