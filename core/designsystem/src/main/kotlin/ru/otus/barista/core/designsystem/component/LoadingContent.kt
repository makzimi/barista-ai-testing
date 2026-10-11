package ru.otus.barista.core.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import ru.otus.barista.core.designsystem.ThemePreviews
import ru.otus.barista.core.designsystem.theme.BaristaTheme

@Composable
fun LoadingContent(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@ThemePreviews
@Composable
private fun LoadingContentPreview() {
    BaristaTheme {
        Surface {
            LoadingContent()
        }
    }
}
