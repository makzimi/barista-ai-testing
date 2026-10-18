package ru.otus.barista.feature.menu.presentation

import ru.otus.barista.core.mvi.MviEvent
import ru.otus.barista.domain.model.DrinkId

/** One-shot effects. Anything the screen keeps showing belongs to [MenuState] instead. */
sealed interface MenuEvent : MviEvent {
    data class NavigateToDrink(val id: DrinkId) : MenuEvent

    data class ShowMessage(val message: MenuMessage) : MenuEvent
}

/** Snackbar messages; the UI maps them to string resources. */
enum class MenuMessage {
    FAVORITE_UPDATE_FAILED,
    REFRESH_FAILED,
}
