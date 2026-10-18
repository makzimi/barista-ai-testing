package ru.otus.barista.data.impl.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import ru.otus.barista.core.common.DispatcherProvider
import ru.otus.barista.data.impl.local.FavoritesLocalDataSource
import ru.otus.barista.data.repository.FavoritesRepository

/** Rule 8 (storage side): favorites persist on the device; writes go to the IO dispatcher. */
internal class FavoritesRepositoryImpl(
    private val local: FavoritesLocalDataSource,
    private val dispatchers: DispatcherProvider,
) : FavoritesRepository {

    override fun observe(): Flow<Set<String>> = local.observeFavorites()

    override suspend fun set(id: String, isFavorite: Boolean) = withContext(dispatchers.io) {
        local.setFavorite(id, isFavorite)
    }
}
