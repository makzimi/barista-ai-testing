package ru.otus.barista.feature.menu.di

import ru.otus.barista.core.common.Clock
import ru.otus.barista.core.common.DispatcherProvider
import ru.otus.barista.feature.menu.api.MenuRepository

/** What the menu feature needs from the outside world. Implemented by `AppGraph` (and by test graphs). */
interface MenuDependencies {
    val menuRepository: MenuRepository
    val clock: Clock
    val dispatchers: DispatcherProvider
}
