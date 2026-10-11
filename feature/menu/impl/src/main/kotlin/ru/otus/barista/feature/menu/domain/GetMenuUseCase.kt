package ru.otus.barista.feature.menu.domain

import ru.otus.barista.feature.menu.api.MenuRepository
import ru.otus.barista.feature.menu.api.MenuSnapshot

class GetMenuUseCase(
    private val repository: MenuRepository,
) {
    suspend operator fun invoke(forceRefresh: Boolean): Result<MenuSnapshot> = repository.getMenu(forceRefresh)
}
