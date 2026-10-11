package ru.otus.barista.feature.drinkdetails.navigation

import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import ru.otus.barista.feature.drinkdetails.api.DRINK_DETAILS_ROUTE
import ru.otus.barista.feature.drinkdetails.api.DRINK_ID_ARG
import ru.otus.barista.feature.drinkdetails.di.DrinkDetailsDependencies
import ru.otus.barista.feature.drinkdetails.di.DrinkDetailsGraph
import ru.otus.barista.feature.drinkdetails.ui.DrinkDetailsRoute
import ru.otus.barista.feature.menu.api.DrinkId

fun NavGraphBuilder.drinkDetailsScreen(
    dependencies: DrinkDetailsDependencies,
    onBack: () -> Unit,
) {
    composable(
        route = DRINK_DETAILS_ROUTE,
        arguments = listOf(navArgument(DRINK_ID_ARG) { type = NavType.StringType }),
    ) { entry ->
        val drinkId = DrinkId(checkNotNull(entry.arguments?.getString(DRINK_ID_ARG)))
        val graph = remember(dependencies) { DrinkDetailsGraph(dependencies) }
        DrinkDetailsRoute(
            viewModel = viewModel(factory = graph.viewModelFactory(drinkId)),
            onBack = onBack,
        )
    }
}
