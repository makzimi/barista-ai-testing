package ru.otus.barista.data.repository

import ru.otus.barista.data.model.DataError
import ru.otus.barista.data.model.DrinkDto
import ru.otus.barista.data.model.MenuSnapshotData

/** The menu as the backend describes it. Failures are [DataError]s. */
interface MenuRepository {
    /** Cached menu unless [forceRefresh]; on remote failure falls back to cache with `isStale = true` (rule 7). */
    suspend fun getMenu(forceRefresh: Boolean): Result<MenuSnapshotData>

    /** Fresh drink from the backend; falls back to the cached menu when the backend is unavailable. */
    suspend fun getDrink(id: String): Result<DrinkDto>
}
