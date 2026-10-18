package ru.otus.barista.data.impl.local

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

/** In-memory replacement for the DataStore-backed favorites; [favorites] can be arranged and inspected directly. */
internal class FakeFavoritesLocalDataSource(
    initial: Set<String> = emptySet(),
) : FavoritesLocalDataSource {

    val favorites: MutableStateFlow<Set<String>> = MutableStateFlow(initial)

    override fun observeFavorites(): Flow<Set<String>> = favorites

    override suspend fun setFavorite(id: String, isFavorite: Boolean) {
        favorites.update { current -> if (isFavorite) current + id else current - id }
    }
}
