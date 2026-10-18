package ru.otus.barista.core.testing

import ru.otus.barista.data.model.DataError
import ru.otus.barista.data.model.DrinkDto
import ru.otus.barista.data.model.MenuSnapshotData
import ru.otus.barista.data.repository.MenuRepository

/**
 * In-memory data-layer [MenuRepository] (DTO-based), for use case tests in `:domain:impl`.
 *
 * - [setDrinks] arranges the menu (default: [Create.sampleMenuDto]).
 * - [failNextGetMenu] makes the next `getMenu` fail once; with [staleOnFailure] it instead serves the current
 *   drinks with `isStale = true`, like the real repository with a warm cache.
 * - [failNextGetDrink] makes the next `getDrink` fail once; an unknown id fails with [DataError.NotFound].
 * - every call is recorded: [getMenuCalls] (`forceRefresh`), [getDrinkCalls] (ids).
 */
class FakeMenuRepository(drinks: List<DrinkDto> = Create.sampleMenuDto()) : MenuRepository {

    private var drinks: List<DrinkDto> = drinks

    var failNextGetMenu: Throwable? = null
    var staleOnFailure: Boolean = false
    var failNextGetDrink: Throwable? = null

    val getMenuCalls: MutableList<Boolean> = mutableListOf()
    val getDrinkCalls: MutableList<String> = mutableListOf()

    fun setDrinks(list: List<DrinkDto>) {
        drinks = list
    }

    override suspend fun getMenu(forceRefresh: Boolean): Result<MenuSnapshotData> {
        getMenuCalls += forceRefresh
        val failure = failNextGetMenu?.also { failNextGetMenu = null }
        return when {
            failure == null -> Result.success(MenuSnapshotData(drinks = drinks, isStale = false))
            staleOnFailure -> Result.success(MenuSnapshotData(drinks = drinks, isStale = true))
            else -> Result.failure(failure)
        }
    }

    override suspend fun getDrink(id: String): Result<DrinkDto> {
        getDrinkCalls += id
        val failure = failNextGetDrink?.also { failNextGetDrink = null }
        if (failure != null) return Result.failure(failure)
        val drink = drinks.firstOrNull { it.id == id } ?: return Result.failure(DataError.NotFound(id))
        return Result.success(drink)
    }
}
