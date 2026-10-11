package ru.otus.barista.app.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import okhttp3.OkHttpClient
import ru.otus.barista.app.fakebackend.FakeBackendConfig
import ru.otus.barista.app.fakebackend.FakeBackendInterceptor
import ru.otus.barista.core.common.Clock
import ru.otus.barista.core.common.DefaultDispatcherProvider
import ru.otus.barista.core.common.DispatcherProvider
import ru.otus.barista.core.common.SystemClock
import ru.otus.barista.core.network.NetworkFactory
import ru.otus.barista.feature.drinkdetails.di.DrinkDetailsDependencies
import ru.otus.barista.feature.menu.api.MenuRepository
import ru.otus.barista.feature.menu.data.MenuRepositoryImpl
import ru.otus.barista.feature.menu.data.local.DataStoreFavoritesLocalDataSource
import ru.otus.barista.feature.menu.data.local.InMemoryMenuCache
import ru.otus.barista.feature.menu.data.remote.MenuApi
import ru.otus.barista.feature.menu.di.MenuDependencies

/** Manual DI root: builds every singleton once and hands features what their `*Dependencies` ask for. */
class AppGraph(context: Context) : MenuDependencies, DrinkDetailsDependencies {

    private val appContext: Context = context.applicationContext

    val fakeBackendConfig: FakeBackendConfig = FakeBackendConfig()

    override val clock: Clock = SystemClock()

    override val dispatchers: DispatcherProvider = DefaultDispatcherProvider()

    private val okHttpClient: OkHttpClient by lazy {
        NetworkFactory.okHttpClient(
            interceptors = listOf(
                FakeBackendInterceptor(
                    config = fakeBackendConfig,
                    readAsset = { path -> appContext.assets.open(path).bufferedReader().use { it.readText() } },
                ),
            ),
        )
    }

    private val favoritesDataStore: DataStore<Preferences> by lazy {
        PreferenceDataStoreFactory.create { appContext.preferencesDataStoreFile(FAVORITES_STORE) }
    }

    override val menuRepository: MenuRepository by lazy {
        MenuRepositoryImpl(
            api = NetworkFactory.retrofit(BASE_URL, okHttpClient).create(MenuApi::class.java),
            cache = InMemoryMenuCache(),
            favorites = DataStoreFavoritesLocalDataSource(favoritesDataStore),
            dispatchers = dispatchers,
        )
    }

    private companion object {
        /** Never resolved: [FakeBackendInterceptor] answers before OkHttp touches the network. */
        const val BASE_URL = "https://barista.fake/"
        const val FAVORITES_STORE = "favorites"
    }
}
