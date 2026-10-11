package ru.otus.barista.core.testing

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import ru.otus.barista.feature.menu.api.Drink
import ru.otus.barista.feature.menu.api.DrinkId
import ru.otus.barista.feature.menu.api.MenuError
import ru.otus.barista.feature.menu.api.MenuRepository
import ru.otus.barista.feature.menu.api.MenuSnapshot

/** One recorded [MenuRepository.setFavorite] call. */
data class FavoriteCall(
    val id: DrinkId,
    val isFavorite: Boolean,
)

/**
 * In-memory [MenuRepository] that behaves like the real one (favorites merged on every read) without I/O.
 *
 * - [setDrinks] arranges the menu (default: [Create.sampleMenu]).
 * - [failNextGetMenu] makes the next `getMenu` fail once; with [staleOnFailure] it instead serves the current
 *   drinks with `isStale = true`, like the real repository with a warm cache.
 * - [failNextGetDrink] makes the next `getDrink` fail once; an unknown id fails with [MenuError.NotFound].
 * - [failSetFavorite] makes every `setFavorite` throw.
 * - [favorites] is the stored favorites; every `setFavorite` call is recorded in [favoriteCalls].
 */
class FakeMenuRepository(drinks: List<Drink> = Create.sampleMenu()) : MenuRepository {

    private var drinks: List<Drink> = drinks

    var failNextGetMenu: Throwable? = null
    var staleOnFailure: Boolean = false
    var failNextGetDrink: Throwable? = null
    var failSetFavorite: Throwable? = null

    val favorites: MutableStateFlow<Set<DrinkId>> = MutableStateFlow(emptySet())
    val favoriteCalls: MutableList<FavoriteCall> = mutableListOf()

    /** `forceRefresh` of every `getMenu` call, in order. */
    val getMenuCalls: MutableList<Boolean> = mutableListOf()

    fun setDrinks(list: List<Drink>) {
        drinks = list
    }

    override suspend fun getMenu(forceRefresh: Boolean): Result<MenuSnapshot> {
        getMenuCalls += forceRefresh
        val failure = failNextGetMenu?.also { failNextGetMenu = null }
        return when {
            failure == null -> Result.success(MenuSnapshot(drinks = drinks.withFavorites(), isStale = false))
            staleOnFailure -> Result.success(MenuSnapshot(drinks = drinks.withFavorites(), isStale = true))
            else -> Result.failure(failure)
        }
    }

    override fun observeFavorites(): Flow<Set<DrinkId>> = favorites

    override suspend fun setFavorite(id: DrinkId, isFavorite: Boolean) {
        favoriteCalls += FavoriteCall(id, isFavorite)
        failSetFavorite?.let { throw it }
        favorites.update { current -> if (isFavorite) current + id else current - id }
    }

    override suspend fun getDrink(id: DrinkId): Result<Drink> {
        val failure = failNextGetDrink?.also { failNextGetDrink = null }
        if (failure != null) return Result.failure(failure)
        val drink = drinks.firstOrNull { it.id == id } ?: return Result.failure(MenuError.NotFound(id))
        return Result.success(drink.copy(isFavorite = id in favorites.value))
    }

    private fun List<Drink>.withFavorites(): List<Drink> {
        val ids = favorites.value
        return map { it.copy(isFavorite = it.id in ids) }
    }
}
