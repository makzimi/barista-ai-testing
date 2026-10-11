package ru.otus.barista.feature.menu.presentation

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import ru.otus.barista.core.mvi.MviState
import ru.otus.barista.feature.menu.api.Category
import ru.otus.barista.feature.menu.api.DrinkId
import ru.otus.barista.feature.menu.domain.MenuSort

/**
 * Everything the menu screen renders. [query] is what the user typed (updated immediately);
 * the list is filtered by it only after the 300 ms debounce.
 */
@Immutable
data class MenuState(
    val status: MenuStatus = MenuStatus.Loading,
    val items: ImmutableList<MenuItemState> = persistentListOf(),
    val category: CategoryFilter = CategoryFilter.ALL,
    val favoritesOnly: Boolean = false,
    val sort: MenuSort = MenuSort.BY_NAME,
    val query: String = "",
    val isRefreshing: Boolean = false,
    val isStale: Boolean = false,
) : MviState

/** One row of the list, fully formatted: the UI only lays it out. */
@Immutable
data class MenuItemState(
    val id: DrinkId,
    val name: String,
    val description: String,
    val price: String,
    val oldPrice: String?,
    val discountLabel: String?,
    val isNew: Boolean,
    val isSoldOut: Boolean,
    val isFavorite: Boolean,
)

@Immutable
sealed interface MenuStatus {
    data object Loading : MenuStatus

    data object Content : MenuStatus

    data class Empty(val reason: EmptyReason) : MenuStatus

    data class Error(val kind: ErrorKind) : MenuStatus
}

enum class EmptyReason {
    /** The favorites filter is on and nothing is marked as favorite. */
    NO_FAVORITES,

    /** The search query / category / favorites filter matched nothing. */
    NOTHING_FOUND,

    /** The backend returned an empty menu. */
    MENU_EMPTY,
}

enum class ErrorKind {
    NETWORK,
    UNKNOWN,
}

/** Category chips: "All" plus every [Category]. */
enum class CategoryFilter(val category: Category?) {
    ALL(null),
    COFFEE(Category.COFFEE),
    TEA(Category.TEA),
    SEASONAL(Category.SEASONAL),
}
