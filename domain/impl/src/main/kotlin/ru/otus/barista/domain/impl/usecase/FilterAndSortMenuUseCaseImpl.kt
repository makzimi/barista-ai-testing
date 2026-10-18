package ru.otus.barista.domain.impl.usecase

import ru.otus.barista.domain.model.MenuFilter
import ru.otus.barista.domain.model.MenuItem
import ru.otus.barista.domain.model.MenuSort
import ru.otus.barista.domain.usecase.FilterAndSortMenuUseCase

/**
 * Rules 2–4, pure:
 * - filter by category, favorites-only and search query (case-insensitive, name or description; blank = no filter);
 * - sort by name (case-insensitive) or by displayed price; the sort is stable;
 * - sold-out drinks always go last, whatever the sort.
 */
class FilterAndSortMenuUseCaseImpl : FilterAndSortMenuUseCase {

    override operator fun invoke(items: List<MenuItem>, filter: MenuFilter): List<MenuItem> {
        val query = filter.query.trim()
        val sortOrder = when (filter.sort) {
            MenuSort.BY_NAME -> compareBy(String.CASE_INSENSITIVE_ORDER) { item: MenuItem -> item.drink.name }
            MenuSort.BY_PRICE -> compareBy { item: MenuItem -> item.price }
        }
        return items
            .asSequence()
            .filter { filter.category == null || it.drink.category == filter.category }
            .filter { !filter.favoritesOnly || it.drink.isFavorite }
            .filter { query.isEmpty() || it.matches(query) }
            .sortedWith(compareBy<MenuItem> { it.drink.isSoldOut }.then(sortOrder))
            .toList()
    }

    private fun MenuItem.matches(query: String): Boolean =
        drink.name.contains(query, ignoreCase = true) || drink.description.contains(query, ignoreCase = true)
}
