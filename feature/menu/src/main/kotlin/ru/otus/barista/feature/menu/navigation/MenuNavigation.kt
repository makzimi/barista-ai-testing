package ru.otus.barista.feature.menu.navigation

import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import ru.otus.barista.domain.model.DrinkId
import ru.otus.barista.feature.menu.di.MenuDependencies
import ru.otus.barista.feature.menu.di.MenuGraph
import ru.otus.barista.feature.menu.ui.MenuRoute

/** Route of the menu screen; the app's start destination. */
const val MENU_ROUTE: String = "menu"

fun NavGraphBuilder.menuScreen(
    dependencies: MenuDependencies,
    onOpenDrink: (DrinkId) -> Unit,
    topBarActions: @Composable RowScope.() -> Unit = {},
) {
    composable(MENU_ROUTE) {
        val graph = remember(dependencies) { MenuGraph(dependencies) }
        MenuRoute(
            viewModel = viewModel(factory = graph.viewModelFactory),
            onOpenDrink = onOpenDrink,
            topBarActions = topBarActions,
        )
    }
}
