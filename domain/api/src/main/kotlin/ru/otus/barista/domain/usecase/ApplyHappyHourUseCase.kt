package ru.otus.barista.domain.usecase

import ru.otus.barista.domain.model.Drink
import ru.otus.barista.domain.model.MenuItem

/** Rule 1: list prices with the happy-hour discount that is active right now. */
interface ApplyHappyHourUseCase {
    operator fun invoke(drinks: List<Drink>): List<MenuItem>
}
