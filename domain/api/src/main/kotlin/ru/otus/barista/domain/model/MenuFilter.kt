package ru.otus.barista.domain.model

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
