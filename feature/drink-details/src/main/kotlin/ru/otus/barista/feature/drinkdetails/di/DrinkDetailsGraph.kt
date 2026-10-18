package ru.otus.barista.feature.drinkdetails.di

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import ru.otus.barista.domain.model.DrinkId
import ru.otus.barista.feature.drinkdetails.presentation.DrinkDetailsStateMapper
import ru.otus.barista.feature.drinkdetails.presentation.DrinkDetailsViewModel

/** Manual DI for the drink details feature. */
class DrinkDetailsGraph(private val dependencies: DrinkDetailsDependencies) {

    fun createViewModel(drinkId: DrinkId): DrinkDetailsViewModel = with(dependencies) {
        DrinkDetailsViewModel(
            drinkId = drinkId,
            getDrinkDetails = getDrinkDetails,
            observeIsFavorite = observeIsFavorite,
            toggleFavorite = toggleFavorite,
            calculatePrice = calculatePrice,
            changeAddOnQuantity = changeAddOnQuantity,
            mapper = DrinkDetailsStateMapper(),
        )
    }

    fun viewModelFactory(drinkId: DrinkId): ViewModelProvider.Factory = viewModelFactory {
        initializer { createViewModel(drinkId) }
    }
}
