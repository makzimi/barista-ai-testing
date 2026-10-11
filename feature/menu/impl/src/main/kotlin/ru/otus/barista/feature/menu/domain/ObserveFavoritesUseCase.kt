package ru.otus.barista.feature.menu.domain

import kotlinx.coroutines.flow.Flow
import ru.otus.barista.feature.menu.api.DrinkId
import ru.otus.barista.feature.menu.api.MenuRepository

class ObserveFavoritesUseCase(
    private val repository: MenuRepository,
) {
    operator fun invoke(): Flow<Set<DrinkId>> = repository.observeFavorites()
}
