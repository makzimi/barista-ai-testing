@file:Suppress("NoUnusedImports", "UnusedImports", "UnusedPrivateProperty")

package ru.otus.barista.data.impl.workshop

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Rule
import ru.otus.barista.core.network.NetworkFactory
import ru.otus.barista.core.testing.Create
import ru.otus.barista.core.testing.TestDispatcherProvider
import ru.otus.barista.data.impl.local.InMemoryMenuCache
import ru.otus.barista.data.impl.remote.Fixtures
import ru.otus.barista.data.impl.remote.MenuApi
import ru.otus.barista.data.impl.repository.MenuRepositoryImpl
import ru.otus.barista.data.model.DataError
import ru.otus.barista.data.model.MenuSnapshotData
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

/**
 * Live-coding copy of `MenuRepositoryImplTest` (LIVE_CODING_SCRIPT Part 2.1): the HTTP boundary with a real
 * Retrofit + OkHttp + kotlinx-serialization stack against [MockWebServer]. The data layer returns DTOs;
 * favorites and DTO → domain mapping live in `:domain:impl` (`GetMenuUseCaseWorkshopTest`, step 3).
 * Uncomment one region at a time (select it, `Cmd + /`) and run it from the gutter.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MenuRepositoryImplWorkshopTest {

    @get:Rule
    val server = MockWebServer()

    private val dispatcher = UnconfinedTestDispatcher()
    private val sut by lazy {
        MenuRepositoryImpl(
            api = NetworkFactory.retrofit(server.url("/").toString()).create(MenuApi::class.java),
            cache = InMemoryMenuCache(),
            dispatchers = TestDispatcherProvider(dispatcher),
        )
    }

    // region Step 1 — MockWebServer + real Retrofit + real JSON → parsed DTOs; rename a fixture field → red
    // @Test
    // fun `getMenu WHEN backend returns the menu JSON SHOULD parse all drinks`() = runTest(dispatcher) {
    //     server.enqueue(MockResponse().setBody(Fixtures.read("menu.json")))
    //     val expected = MenuSnapshotData(drinks = Create.sampleMenuDto(), isStale = false)
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
    //         val expected = MenuSnapshotData(drinks = Create.sampleMenuDto(), isStale = true)
    //
    //         val result = sut.getMenu(forceRefresh = true)
    //
    //         assertEquals(expected, result.getOrThrow())
    //     }
    // endregion

    // region Step 3 — 500 with a cold cache → Result.failure(DataError.Network)
    // @Test
    // fun `getMenu WHEN backend fails with 500 and cache is cold SHOULD fail with Network error`() =
    //     runTest(dispatcher) {
    //         server.enqueue(MockResponse().setResponseCode(500))
    //
    //         val result = sut.getMenu(forceRefresh = false)
    //
    //         assertIs<DataError.Network>(result.exceptionOrNull())
    //     }
    // endregion
}
