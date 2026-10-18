package ru.otus.barista.feature.menu.di

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import ru.otus.barista.feature.menu.presentation.MenuStateMapper
import ru.otus.barista.feature.menu.presentation.MenuViewModel

/** Manual DI for the menu feature: the only place that knows how to build [MenuViewModel]. */
class MenuGraph(private val dependencies: MenuDependencies) {

    fun createViewModel(): MenuViewModel = with(dependencies) {
        MenuViewModel(
            getMenu = getMenu,
            observeFavorites = observeFavorites,
            toggleFavorite = toggleFavorite,
            applyHappyHour = applyHappyHour,
            filterAndSort = filterAndSortMenu,
            mapper = MenuStateMapper(),
            dispatchers = dispatchers,
        )
    }

    val viewModelFactory: ViewModelProvider.Factory = viewModelFactory {
        initializer { createViewModel() }
    }
}
