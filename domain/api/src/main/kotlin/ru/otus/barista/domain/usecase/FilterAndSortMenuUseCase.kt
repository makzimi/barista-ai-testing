package ru.otus.barista.domain.usecase

import ru.otus.barista.domain.model.MenuFilter
import ru.otus.barista.domain.model.MenuItem

/** Rules 2–4: filter by category / favorites / query, sort by name or price, sold-out drinks last. */
interface FilterAndSortMenuUseCase {
    operator fun invoke(items: List<MenuItem>, filter: MenuFilter): List<MenuItem>
}
