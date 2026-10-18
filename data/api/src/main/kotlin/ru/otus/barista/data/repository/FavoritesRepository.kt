package ru.otus.barista.data.repository

import kotlinx.coroutines.flow.Flow

/** Ids of the drinks the user marked as favorite, stored on the device (rule 8). */
interface FavoritesRepository {
    fun observe(): Flow<Set<String>>

    suspend fun set(id: String, isFavorite: Boolean)
}
