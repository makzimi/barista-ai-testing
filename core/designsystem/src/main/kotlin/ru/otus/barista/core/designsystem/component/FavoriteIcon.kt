package ru.otus.barista.core.designsystem.component

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import ru.otus.barista.core.designsystem.BaristaIcons
import ru.otus.barista.core.designsystem.R
import ru.otus.barista.core.designsystem.ThemePreviews
import ru.otus.barista.core.designsystem.theme.BaristaTheme

/** Heart toggle. The content description says what a tap will do, so it doubles as a test selector. */
@Composable
fun FavoriteIcon(
    isFavorite: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    IconButton(onClick = onClick, modifier = modifier) {
        Icon(
            painter = painterResource(if (isFavorite) BaristaIcons.Favorite else BaristaIcons.FavoriteBorder),
            contentDescription = stringResource(
                if (isFavorite) R.string.ds_remove_from_favorites else R.string.ds_add_to_favorites,
            ),
            tint = if (isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@ThemePreviews
@Composable
private fun FavoriteIconPreview() {
    BaristaTheme {
        Surface {
            Row {
                FavoriteIcon(isFavorite = false, onClick = {})
                FavoriteIcon(isFavorite = true, onClick = {})
            }
        }
    }
}
