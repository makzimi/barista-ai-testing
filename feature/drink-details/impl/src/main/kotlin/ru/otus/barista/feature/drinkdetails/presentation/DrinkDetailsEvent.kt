package ru.otus.barista.feature.drinkdetails.presentation

import ru.otus.barista.core.mvi.MviEvent

sealed interface DrinkDetailsEvent : MviEvent {
    /** Shown as a snackbar; the cart itself is out of scope. */
    data class AddedToCart(val drinkName: String) : DrinkDetailsEvent

    data object NavigateBack : DrinkDetailsEvent

    data class ShowMessage(val message: DrinkDetailsMessage) : DrinkDetailsEvent
}

enum class DrinkDetailsMessage {
    FAVORITE_UPDATE_FAILED,
}
