package ru.otus.barista.feature.menu

import ru.otus.barista.core.common.Clock
import ru.otus.barista.core.common.DispatcherProvider
import ru.otus.barista.feature.menu.api.MenuRepository
import ru.otus.barista.feature.menu.data.MenuRepositoryImpl
import ru.otus.barista.feature.menu.data.local.FavoritesLocalDataSource
import ru.otus.barista.feature.menu.data.local.InMemoryMenuCache
import ru.otus.barista.feature.menu.data.remote.MenuApi
import ru.otus.barista.feature.menu.di.MenuDependencies

/**
 * The real menu graph with only the outer boundaries replaced:
 * [api] (network), [favorites] (disk), [clock] and [dispatchers].
 * Everything between them — repository, cache, use cases, mapper, ViewModel — is production code.
 */
class TestMenuDependencies(
    api: MenuApi,
    favorites: FavoritesLocalDataSource,
    override val clock: Clock,
    override val dispatchers: DispatcherProvider,
) : MenuDependencies {
    override val menuRepository: MenuRepository = MenuRepositoryImpl(
        api = api,
        cache = InMemoryMenuCache(),
        favorites = favorites,
        dispatchers = dispatchers,
    )
}
