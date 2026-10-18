package ru.otus.barista.core.testing

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import ru.otus.barista.data.repository.FavoritesRepository

/** One recorded [FavoritesRepository.set] call. */
data class FavoriteCall(
    val id: String,
    val isFavorite: Boolean,
)

/**
 * In-memory data-layer [FavoritesRepository], for use case tests in `:domain:impl`.
 * [favorites] is the stored set (arrange or inspect it directly); every `set` call is recorded in [calls];
 * [failSet] makes every `set` throw.
 */
class FakeFavoritesRepository(initial: Set<String> = emptySet()) : FavoritesRepository {

    val favorites: MutableStateFlow<Set<String>> = MutableStateFlow(initial)
    val calls: MutableList<FavoriteCall> = mutableListOf()
    var failSet: Throwable? = null

    override fun observe(): Flow<Set<String>> = favorites

    override suspend fun set(id: String, isFavorite: Boolean) {
        calls += FavoriteCall(id, isFavorite)
        failSet?.let { throw it }
        favorites.update { current -> if (isFavorite) current + id else current - id }
    }
}
