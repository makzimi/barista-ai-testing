package ru.otus.barista.data.impl.local

import kotlinx.coroutines.flow.Flow

/** Favorite drink ids stored on the device (rule 8). */
internal interface FavoritesLocalDataSource {
    fun observeFavorites(): Flow<Set<String>>

    suspend fun setFavorite(id: String, isFavorite: Boolean)
}
