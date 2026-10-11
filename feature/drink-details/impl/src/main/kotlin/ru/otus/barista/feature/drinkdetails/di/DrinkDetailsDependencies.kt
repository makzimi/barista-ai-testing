package ru.otus.barista.feature.drinkdetails.di

import ru.otus.barista.core.common.Clock
import ru.otus.barista.feature.menu.api.MenuRepository

/** What the drink details feature needs from the outside world. Implemented by `AppGraph`. */
interface DrinkDetailsDependencies {
    val menuRepository: MenuRepository
    val clock: Clock
}
