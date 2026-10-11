package ru.otus.barista.feature.menu.domain

import ru.otus.barista.feature.menu.api.Category

enum class MenuSort {
    BY_NAME,
    BY_PRICE,
}

/** [category] `null` means "all categories". A blank [query] matches everything. */
data class MenuFilter(
    val category: Category? = null,
    val favoritesOnly: Boolean = false,
    val query: String = "",
    val sort: MenuSort = MenuSort.BY_NAME,
)
