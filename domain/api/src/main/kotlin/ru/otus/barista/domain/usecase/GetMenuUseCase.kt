package ru.otus.barista.domain.usecase

import ru.otus.barista.domain.model.MenuSnapshot

/** The menu with favorites merged in (rule 8); stale cache is reported via `isStale` (rule 7). */
interface GetMenuUseCase {
    suspend operator fun invoke(forceRefresh: Boolean): Result<MenuSnapshot>
}
