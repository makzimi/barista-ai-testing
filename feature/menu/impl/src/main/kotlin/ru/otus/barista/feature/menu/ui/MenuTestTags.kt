package ru.otus.barista.feature.menu.ui

import ru.otus.barista.feature.menu.api.DrinkId
import ru.otus.barista.feature.menu.presentation.CategoryFilter

/** Test tags of the menu screen. Prefixed tags are completed with an id: `MenuTestTags.row(id)`. */
object MenuTestTags {
    const val SCREEN: String = "menu_screen"
    const val LIST: String = "menu_list"
    const val ROW: String = "menu_row_"
    const val FAVORITE: String = "menu_favorite_"
    const val SEARCH: String = "menu_search"
    const val CHIP: String = "menu_chip_"
    const val FAVORITES_FILTER: String = "menu_favorites_filter"
    const val SORT_BY_PRICE: String = "menu_sort_by_price"
    const val STALE_BANNER: String = "menu_stale_banner"
    const val LOADING: String = "menu_loading"
    const val EMPTY: String = "menu_empty"
    const val ERROR: String = "menu_error"

    fun row(id: DrinkId): String = ROW + id.value

    fun favorite(id: DrinkId): String = FAVORITE + id.value

    fun chip(category: CategoryFilter): String = CHIP + category.name.lowercase()
}
