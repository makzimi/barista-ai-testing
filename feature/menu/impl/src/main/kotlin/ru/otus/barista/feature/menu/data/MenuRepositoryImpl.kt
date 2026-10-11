package ru.otus.barista.feature.menu.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import ru.otus.barista.core.common.DispatcherProvider
import ru.otus.barista.core.common.runSuspendCatching
import ru.otus.barista.feature.menu.api.Drink
import ru.otus.barista.feature.menu.api.DrinkId
import ru.otus.barista.feature.menu.api.MenuError
import ru.otus.barista.feature.menu.api.MenuRepository
import ru.otus.barista.feature.menu.api.MenuSnapshot
import ru.otus.barista.feature.menu.data.local.FavoritesLocalDataSource
import ru.otus.barista.feature.menu.data.local.InMemoryMenuCache
import ru.otus.barista.feature.menu.data.remote.MenuApi
import ru.otus.barista.feature.menu.data.remote.toDomain
import ru.otus.barista.feature.menu.data.remote.toDomainOrNull

private const val HTTP_NOT_FOUND = 404

/**
 * Rule 7 (stale cache) and rule 8 (favorites merged on read) live here.
 *
 * - `getMenu(forceRefresh = false)` with a warm cache → cache, `isStale = false`, no network call.
 * - otherwise → remote; success is cached; failure → cache with `isStale = true`, or [MenuError.Network] if cold.
 */
class MenuRepositoryImpl(
    private val api: MenuApi,
    private val cache: InMemoryMenuCache,
    private val favorites: FavoritesLocalDataSource,
    private val dispatchers: DispatcherProvider,
) : MenuRepository {

    override suspend fun getMenu(forceRefresh: Boolean): Result<MenuSnapshot> = withContext(dispatchers.io) {
        val cached = cache.get()
        if (!forceRefresh && cached != null) {
            return@withContext Result.success(MenuSnapshot(drinks = cached.withFavorites(), isStale = false))
        }
        runSuspendCatching { api.getMenu().toDomain() }.fold(
            onSuccess = { drinks ->
                cache.put(drinks)
                Result.success(MenuSnapshot(drinks = drinks.withFavorites(), isStale = false))
            },
            onFailure = { error ->
                if (cached != null) {
                    Result.success(MenuSnapshot(drinks = cached.withFavorites(), isStale = true))
                } else {
                    Result.failure(MenuError.Network(error))
                }
            },
        )
    }

    override fun observeFavorites(): Flow<Set<DrinkId>> = favorites.observeFavorites()

    override suspend fun setFavorite(id: DrinkId, isFavorite: Boolean) = withContext(dispatchers.io) {
        favorites.setFavorite(id, isFavorite)
    }

    /** Fresh data from the backend; falls back to the cached menu when the backend is unavailable. */
    override suspend fun getDrink(id: DrinkId): Result<Drink> = withContext(dispatchers.io) {
        val remote = runSuspendCatching { api.getDrink(id.value).toDomainOrNull() ?: throw MenuError.NotFound(id) }
        val drink = remote.getOrNull() ?: cache.get()?.firstOrNull { it.id == id }
        if (drink != null) {
            Result.success(drink.copy(isFavorite = id in currentFavorites()))
        } else {
            Result.failure(remote.exceptionOrNull().toMenuError(id))
        }
    }

    private suspend fun List<Drink>.withFavorites(): List<Drink> {
        val favoriteIds = currentFavorites()
        return map { it.copy(isFavorite = it.id in favoriteIds) }
    }

    private suspend fun currentFavorites(): Set<DrinkId> = favorites.observeFavorites().first()

    private fun Throwable?.toMenuError(id: DrinkId): MenuError = when {
        this is MenuError -> this
        this is HttpException && code() == HTTP_NOT_FOUND -> MenuError.NotFound(id)
        else -> MenuError.Network(this)
    }
}
