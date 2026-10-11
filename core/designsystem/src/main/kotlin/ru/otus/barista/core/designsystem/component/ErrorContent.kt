package ru.otus.barista.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import ru.otus.barista.core.designsystem.DesignSystemTestTags
import ru.otus.barista.core.designsystem.R
import ru.otus.barista.core.designsystem.ThemePreviews
import ru.otus.barista.core.designsystem.theme.BaristaSpacing
import ru.otus.barista.core.designsystem.theme.BaristaTheme

@Composable
fun ErrorContent(
    title: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    message: String? = null,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(BaristaSpacing.XLarge),
        verticalArrangement = Arrangement.spacedBy(BaristaSpacing.Medium, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = title, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
        if (message != null) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
        Button(onClick = onRetry, modifier = Modifier.testTag(DesignSystemTestTags.RETRY)) {
            Text(text = stringResource(R.string.ds_retry))
        }
    }
}

@ThemePreviews
@Composable
private fun ErrorContentPreview() {
    BaristaTheme {
        Surface {
            ErrorContent(
                title = "Не удалось загрузить меню",
                message = "Проверьте соединение и попробуйте ещё раз",
                onRetry = {},
            )
        }
    }
}
