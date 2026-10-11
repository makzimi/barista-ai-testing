package ru.otus.barista.feature.drinkdetails.presentation

import ru.otus.barista.core.mvi.MviAction
import ru.otus.barista.feature.drinkdetails.api.Size
import ru.otus.barista.feature.menu.api.AddOnId

sealed interface DrinkDetailsAction : MviAction {
    data class SelectSize(val size: Size) : DrinkDetailsAction

    data class IncrementAddOn(val id: AddOnId) : DrinkDetailsAction

    data class DecrementAddOn(val id: AddOnId) : DrinkDetailsAction

    data object ToggleFavorite : DrinkDetailsAction

    data object AddToCart : DrinkDetailsAction

    data object Retry : DrinkDetailsAction

    data object Back : DrinkDetailsAction
}
