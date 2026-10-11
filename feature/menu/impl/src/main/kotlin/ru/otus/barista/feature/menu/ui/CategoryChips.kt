package ru.otus.barista.feature.menu.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import ru.otus.barista.core.designsystem.BaristaIcons
import ru.otus.barista.core.designsystem.ThemePreviews
import ru.otus.barista.core.designsystem.theme.BaristaSpacing
import ru.otus.barista.core.designsystem.theme.BaristaTheme
import ru.otus.barista.feature.menu.R
import ru.otus.barista.feature.menu.domain.MenuSort
import ru.otus.barista.feature.menu.presentation.CategoryFilter
import ru.otus.barista.feature.menu.presentation.MenuAction

/** Category chips + "Favorites" filter + "Cheapest first" sort, in one horizontally scrolling row. */
@Composable
fun CategoryChips(
    selected: CategoryFilter,
    favoritesOnly: Boolean,
    sort: MenuSort,
    onAction: (MenuAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = BaristaSpacing.Large),
        horizontalArrangement = Arrangement.spacedBy(BaristaSpacing.Small),
    ) {
        items(CategoryFilter.entries, key = { it.name }) { category ->
            FilterChip(
                selected = category == selected,
                onClick = { onAction(MenuAction.SelectCategory(category)) },
                label = { Text(stringResource(category.labelRes())) },
                modifier = Modifier.testTag(MenuTestTags.chip(category)),
            )
        }
        item(key = "favorites") {
            FilterChip(
                selected = favoritesOnly,
                onClick = { onAction(MenuAction.ToggleFavoritesOnly) },
                label = { Text(stringResource(R.string.menu_filter_favorites)) },
                leadingIcon = { Icon(painter = painterResource(BaristaIcons.Favorite), contentDescription = null) },
                modifier = Modifier.testTag(MenuTestTags.FAVORITES_FILTER),
            )
        }
        item(key = "sort") {
            val byPrice = sort == MenuSort.BY_PRICE
            FilterChip(
                selected = byPrice,
                onClick = { onAction(MenuAction.ChangeSort(if (byPrice) MenuSort.BY_NAME else MenuSort.BY_PRICE)) },
                label = { Text(stringResource(R.string.menu_sort_by_price)) },
                modifier = Modifier.testTag(MenuTestTags.SORT_BY_PRICE),
            )
        }
    }
}

private fun CategoryFilter.labelRes(): Int = when (this) {
    CategoryFilter.ALL -> R.string.menu_category_all
    CategoryFilter.COFFEE -> R.string.menu_category_coffee
    CategoryFilter.TEA -> R.string.menu_category_tea
    CategoryFilter.SEASONAL -> R.string.menu_category_seasonal
}

@ThemePreviews
@Composable
private fun CategoryChipsPreview() {
    BaristaTheme {
        Surface {
            CategoryChips(
                selected = CategoryFilter.COFFEE,
                favoritesOnly = true,
                sort = MenuSort.BY_NAME,
                onAction = {},
            )
        }
    }
}
