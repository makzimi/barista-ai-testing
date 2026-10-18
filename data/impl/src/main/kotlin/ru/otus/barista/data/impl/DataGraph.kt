package ru.otus.barista.data.impl

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import retrofit2.Retrofit
import ru.otus.barista.core.common.DispatcherProvider
import ru.otus.barista.data.impl.local.DataStoreFavoritesLocalDataSource
import ru.otus.barista.data.impl.local.InMemoryMenuCache
import ru.otus.barista.data.impl.remote.MenuApi
import ru.otus.barista.data.impl.repository.FavoritesRepositoryImpl
import ru.otus.barista.data.impl.repository.MenuRepositoryImpl
import ru.otus.barista.data.repository.FavoritesRepository
import ru.otus.barista.data.repository.MenuRepository

/**
 * The only public class of `:data:impl`: builds the data layer from the app's [retrofit] (base URL, OkHttp
 * interceptors) and [dataStore], and hands out the repository interfaces of `:data:api`.
 */
class DataGraph(
    retrofit: Retrofit,
    dataStore: DataStore<Preferences>,
    dispatchers: DispatcherProvider,
) {
    val menuRepository: MenuRepository = MenuRepositoryImpl(
        api = retrofit.create(MenuApi::class.java),
        cache = InMemoryMenuCache(),
        dispatchers = dispatchers,
    )

    val favoritesRepository: FavoritesRepository = FavoritesRepositoryImpl(
        local = DataStoreFavoritesLocalDataSource(dataStore),
        dispatchers = dispatchers,
    )
}
