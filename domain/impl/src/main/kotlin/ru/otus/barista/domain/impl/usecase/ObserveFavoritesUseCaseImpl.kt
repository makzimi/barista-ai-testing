package ru.otus.barista.domain.impl.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.otus.barista.data.repository.FavoritesRepository
import ru.otus.barista.domain.model.DrinkId
import ru.otus.barista.domain.usecase.ObserveFavoritesUseCase

class ObserveFavoritesUseCaseImpl(
    private val favoritesRepository: FavoritesRepository,
) : ObserveFavoritesUseCase {
    override operator fun invoke(): Flow<Set<DrinkId>> =
        favoritesRepository.observe().map { ids -> ids.mapTo(mutableSetOf(), ::DrinkId) }
}
