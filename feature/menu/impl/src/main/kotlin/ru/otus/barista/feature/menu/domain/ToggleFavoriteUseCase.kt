package ru.otus.barista.feature.menu.domain

import ru.otus.barista.core.common.runSuspendCatching
import ru.otus.barista.feature.menu.api.DrinkId
import ru.otus.barista.feature.menu.api.MenuRepository

class ToggleFavoriteUseCase(
    private val repository: MenuRepository,
) {
    /** Flips the favorite flag of [id]; returns the new value. */
    suspend operator fun invoke(id: DrinkId, isFavoriteNow: Boolean): Result<Boolean> = runSuspendCatching {
        val newValue = !isFavoriteNow
        repository.setFavorite(id, newValue)
        newValue
    }
}
