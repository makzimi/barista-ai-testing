package ru.otus.barista.feature.menu.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import ru.otus.barista.core.designsystem.ThemePreviews
import ru.otus.barista.core.designsystem.component.BadgeKind
import ru.otus.barista.core.designsystem.component.BaristaBadge
import ru.otus.barista.core.designsystem.component.FavoriteIcon
import ru.otus.barista.core.designsystem.component.PriceText
import ru.otus.barista.core.designsystem.theme.BaristaSpacing
import ru.otus.barista.core.designsystem.theme.BaristaTheme
import ru.otus.barista.feature.menu.R
import ru.otus.barista.feature.menu.presentation.MenuItemState

private const val SOLD_OUT_ALPHA = 0.6f

@Composable
fun MenuRow(
    item: MenuItemState,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .testTag(MenuTestTags.row(item.id))
            .clickable(onClick = onClick)
            .padding(start = BaristaSpacing.Large, top = BaristaSpacing.Medium, bottom = BaristaSpacing.Medium),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .alpha(if (item.isSoldOut) SOLD_OUT_ALPHA else 1f),
            verticalArrangement = Arrangement.spacedBy(BaristaSpacing.XSmall),
        ) {
            Text(text = item.name, style = MaterialTheme.typography.titleMedium)
            Text(
                text = item.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(BaristaSpacing.Small),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PriceText(price = item.price, oldPrice = item.oldPrice)
                MenuRowBadges(item)
            }
        }
        FavoriteIcon(
            isFavorite = item.isFavorite,
            onClick = onToggleFavorite,
            modifier = Modifier.testTag(MenuTestTags.favorite(item.id)),
        )
    }
}

@Composable
private fun MenuRowBadges(item: MenuItemState, modifier: Modifier = Modifier) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(BaristaSpacing.XSmall)) {
        if (item.isNew) {
            BaristaBadge(text = stringResource(R.string.menu_badge_new), kind = BadgeKind.New)
        }
        if (item.discountLabel != null) {
            BaristaBadge(text = item.discountLabel, kind = BadgeKind.Discount)
        }
        if (item.isSoldOut) {
            BaristaBadge(text = stringResource(R.string.menu_badge_sold_out), kind = BadgeKind.SoldOut)
        }
    }
}

@ThemePreviews
@Composable
private fun MenuRowPreview() {
    BaristaTheme {
        Surface {
            MenuRow(item = MenuPreviewData.flatWhite, onClick = {}, onToggleFavorite = {})
        }
    }
}

@ThemePreviews
@Composable
private fun MenuRowDiscountPreview() {
    BaristaTheme {
        Surface {
            MenuRow(item = MenuPreviewData.cappuccino, onClick = {}, onToggleFavorite = {})
        }
    }
}

@ThemePreviews
@Composable
private fun MenuRowNewPreview() {
    BaristaTheme {
        Surface {
            MenuRow(item = MenuPreviewData.pumpkinLatte, onClick = {}, onToggleFavorite = {})
        }
    }
}

@ThemePreviews
@Composable
private fun MenuRowSoldOutPreview() {
    BaristaTheme {
        Surface {
            MenuRow(item = MenuPreviewData.icedLatte, onClick = {}, onToggleFavorite = {})
        }
    }
}
