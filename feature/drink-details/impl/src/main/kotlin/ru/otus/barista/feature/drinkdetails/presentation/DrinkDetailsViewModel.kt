package ru.otus.barista.feature.drinkdetails.presentation

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.otus.barista.core.mvi.MviViewModel
import ru.otus.barista.feature.drinkdetails.domain.CalculatePriceUseCase
import ru.otus.barista.feature.drinkdetails.domain.ChangeAddOnQuantityUseCase
import ru.otus.barista.feature.drinkdetails.domain.DrinkConfiguration
import ru.otus.barista.feature.drinkdetails.domain.DrinkDetails
import ru.otus.barista.feature.drinkdetails.domain.GetDrinkDetailsUseCase
import ru.otus.barista.feature.drinkdetails.domain.ObserveIsFavoriteUseCase
import ru.otus.barista.feature.drinkdetails.domain.ToggleFavoriteUseCase
import ru.otus.barista.feature.menu.api.AddOnId
import ru.otus.barista.feature.menu.api.DrinkId

/** Loads one drink, recalculates the price on every configuration change, emits one-shot cart/navigation events. */
class DrinkDetailsViewModel(
    private val drinkId: DrinkId,
    private val getDrinkDetails: GetDrinkDetailsUseCase,
    private val observeIsFavorite: ObserveIsFavoriteUseCase,
    private val toggleFavorite: ToggleFavoriteUseCase,
    private val calculatePrice: CalculatePriceUseCase,
    private val changeAddOnQuantity: ChangeAddOnQuantityUseCase,
    private val mapper: DrinkDetailsStateMapper,
) : MviViewModel<DrinkDetailsState, DrinkDetailsAction, DrinkDetailsEvent>(DrinkDetailsState()) {

    private var details: DrinkDetails? = null
    private var configuration = DrinkConfiguration()
    private var storedFavorite: Boolean? = null

    init {
        load()
        viewModelScope.launch {
            observeIsFavorite(drinkId).collect { value ->
                storedFavorite = value
                render()
            }
        }
    }

    override fun onAction(action: DrinkDetailsAction) {
        when (action) {
            is DrinkDetailsAction.SelectSize -> changeConfiguration { copy(size = action.size) }
            is DrinkDetailsAction.IncrementAddOn -> changeAddOn(action.id, delta = 1)
            is DrinkDetailsAction.DecrementAddOn -> changeAddOn(action.id, delta = -1)
            DrinkDetailsAction.ToggleFavorite -> toggle()
            DrinkDetailsAction.AddToCart -> if (currentState.canAddToCart) {
                sendEvent(DrinkDetailsEvent.AddedToCart(currentState.name))
            }
            DrinkDetailsAction.Retry -> {
                updateState { copy(status = DrinkDetailsStatus.Loading) }
                load()
            }
            DrinkDetailsAction.Back -> sendEvent(DrinkDetailsEvent.NavigateBack)
        }
    }

    private fun load() {
        viewModelScope.launch {
            getDrinkDetails(drinkId)
                .onSuccess { loaded ->
                    details = loaded
                    render()
                }
                .onFailure { error -> updateState { mapper.mapError(this, error) } }
        }
    }

    private fun changeAddOn(id: AddOnId, delta: Int) {
        val loaded = details ?: return
        changeConfiguration { changeAddOnQuantity(loaded, this, id, delta) }
    }

    private fun changeConfiguration(transform: DrinkConfiguration.() -> DrinkConfiguration) {
        if (details == null) return
        configuration = configuration.transform()
        render()
    }

    private fun toggle() {
        if (details == null) return
        viewModelScope.launch {
            toggleFavorite(drinkId, currentState.isFavorite).onFailure {
                sendEvent(DrinkDetailsEvent.ShowMessage(DrinkDetailsMessage.FAVORITE_UPDATE_FAILED))
            }
        }
    }

    private fun render() {
        val loaded = details ?: return
        val price = calculatePrice(loaded, configuration)
        updateState {
            mapper.mapContent(
                details = loaded,
                configuration = configuration,
                price = price,
                isFavorite = storedFavorite ?: loaded.drink.isFavorite,
            )
        }
    }
}
