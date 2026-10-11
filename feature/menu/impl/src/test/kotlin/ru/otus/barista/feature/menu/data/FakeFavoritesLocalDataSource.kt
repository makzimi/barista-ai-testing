package ru.otus.barista.feature.menu.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import ru.otus.barista.feature.menu.api.DrinkId
import ru.otus.barista.feature.menu.data.local.FavoritesLocalDataSource

/** In-memory replacement for the DataStore-backed favorites; [favorites] can be arranged and inspected directly. */
class FakeFavoritesLocalDataSource(
    initial: Set<DrinkId> = emptySet(),
) : FavoritesLocalDataSource {

    val favorites: MutableStateFlow<Set<DrinkId>> = MutableStateFlow(initial)

    override fun observeFavorites(): Flow<Set<DrinkId>> = favorites

    override suspend fun setFavorite(id: DrinkId, isFavorite: Boolean) {
        favorites.update { current -> if (isFavorite) current + id else current - id }
    }
}
