package ru.otus.barista.app

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestDispatcher
import okhttp3.Dispatcher
import okhttp3.Interceptor
import ru.otus.barista.app.fakebackend.FakeBackendConfig
import ru.otus.barista.app.fakebackend.FakeBackendInterceptor
import ru.otus.barista.core.common.Clock
import ru.otus.barista.core.common.DispatcherProvider
import ru.otus.barista.core.network.NetworkFactory
import ru.otus.barista.core.testing.TestDispatcherProvider
import ru.otus.barista.data.impl.DataGraph
import ru.otus.barista.domain.impl.DomainGraph
import ru.otus.barista.domain.usecase.ApplyHappyHourUseCase
import ru.otus.barista.domain.usecase.FilterAndSortMenuUseCase
import ru.otus.barista.domain.usecase.GetMenuUseCase
import ru.otus.barista.domain.usecase.ObserveFavoritesUseCase
import ru.otus.barista.domain.usecase.ToggleFavoriteUseCase
import ru.otus.barista.feature.menu.di.MenuDependencies
import java.util.concurrent.AbstractExecutorService
import java.util.concurrent.TimeUnit
import kotlin.coroutines.EmptyCoroutineContext

/**
 * The production graph of the menu feature — [DataGraph] + [DomainGraph] — with only the outer boundaries replaced:
 * - the backend: the app's own [FakeBackendInterceptor] serving `assets/fake-backend/menu.json` (the test source set
 *   includes `src/main/assets` as resources), with [backend] to fail the next request;
 * - the favorites file: an in-memory `DataStore`;
 * - the clock and the dispatchers.
 * `:data:impl` stays sealed: the test hands `DataGraph` a Retrofit, exactly like `AppGraph` does.
 */
class TestMenuDependencies(
    clock: Clock,
    testDispatcher: TestDispatcher,
) : MenuDependencies {

    val backend: FakeBackendConfig = FakeBackendConfig()

    /** Paths of every HTTP request that reached the fake backend, e.g. `/menu`. */
    val requests: MutableList<String> = mutableListOf()

    override val dispatchers: DispatcherProvider = TestDispatcherProvider(testDispatcher)

    private val data: DataGraph = DataGraph(
        retrofit = NetworkFactory.retrofit(
            baseUrl = "https://barista.fake/",
            client = NetworkFactory.okHttpClient(
                interceptors = listOf(
                    Interceptor { chain -> chain.proceed(chain.request().also { requests += it.url.encodedPath }) },
                    FakeBackendInterceptor(config = backend, readAsset = ::readAsset, delayMillis = 0),
                ),
            ).newBuilder()
                // OkHttp runs async calls on its own threads; run them as test-dispatcher tasks instead,
                // so `advanceUntilIdle()` covers the whole round trip deterministically.
                .dispatcher(Dispatcher(TestDispatcherExecutorService(testDispatcher)))
                .build(),
        ),
        dataStore = InMemoryPreferencesDataStore(),
        dispatchers = dispatchers,
    )

    private val domain = DomainGraph(data.menuRepository, data.favoritesRepository, clock)

    override val getMenu: GetMenuUseCase = domain.getMenu
    override val observeFavorites: ObserveFavoritesUseCase = domain.observeFavorites
    override val toggleFavorite: ToggleFavoriteUseCase = domain.toggleFavorite
    override val applyHappyHour: ApplyHappyHourUseCase = domain.applyHappyHour
    override val filterAndSortMenu: FilterAndSortMenuUseCase = domain.filterAndSortMenu

    /** What is stored on "disk" right now. */
    suspend fun storedFavoriteIds(): Set<String> = data.favoritesRepository.observe().first()

    private fun readAsset(path: String): String =
        checkNotNull(javaClass.classLoader?.getResource(path)) { "$path is not on the test classpath" }.readText()
}

/** `DataStore<Preferences>` without a file: enough for the real `DataStoreFavoritesLocalDataSource`. */
private class InMemoryPreferencesDataStore : DataStore<Preferences> {
    private val state = MutableStateFlow(emptyPreferences())

    override val data: Flow<Preferences> = state

    override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences =
        transform(state.value).also { state.value = it }
}

/** An `ExecutorService` that schedules every command on a (test) coroutine dispatcher. */
private class TestDispatcherExecutorService(private val dispatcher: CoroutineDispatcher) : AbstractExecutorService() {
    override fun execute(command: Runnable) = dispatcher.dispatch(EmptyCoroutineContext, command)

    override fun shutdown() = Unit

    override fun shutdownNow(): MutableList<Runnable> = mutableListOf()

    override fun isShutdown(): Boolean = false

    override fun isTerminated(): Boolean = false

    override fun awaitTermination(timeout: Long, unit: TimeUnit): Boolean = true
}
