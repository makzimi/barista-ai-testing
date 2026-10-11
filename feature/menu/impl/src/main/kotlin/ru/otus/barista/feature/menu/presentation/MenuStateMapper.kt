package ru.otus.barista.feature.menu.presentation

import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import ru.otus.barista.feature.menu.api.MenuError
import ru.otus.barista.feature.menu.domain.MenuItem

/** Domain → [MenuState]. All formatting and the choice of empty reason live here, never in composables. */
class MenuStateMapper {

    /**
     * @param allItems the whole menu (used to pick the empty reason)
     * @param visibleItems what is left after filtering and sorting
     */
    fun mapContent(
        state: MenuState,
        allItems: List<MenuItem>,
        visibleItems: List<MenuItem>,
        isStale: Boolean,
    ): MenuState = state.copy(
        status = if (visibleItems.isEmpty()) MenuStatus.Empty(emptyReason(state, allItems)) else MenuStatus.Content,
        items = visibleItems.map(::mapItem).toImmutableList(),
        isRefreshing = false,
        isStale = isStale,
    )

    fun mapError(state: MenuState, error: Throwable): MenuState = state.copy(
        status = MenuStatus.Error(if (error is MenuError.Network) ErrorKind.NETWORK else ErrorKind.UNKNOWN),
        items = persistentListOf(),
        isRefreshing = false,
        isStale = false,
    )

    fun mapItem(item: MenuItem): MenuItemState {
        val drink = item.drink
        val discount = item.discountPercent
        return MenuItemState(
            id = drink.id,
            name = drink.name,
            description = drink.description,
            price = item.price.format(),
            oldPrice = if (discount != null) drink.basePrice.format() else null,
            discountLabel = discount?.let { "−$it%" },
            isNew = drink.isNew,
            isSoldOut = drink.isSoldOut,
            isFavorite = drink.isFavorite,
        )
    }

    private fun emptyReason(state: MenuState, allItems: List<MenuItem>): EmptyReason = when {
        allItems.isEmpty() -> EmptyReason.MENU_EMPTY
        state.favoritesOnly && allItems.none { it.drink.isFavorite } -> EmptyReason.NO_FAVORITES
        else -> EmptyReason.NOTHING_FOUND
    }
}
