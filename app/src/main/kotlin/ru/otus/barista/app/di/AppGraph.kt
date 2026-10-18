package ru.otus.barista.app.di

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.preferencesDataStoreFile
import ru.otus.barista.app.fakebackend.FakeBackendConfig
import ru.otus.barista.app.fakebackend.FakeBackendInterceptor
import ru.otus.barista.core.common.Clock
import ru.otus.barista.core.common.DefaultDispatcherProvider
import ru.otus.barista.core.common.DispatcherProvider
import ru.otus.barista.core.common.SystemClock
import ru.otus.barista.core.network.NetworkFactory
import ru.otus.barista.data.impl.DataGraph
import ru.otus.barista.domain.impl.DomainGraph
import ru.otus.barista.domain.usecase.ApplyHappyHourUseCase
import ru.otus.barista.domain.usecase.CalculatePriceUseCase
import ru.otus.barista.domain.usecase.ChangeAddOnQuantityUseCase
import ru.otus.barista.domain.usecase.FilterAndSortMenuUseCase
import ru.otus.barista.domain.usecase.GetDrinkDetailsUseCase
import ru.otus.barista.domain.usecase.GetMenuUseCase
import ru.otus.barista.domain.usecase.ObserveFavoritesUseCase
import ru.otus.barista.domain.usecase.ObserveIsFavoriteUseCase
import ru.otus.barista.domain.usecase.ToggleFavoriteUseCase
import ru.otus.barista.feature.drinkdetails.di.DrinkDetailsDependencies
import ru.otus.barista.feature.menu.di.MenuDependencies

/**
 * Manual DI root = [DataGraph] + [DomainGraph]: builds every singleton once and hands each feature the use cases
 * its `*Dependencies` interface asks for. The only module that sees every `impl`.
 */
class AppGraph(context: Context) : MenuDependencies, DrinkDetailsDependencies {

    private val appContext: Context = context.applicationContext

    val fakeBackendConfig: FakeBackendConfig = FakeBackendConfig()

    private val clock: Clock = SystemClock()

    override val dispatchers: DispatcherProvider = DefaultDispatcherProvider()

    private val data: DataGraph by lazy {
        val okHttpClient = NetworkFactory.okHttpClient(
            interceptors = listOf(
                FakeBackendInterceptor(
                    config = fakeBackendConfig,
                    readAsset = { path -> appContext.assets.open(path).bufferedReader().use { it.readText() } },
                ),
            ),
        )
        DataGraph(
            retrofit = NetworkFactory.retrofit(BASE_URL, okHttpClient),
            dataStore = PreferenceDataStoreFactory.create { appContext.preferencesDataStoreFile(FAVORITES_STORE) },
            dispatchers = dispatchers,
        )
    }

    private val domain: DomainGraph by lazy {
        DomainGraph(menuRepository = data.menuRepository, favoritesRepository = data.favoritesRepository, clock = clock)
    }

    override val getMenu: GetMenuUseCase get() = domain.getMenu
    override val observeFavorites: ObserveFavoritesUseCase get() = domain.observeFavorites
    override val toggleFavorite: ToggleFavoriteUseCase get() = domain.toggleFavorite
    override val applyHappyHour: ApplyHappyHourUseCase get() = domain.applyHappyHour
    override val filterAndSortMenu: FilterAndSortMenuUseCase get() = domain.filterAndSortMenu
    override val getDrinkDetails: GetDrinkDetailsUseCase get() = domain.getDrinkDetails
    override val observeIsFavorite: ObserveIsFavoriteUseCase get() = domain.observeIsFavorite
    override val calculatePrice: CalculatePriceUseCase get() = domain.calculatePrice
    override val changeAddOnQuantity: ChangeAddOnQuantityUseCase get() = domain.changeAddOnQuantity

    private companion object {
        /** Never resolved: [FakeBackendInterceptor] answers before OkHttp touches the network. */
        const val BASE_URL = "https://barista.fake/"
        const val FAVORITES_STORE = "favorites"
    }
}
