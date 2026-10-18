package ru.otus.barista.data.impl.repository

import kotlinx.coroutines.withContext
import retrofit2.HttpException
import ru.otus.barista.core.common.DispatcherProvider
import ru.otus.barista.core.common.runSuspendCatching
import ru.otus.barista.data.impl.local.InMemoryMenuCache
import ru.otus.barista.data.impl.remote.MenuApi
import ru.otus.barista.data.model.DataError
import ru.otus.barista.data.model.DrinkDto
import ru.otus.barista.data.model.MenuSnapshotData
import ru.otus.barista.data.repository.MenuRepository

private const val HTTP_NOT_FOUND = 404

/**
 * Rule 7 (stale cache) lives here.
 *
 * - `getMenu(forceRefresh = false)` with a warm cache → cache, `isStale = false`, no network call.
 * - otherwise → remote; success is cached; failure → cache with `isStale = true`, or [DataError.Network] if cold.
 */
internal class MenuRepositoryImpl(
    private val api: MenuApi,
    private val cache: InMemoryMenuCache,
    private val dispatchers: DispatcherProvider,
) : MenuRepository {

    override suspend fun getMenu(forceRefresh: Boolean): Result<MenuSnapshotData> = withContext(dispatchers.io) {
        val cached = cache.get()
        if (!forceRefresh && cached != null) {
            return@withContext Result.success(MenuSnapshotData(drinks = cached, isStale = false))
        }
        runSuspendCatching { api.getMenu().drinks }.fold(
            onSuccess = { drinks ->
                cache.put(drinks)
                Result.success(MenuSnapshotData(drinks = drinks, isStale = false))
            },
            onFailure = { error ->
                if (cached != null) {
                    Result.success(MenuSnapshotData(drinks = cached, isStale = true))
                } else {
                    Result.failure(DataError.Network(error))
                }
            },
        )
    }

    override suspend fun getDrink(id: String): Result<DrinkDto> = withContext(dispatchers.io) {
        val remote = runSuspendCatching { api.getDrink(id) }
        val drink = remote.getOrNull() ?: cache.get()?.firstOrNull { it.id == id }
        if (drink != null) {
            Result.success(drink)
        } else {
            Result.failure(remote.exceptionOrNull().toDataError(id))
        }
    }

    private fun Throwable?.toDataError(id: String): DataError = when {
        this is DataError -> this
        this is HttpException && code() == HTTP_NOT_FOUND -> DataError.NotFound(id)
        else -> DataError.Network(this)
    }
}
