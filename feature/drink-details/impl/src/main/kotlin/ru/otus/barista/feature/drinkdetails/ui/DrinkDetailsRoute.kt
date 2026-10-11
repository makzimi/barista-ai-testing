package ru.otus.barista.feature.drinkdetails.ui

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import ru.otus.barista.feature.drinkdetails.R
import ru.otus.barista.feature.drinkdetails.presentation.DrinkDetailsEvent
import ru.otus.barista.feature.drinkdetails.presentation.DrinkDetailsMessage
import ru.otus.barista.feature.drinkdetails.presentation.DrinkDetailsViewModel

/** Glue between [DrinkDetailsViewModel] and the stateless [DrinkDetailsScreen]. */
@Composable
fun DrinkDetailsRoute(
    viewModel: DrinkDetailsViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val currentOnBack by rememberUpdatedState(onBack)
    val context = LocalContext.current

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                DrinkDetailsEvent.NavigateBack -> currentOnBack()
                is DrinkDetailsEvent.AddedToCart -> launch {
                    snackbarHostState.showSnackbar(context.getString(R.string.details_added_to_cart, event.drinkName))
                }
                is DrinkDetailsEvent.ShowMessage -> launch {
                    val text = when (event.message) {
                        DrinkDetailsMessage.FAVORITE_UPDATE_FAILED -> R.string.details_message_favorite_failed
                    }
                    snackbarHostState.showSnackbar(context.getString(text))
                }
            }
        }
    }

    DrinkDetailsScreen(
        state = state,
        onAction = viewModel::onAction,
        modifier = modifier,
        snackbarHostState = snackbarHostState,
    )
}
