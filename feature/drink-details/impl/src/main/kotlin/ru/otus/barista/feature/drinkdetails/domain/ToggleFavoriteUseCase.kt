package ru.otus.barista.feature.drinkdetails.domain

import ru.otus.barista.core.common.runSuspendCatching
import ru.otus.barista.feature.menu.api.DrinkId
import ru.otus.barista.feature.menu.api.MenuRepository

/**
 * Same as the menu's use case: `impl` modules may not depend on each other, so this small class is
 * duplicated on purpose instead of creating a shared dependency.
 */
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
