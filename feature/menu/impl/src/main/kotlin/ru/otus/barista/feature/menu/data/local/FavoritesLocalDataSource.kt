package ru.otus.barista.feature.menu.data.local

import kotlinx.coroutines.flow.Flow
import ru.otus.barista.feature.menu.api.DrinkId

/** Favorite drink ids stored on the device (rule 8). */
interface FavoritesLocalDataSource {
    fun observeFavorites(): Flow<Set<DrinkId>>

    suspend fun setFavorite(id: DrinkId, isFavorite: Boolean)
}
