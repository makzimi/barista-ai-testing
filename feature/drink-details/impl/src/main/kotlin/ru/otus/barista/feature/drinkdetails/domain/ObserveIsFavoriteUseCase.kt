package ru.otus.barista.feature.drinkdetails.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import ru.otus.barista.feature.menu.api.DrinkId
import ru.otus.barista.feature.menu.api.MenuRepository

/** Favorite flag of one drink, shared with the menu list through the repository. */
class ObserveIsFavoriteUseCase(
    private val repository: MenuRepository,
) {
    operator fun invoke(id: DrinkId): Flow<Boolean> =
        repository.observeFavorites()
            .map { favorites -> id in favorites }
            .distinctUntilChanged()
}
