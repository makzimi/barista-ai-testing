package ru.otus.barista.feature.menu.presentation

import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import ru.otus.barista.core.designsystem.BaristaImages
import ru.otus.barista.domain.model.MenuError
import ru.otus.barista.domain.model.MenuItem

/**
 * Domain → [MenuState]. All formatting, the choice of empty reason and of every illustration ([BaristaImages])
 * live here, never in composables.
 */
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
        status = if (visibleItems.isEmpty()) mapEmpty(emptyReason(state, allItems)) else MenuStatus.Content,
        items = visibleItems.map(::mapItem).toImmutableList(),
        isRefreshing = false,
        isStale = isStale,
    )

    fun mapError(state: MenuState, error: Throwable): MenuState = state.copy(
        status = MenuStatus.Error(
            kind = if (error is MenuError.Network) ErrorKind.NETWORK else ErrorKind.UNKNOWN,
            imageRes = BaristaImages.error,
        ),
        items = persistentListOf(),
        isRefreshing = false,
        isStale = false,
    )

    fun mapItem(item: MenuItem): MenuItemState {
        val drink = item.drink
        val discount = item.discountPercent
        return MenuItemState(
            id = drink.id,
            imageRes = BaristaImages.drink(drink.id.value),
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

    private fun mapEmpty(reason: EmptyReason): MenuStatus.Empty = MenuStatus.Empty(
        reason = reason,
        imageRes = when (reason) {
            EmptyReason.NO_FAVORITES -> BaristaImages.noFavorites
            EmptyReason.NOTHING_FOUND -> BaristaImages.nothingFound
            EmptyReason.MENU_EMPTY -> BaristaImages.menuEmpty
        },
    )

    private fun emptyReason(state: MenuState, allItems: List<MenuItem>): EmptyReason = when {
        allItems.isEmpty() -> EmptyReason.MENU_EMPTY
        state.favoritesOnly && allItems.none { it.drink.isFavorite } -> EmptyReason.NO_FAVORITES
        else -> EmptyReason.NOTHING_FOUND
    }
}
