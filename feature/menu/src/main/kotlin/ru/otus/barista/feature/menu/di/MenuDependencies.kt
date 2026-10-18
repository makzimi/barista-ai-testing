package ru.otus.barista.feature.menu.di

import ru.otus.barista.core.common.DispatcherProvider
import ru.otus.barista.domain.usecase.ApplyHappyHourUseCase
import ru.otus.barista.domain.usecase.FilterAndSortMenuUseCase
import ru.otus.barista.domain.usecase.GetMenuUseCase
import ru.otus.barista.domain.usecase.ObserveFavoritesUseCase
import ru.otus.barista.domain.usecase.ToggleFavoriteUseCase

/**
 * What the menu feature needs from the outside world: use case interfaces only (`:domain:api`).
 * Implemented by `AppGraph` (and by test graphs).
 */
interface MenuDependencies {
    val getMenu: GetMenuUseCase
    val observeFavorites: ObserveFavoritesUseCase
    val toggleFavorite: ToggleFavoriteUseCase
    val applyHappyHour: ApplyHappyHourUseCase
    val filterAndSortMenu: FilterAndSortMenuUseCase
    val dispatchers: DispatcherProvider
}
