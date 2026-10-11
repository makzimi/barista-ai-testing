package ru.otus.barista.feature.menu.ui

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import ru.otus.barista.feature.menu.R
import ru.otus.barista.feature.menu.api.DrinkId
import ru.otus.barista.feature.menu.presentation.MenuEvent
import ru.otus.barista.feature.menu.presentation.MenuMessage
import ru.otus.barista.feature.menu.presentation.MenuViewModel

/** Glue between [MenuViewModel] and the stateless [MenuScreen]: collects state, handles one-shot events. */
@Composable
fun MenuRoute(
    viewModel: MenuViewModel,
    onOpenDrink: (DrinkId) -> Unit,
    modifier: Modifier = Modifier,
    topBarActions: @Composable RowScope.() -> Unit = {},
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val currentOnOpenDrink by rememberUpdatedState(onOpenDrink)
    val favoriteFailed = stringResource(R.string.menu_message_favorite_failed)
    val refreshFailed = stringResource(R.string.menu_message_refresh_failed)

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is MenuEvent.NavigateToDrink -> currentOnOpenDrink(event.id)
                is MenuEvent.ShowMessage -> launch {
                    val text = when (event.message) {
                        MenuMessage.FAVORITE_UPDATE_FAILED -> favoriteFailed
                        MenuMessage.REFRESH_FAILED -> refreshFailed
                    }
                    snackbarHostState.showSnackbar(text)
                }
            }
        }
    }

    MenuScreen(
        state = state,
        onAction = viewModel::onAction,
        modifier = modifier,
        snackbarHostState = snackbarHostState,
        topBarActions = topBarActions,
    )
}
