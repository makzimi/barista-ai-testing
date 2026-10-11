package ru.otus.barista.feature.menu.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import ru.otus.barista.core.designsystem.ThemePreviews
import ru.otus.barista.core.designsystem.component.EmptyContent
import ru.otus.barista.core.designsystem.component.ErrorContent
import ru.otus.barista.core.designsystem.component.LoadingContent
import ru.otus.barista.core.designsystem.theme.BaristaSpacing
import ru.otus.barista.core.designsystem.theme.BaristaTheme
import ru.otus.barista.feature.menu.R
import ru.otus.barista.feature.menu.presentation.CategoryFilter
import ru.otus.barista.feature.menu.presentation.EmptyReason
import ru.otus.barista.feature.menu.presentation.ErrorKind
import ru.otus.barista.feature.menu.presentation.MenuAction
import ru.otus.barista.feature.menu.presentation.MenuState
import ru.otus.barista.feature.menu.presentation.MenuStatus

/**
 * Stateless menu screen: renders [state], reports every user intent through [onAction].
 * [topBarActions] lets the app add items to the top bar (the debug "Fail next request" menu).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(
    state: MenuState,
    onAction: (MenuAction) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    topBarActions: @Composable RowScope.() -> Unit = {},
) {
    Scaffold(
        modifier = modifier.testTag(MenuTestTags.SCREEN),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.menu_title)) },
                actions = topBarActions,
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
        ) {
            SearchField(
                query = state.query,
                onQueryChange = { onAction(MenuAction.Search(it)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = BaristaSpacing.Large, vertical = BaristaSpacing.XSmall),
            )
            CategoryChips(
                selected = state.category,
                favoritesOnly = state.favoritesOnly,
                sort = state.sort,
                onAction = onAction,
                modifier = Modifier.fillMaxWidth(),
            )
            if (state.isStale) {
                StaleBanner(modifier = Modifier.fillMaxWidth())
            }
            PullToRefreshBox(
                isRefreshing = state.isRefreshing,
                onRefresh = { onAction(MenuAction.Refresh) },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) {
                MenuContent(state = state, onAction = onAction)
            }
        }
    }
}

@Composable
private fun MenuContent(
    state: MenuState,
    onAction: (MenuAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (val status = state.status) {
        MenuStatus.Loading -> LoadingContent(modifier = modifier.testTag(MenuTestTags.LOADING))
        MenuStatus.Content -> LazyColumn(modifier = modifier.fillMaxSize().testTag(MenuTestTags.LIST)) {
            items(state.items, key = { it.id.value }) { item ->
                MenuRow(
                    item = item,
                    onClick = { onAction(MenuAction.OpenDrink(item.id)) },
                    onToggleFavorite = { onAction(MenuAction.ToggleFavorite(item.id)) },
                )
                HorizontalDivider()
            }
        }
        is MenuStatus.Empty -> EmptyContent(
            title = stringResource(status.reason.titleRes()),
            message = stringResource(status.reason.messageRes()),
            // Scrollable, so pull-to-refresh also works on the empty state.
            modifier = modifier
                .verticalScroll(rememberScrollState())
                .testTag(MenuTestTags.EMPTY),
        )
        is MenuStatus.Error -> ErrorContent(
            title = stringResource(status.kind.titleRes()),
            message = status.kind.messageRes()?.let { stringResource(it) },
            onRetry = { onAction(MenuAction.Retry) },
            modifier = modifier.testTag(MenuTestTags.ERROR),
        )
    }
}

@Composable
private fun StaleBanner(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(R.string.menu_stale_banner),
        modifier = modifier
            .testTag(MenuTestTags.STALE_BANNER)
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(horizontal = BaristaSpacing.Large, vertical = BaristaSpacing.Small),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSecondaryContainer,
    )
}

private fun EmptyReason.titleRes(): Int = when (this) {
    EmptyReason.NO_FAVORITES -> R.string.menu_empty_no_favorites_title
    EmptyReason.NOTHING_FOUND -> R.string.menu_empty_nothing_found_title
    EmptyReason.MENU_EMPTY -> R.string.menu_empty_menu_title
}

private fun EmptyReason.messageRes(): Int = when (this) {
    EmptyReason.NO_FAVORITES -> R.string.menu_empty_no_favorites_message
    EmptyReason.NOTHING_FOUND -> R.string.menu_empty_nothing_found_message
    EmptyReason.MENU_EMPTY -> R.string.menu_empty_menu_message
}

private fun ErrorKind.titleRes(): Int = when (this) {
    ErrorKind.NETWORK -> R.string.menu_error_network_title
    ErrorKind.UNKNOWN -> R.string.menu_error_unknown_title
}

private fun ErrorKind.messageRes(): Int? = when (this) {
    ErrorKind.NETWORK -> R.string.menu_error_network_message
    ErrorKind.UNKNOWN -> null
}

@ThemePreviews
@Composable
private fun MenuScreenLoadingPreview() {
    BaristaTheme {
        MenuScreen(state = MenuState(status = MenuStatus.Loading), onAction = {})
    }
}

@ThemePreviews
@Composable
private fun MenuScreenContentPreview() {
    BaristaTheme {
        MenuScreen(state = MenuPreviewData.content, onAction = {})
    }
}

@ThemePreviews
@Composable
private fun MenuScreenStalePreview() {
    BaristaTheme {
        MenuScreen(state = MenuPreviewData.content.copy(isStale = true), onAction = {})
    }
}

@ThemePreviews
@Composable
private fun MenuScreenEmptyPreview() {
    BaristaTheme {
        MenuScreen(
            state = MenuState(
                status = MenuStatus.Empty(EmptyReason.NOTHING_FOUND),
                category = CategoryFilter.TEA,
                query = "раф",
            ),
            onAction = {},
        )
    }
}

@ThemePreviews
@Composable
private fun MenuScreenNoFavoritesPreview() {
    BaristaTheme {
        MenuScreen(
            state = MenuState(status = MenuStatus.Empty(EmptyReason.NO_FAVORITES), favoritesOnly = true),
            onAction = {},
        )
    }
}

@ThemePreviews
@Composable
private fun MenuScreenErrorPreview() {
    BaristaTheme {
        MenuScreen(state = MenuState(status = MenuStatus.Error(ErrorKind.NETWORK)), onAction = {})
    }
}
