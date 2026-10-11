package ru.otus.barista.feature.drinkdetails.di

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import ru.otus.barista.feature.drinkdetails.domain.CalculatePriceUseCase
import ru.otus.barista.feature.drinkdetails.domain.ChangeAddOnQuantityUseCase
import ru.otus.barista.feature.drinkdetails.domain.GetDrinkDetailsUseCase
import ru.otus.barista.feature.drinkdetails.domain.ObserveIsFavoriteUseCase
import ru.otus.barista.feature.drinkdetails.domain.ToggleFavoriteUseCase
import ru.otus.barista.feature.drinkdetails.presentation.DrinkDetailsStateMapper
import ru.otus.barista.feature.drinkdetails.presentation.DrinkDetailsViewModel
import ru.otus.barista.feature.menu.api.DrinkId

/** Manual DI for the drink details feature. */
class DrinkDetailsGraph(private val dependencies: DrinkDetailsDependencies) {

    fun createViewModel(drinkId: DrinkId): DrinkDetailsViewModel = with(dependencies) {
        DrinkDetailsViewModel(
            drinkId = drinkId,
            getDrinkDetails = GetDrinkDetailsUseCase(menuRepository, clock),
            observeIsFavorite = ObserveIsFavoriteUseCase(menuRepository),
            toggleFavorite = ToggleFavoriteUseCase(menuRepository),
            calculatePrice = CalculatePriceUseCase(),
            changeAddOnQuantity = ChangeAddOnQuantityUseCase(),
            mapper = DrinkDetailsStateMapper(),
        )
    }

    fun viewModelFactory(drinkId: DrinkId): ViewModelProvider.Factory = viewModelFactory {
        initializer { createViewModel(drinkId) }
    }
}
