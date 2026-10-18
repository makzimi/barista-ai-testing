package ru.otus.barista.feature.menu.presentation

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import ru.otus.barista.core.common.DispatcherProvider
import ru.otus.barista.core.mvi.MviViewModel
import ru.otus.barista.domain.model.DrinkId
import ru.otus.barista.domain.model.MenuFilter
import ru.otus.barista.domain.model.MenuSnapshot
import ru.otus.barista.domain.model.withFavorites
import ru.otus.barista.domain.usecase.ApplyHappyHourUseCase
import ru.otus.barista.domain.usecase.FilterAndSortMenuUseCase
import ru.otus.barista.domain.usecase.GetMenuUseCase
import ru.otus.barista.domain.usecase.ObserveFavoritesUseCase
import ru.otus.barista.domain.usecase.ToggleFavoriteUseCase

/**
 * Loads the menu on start, keeps it in sync with favorites, and re-filters on every user action.
 * Search is debounced by [SEARCH_DEBOUNCE_MS] on [DispatcherProvider.main], so tests drive it with virtual time.
 */
@OptIn(FlowPreview::class)
class MenuViewModel(
    private val getMenu: GetMenuUseCase,
    private val observeFavorites: ObserveFavoritesUseCase,
    private val toggleFavorite: ToggleFavoriteUseCase,
    private val applyHappyHour: ApplyHappyHourUseCase,
    private val filterAndSort: FilterAndSortMenuUseCase,
    private val mapper: MenuStateMapper,
    private val dispatchers: DispatcherProvider,
) : MviViewModel<MenuState, MenuAction, MenuEvent>(MenuState()) {

    private val searchQuery = MutableStateFlow("")
    private var appliedQuery = ""
    private var snapshot: MenuSnapshot? = null
    private var favoriteIds: Set<DrinkId>? = null
    private var loadJob: Job? = null

    init {
        viewModelScope.launch(dispatchers.main) {
            searchQuery.debounce(SEARCH_DEBOUNCE_MS).collect { query ->
                appliedQuery = query
                render()
            }
        }
        viewModelScope.launch(dispatchers.main) {
            observeFavorites().collect { ids ->
                favoriteIds = ids
                render()
            }
        }
        load(forceRefresh = false)
    }

    override fun onAction(action: MenuAction) {
        when (action) {
            MenuAction.Refresh -> {
                updateState { copy(isRefreshing = true) }
                load(forceRefresh = true)
            }
            MenuAction.Retry -> {
                updateState { copy(status = MenuStatus.Loading) }
                load(forceRefresh = true)
            }
            is MenuAction.SelectCategory -> updateAndRender { copy(category = action.category) }
            MenuAction.ToggleFavoritesOnly -> updateAndRender { copy(favoritesOnly = !favoritesOnly) }
            is MenuAction.ChangeSort -> updateAndRender { copy(sort = action.sort) }
            is MenuAction.Search -> {
                updateState { copy(query = action.query) }
                searchQuery.value = action.query
            }
            is MenuAction.ToggleFavorite -> toggle(action.id)
            is MenuAction.OpenDrink -> sendEvent(MenuEvent.NavigateToDrink(action.id))
        }
    }

    private fun load(forceRefresh: Boolean) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch(dispatchers.main) {
            getMenu(forceRefresh)
                .onSuccess { result ->
                    snapshot = result
                    render()
                }
                .onFailure { error ->
                    if (snapshot != null) {
                        updateState { copy(isRefreshing = false) }
                        sendEvent(MenuEvent.ShowMessage(MenuMessage.REFRESH_FAILED))
                    } else {
                        updateState { mapper.mapError(this, error) }
                    }
                }
        }
    }

    private fun toggle(id: DrinkId) {
        val isFavoriteNow = currentState.items.firstOrNull { it.id == id }?.isFavorite ?: return
        viewModelScope.launch(dispatchers.main) {
            toggleFavorite(id, isFavoriteNow).onFailure {
                sendEvent(MenuEvent.ShowMessage(MenuMessage.FAVORITE_UPDATE_FAILED))
            }
        }
    }

    private fun updateAndRender(reducer: MenuState.() -> MenuState) {
        updateState(reducer)
        render()
    }

    private fun render() {
        val menu = snapshot ?: return
        val drinks = favoriteIds?.let { menu.drinks.withFavorites(it) } ?: menu.drinks
        val allItems = applyHappyHour(drinks)
        updateState {
            val filter = MenuFilter(
                category = category.category,
                favoritesOnly = favoritesOnly,
                query = appliedQuery,
                sort = sort,
            )
            mapper.mapContent(
                state = this,
                allItems = allItems,
                visibleItems = filterAndSort(allItems, filter),
                isStale = menu.isStale,
            )
        }
    }

    companion object {
        const val SEARCH_DEBOUNCE_MS: Long = 300L
    }
}
