package ru.otus.barista.domain.usecase

import ru.otus.barista.domain.model.DrinkId

/** Flips the favorite flag of a drink; returns the new value. */
interface ToggleFavoriteUseCase {
    suspend operator fun invoke(id: DrinkId, isFavoriteNow: Boolean): Result<Boolean>
}
