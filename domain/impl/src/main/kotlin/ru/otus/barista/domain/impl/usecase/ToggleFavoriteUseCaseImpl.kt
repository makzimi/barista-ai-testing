package ru.otus.barista.domain.impl.usecase

import ru.otus.barista.core.common.runSuspendCatching
import ru.otus.barista.data.repository.FavoritesRepository
import ru.otus.barista.domain.model.DrinkId
import ru.otus.barista.domain.usecase.ToggleFavoriteUseCase

/** Shared by the menu list and the drink details screen (one implementation since the domain layer is shared). */
class ToggleFavoriteUseCaseImpl(
    private val favoritesRepository: FavoritesRepository,
) : ToggleFavoriteUseCase {
    /** Flips the favorite flag of [id]; returns the new value. */
    override suspend operator fun invoke(id: DrinkId, isFavoriteNow: Boolean): Result<Boolean> = runSuspendCatching {
        val newValue = !isFavoriteNow
        favoritesRepository.set(id.value, newValue)
        newValue
    }
}
