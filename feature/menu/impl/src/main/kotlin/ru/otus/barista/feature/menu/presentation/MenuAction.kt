package ru.otus.barista.feature.menu.presentation

import ru.otus.barista.core.mvi.MviAction
import ru.otus.barista.feature.menu.api.DrinkId
import ru.otus.barista.feature.menu.domain.MenuSort

sealed interface MenuAction : MviAction {
    /** Pull-to-refresh. */
    data object Refresh : MenuAction

    /** "Retry" on the error state. */
    data object Retry : MenuAction

    data class SelectCategory(val category: CategoryFilter) : MenuAction

    data object ToggleFavoritesOnly : MenuAction

    data class ChangeSort(val sort: MenuSort) : MenuAction

    data class Search(val query: String) : MenuAction

    data class ToggleFavorite(val id: DrinkId) : MenuAction

    data class OpenDrink(val id: DrinkId) : MenuAction
}
