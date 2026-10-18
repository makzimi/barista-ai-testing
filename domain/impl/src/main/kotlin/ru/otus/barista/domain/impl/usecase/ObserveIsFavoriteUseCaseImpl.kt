package ru.otus.barista.domain.impl.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import ru.otus.barista.data.repository.FavoritesRepository
import ru.otus.barista.domain.model.DrinkId
import ru.otus.barista.domain.usecase.ObserveIsFavoriteUseCase

/** Favorite flag of one drink, shared with the menu list through the favorites repository. */
class ObserveIsFavoriteUseCaseImpl(
    private val favoritesRepository: FavoritesRepository,
) : ObserveIsFavoriteUseCase {
    override operator fun invoke(id: DrinkId): Flow<Boolean> =
        favoritesRepository.observe()
            .map { favorites -> id.value in favorites }
            .distinctUntilChanged()
}
