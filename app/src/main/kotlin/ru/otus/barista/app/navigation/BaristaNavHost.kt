package ru.otus.barista.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import ru.otus.barista.app.debug.DebugMenu
import ru.otus.barista.app.di.AppGraph
import ru.otus.barista.feature.drinkdetails.navigation.drinkDetailsRoute
import ru.otus.barista.feature.drinkdetails.navigation.drinkDetailsScreen
import ru.otus.barista.feature.menu.navigation.MENU_ROUTE
import ru.otus.barista.feature.menu.navigation.menuScreen

@Composable
fun BaristaNavHost(
    graph: AppGraph,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(navController = navController, startDestination = MENU_ROUTE, modifier = modifier) {
        menuScreen(
            dependencies = graph,
            onOpenDrink = { id -> navController.navigate(drinkDetailsRoute(id.value)) },
            topBarActions = { DebugMenu(config = graph.fakeBackendConfig) },
        )
        drinkDetailsScreen(
            dependencies = graph,
            onBack = {
                if (navController.previousBackStackEntry != null) navController.popBackStack()
            },
        )
    }
}
