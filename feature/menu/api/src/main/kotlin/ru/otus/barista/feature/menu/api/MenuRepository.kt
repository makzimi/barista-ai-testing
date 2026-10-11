package ru.otus.barista.feature.menu.api

import kotlinx.coroutines.flow.Flow

/** Menu data as returned by [MenuRepository.getMenu]. [isStale] = remote failed and cached data was served (rule 7). */
data class MenuSnapshot(
    val drinks: List<Drink>,
    val isStale: Boolean,
)

/** Failures of [MenuRepository]. */
sealed class MenuError(message: String, cause: Throwable? = null) : Exception(message, cause) {
    /** Remote failed and there was nothing cached to fall back to. */
    class Network(cause: Throwable? = null) : MenuError("Menu is unavailable", cause)

    /** The backend does not know this drink. */
    class NotFound(val id: DrinkId) : MenuError("Drink ${id.value} not found")
}

interface MenuRepository {
    /** Cached menu unless [forceRefresh]; on remote failure falls back to cache with `isStale = true`. */
    suspend fun getMenu(forceRefresh: Boolean): Result<MenuSnapshot>

    fun observeFavorites(): Flow<Set<DrinkId>>

    suspend fun setFavorite(id: DrinkId, isFavorite: Boolean)

    suspend fun getDrink(id: DrinkId): Result<Drink>
}
