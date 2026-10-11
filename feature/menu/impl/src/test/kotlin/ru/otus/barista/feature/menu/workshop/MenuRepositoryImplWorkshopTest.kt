@file:Suppress("NoUnusedImports", "UnusedImports", "UnusedPrivateProperty")

package ru.otus.barista.feature.menu.workshop

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Rule
import ru.otus.barista.core.network.NetworkFactory
import ru.otus.barista.core.testing.Create
import ru.otus.barista.core.testing.TestDispatcherProvider
import ru.otus.barista.feature.menu.api.MenuError
import ru.otus.barista.feature.menu.api.MenuSnapshot
import ru.otus.barista.feature.menu.data.FakeFavoritesLocalDataSource
import ru.otus.barista.feature.menu.data.Fixtures
import ru.otus.barista.feature.menu.data.MenuRepositoryImpl
import ru.otus.barista.feature.menu.data.local.InMemoryMenuCache
import ru.otus.barista.feature.menu.data.remote.MenuApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

/**
 * Live-coding copy of `MenuRepositoryImplTest` (LIVE_CODING_SCRIPT Part 2.1): the HTTP boundary with a real
 * Retrofit + OkHttp + kotlinx-serialization stack against [MockWebServer].
 * Uncomment one region at a time (select it, `Cmd + /`) and run it from the gutter.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MenuRepositoryImplWorkshopTest {

    @get:Rule
    val server = MockWebServer()

    private val dispatcher = UnconfinedTestDispatcher()
    private val favorites = FakeFavoritesLocalDataSource()
    private val sut by lazy {
        MenuRepositoryImpl(
            api = NetworkFactory.retrofit(server.url("/").toString()).create(MenuApi::class.java),
            cache = InMemoryMenuCache(),
            favorites = favorites,
            dispatchers = TestDispatcherProvider(dispatcher),
        )
    }

    // region Step 1 — MockWebServer + real Retrofit + real JSON → parsed drinks; rename a fixture field → red
    // @Test
    // fun `getMenu WHEN backend returns the menu JSON SHOULD parse all drinks`() = runTest(dispatcher) {
    //     server.enqueue(MockResponse().setBody(Fixtures.read("menu.json")))
    //     val expected = MenuSnapshot(drinks = Create.sampleMenu(), isStale = false)
    //
    //     val result = sut.getMenu(forceRefresh = false)
    //
    //     assertEquals(expected, result.getOrThrow())
    // }
    // endregion

    // region Step 2 — 500 with a warm cache → cached menu, isStale = true
    // @Test
    // fun `getMenu WHEN force refresh fails with 500 and cache is warm SHOULD return cached menu marked stale`() =
    //     runTest(dispatcher) {
    //         server.enqueue(MockResponse().setBody(Fixtures.read("menu.json")))
    //         server.enqueue(MockResponse().setResponseCode(500))
    //         sut.getMenu(forceRefresh = false)
    //         val expected = MenuSnapshot(drinks = Create.sampleMenu(), isStale = true)
    //
    //         val result = sut.getMenu(forceRefresh = true)
    //
    //         assertEquals(expected, result.getOrThrow())
    //     }
    // endregion

    // region Step 3 — 500 with a cold cache → Result.failure(MenuError.Network)
    // @Test
    // fun `getMenu WHEN backend fails with 500 and cache is cold SHOULD fail with Network error`() =
    //     runTest(dispatcher) {
    //         server.enqueue(MockResponse().setResponseCode(500))
    //
    //         val result = sut.getMenu(forceRefresh = false)
    //
    //         assertIs<MenuError.Network>(result.exceptionOrNull())
    //     }
    // endregion

    // region Step 4 — favorites from the (fake) local source are merged into the parsed drinks
    // @Test
    // fun `getMenu WHEN some drinks are favorite locally SHOULD merge isFavorite into the parsed drinks`() =
    //     runTest(dispatcher) {
    //         server.enqueue(MockResponse().setBody(Fixtures.read("menu.json")))
    //         val favoriteIds = setOf(Create.CAPPUCCINO.id, Create.MATCHA.id)
    //         favorites.favorites.value = favoriteIds
    //         val expected = MenuSnapshot(
    //             drinks = Create.sampleMenu().map { it.copy(isFavorite = it.id in favoriteIds) },
    //             isStale = false,
    //         )
    //
    //         val result = sut.getMenu(forceRefresh = false)
    //
    //         assertEquals(expected, result.getOrThrow())
    //     }
    // endregion
}
