package ru.otus.barista.feature.menu.data

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.SerializationException
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Rule
import ru.otus.barista.core.network.NetworkFactory
import ru.otus.barista.core.testing.Create
import ru.otus.barista.core.testing.TestDispatcherProvider
import ru.otus.barista.feature.menu.api.DrinkId
import ru.otus.barista.feature.menu.api.MenuError
import ru.otus.barista.feature.menu.api.MenuSnapshot
import ru.otus.barista.feature.menu.data.local.InMemoryMenuCache
import ru.otus.barista.feature.menu.data.remote.MenuApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

/**
 * Integration test of the data layer: real Retrofit + OkHttp + kotlinx-serialization (built by [NetworkFactory],
 * exactly as in production) against a local [MockWebServer], real [InMemoryMenuCache], fake favorites storage.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MenuRepositoryImplTest {

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

    @Test
    fun `getMenu WHEN backend returns the menu JSON SHOULD parse all drinks`() = runTest(dispatcher) {
        server.enqueue(MockResponse().setBody(Fixtures.read("menu.json")))
        val expected = MenuSnapshot(drinks = Create.sampleMenu(), isStale = false)

        val result = sut.getMenu(forceRefresh = false)

        assertEquals(expected, result.getOrThrow())
    }

    @Test
    fun `getMenu WHEN backend returns the menu SHOULD call GET menu`() = runTest(dispatcher) {
        server.enqueue(MockResponse().setBody(Fixtures.read("menu.json")))

        sut.getMenu(forceRefresh = false)

        assertEquals("/menu", server.takeRequest().path)
    }

    @Test
    fun `getMenu WHEN cache is warm and no force refresh SHOULD not call the backend again`() = runTest(dispatcher) {
        server.enqueue(MockResponse().setBody(Fixtures.read("menu.json")))
        sut.getMenu(forceRefresh = false)
        val expected = MenuSnapshot(drinks = Create.sampleMenu(), isStale = false)

        val result = sut.getMenu(forceRefresh = false)

        assertEquals(expected, result.getOrThrow())
        assertEquals(1, server.requestCount)
    }

    @Test
    fun `getMenu WHEN force refresh fails with 500 and cache is warm SHOULD return cached menu marked stale`() =
        runTest(dispatcher) {
            server.enqueue(MockResponse().setBody(Fixtures.read("menu.json")))
            server.enqueue(MockResponse().setResponseCode(500))
            sut.getMenu(forceRefresh = false)
            val expected = MenuSnapshot(drinks = Create.sampleMenu(), isStale = true)

            val result = sut.getMenu(forceRefresh = true)

            assertEquals(expected, result.getOrThrow())
        }

    @Test
    fun `getMenu WHEN backend fails with 500 and cache is cold SHOULD fail with Network error`() = runTest(dispatcher) {
        server.enqueue(MockResponse().setResponseCode(500))

        val result = sut.getMenu(forceRefresh = false)

        assertIs<MenuError.Network>(result.exceptionOrNull())
    }

    @Test
    fun `getMenu WHEN a required JSON field is renamed SHOULD fail with Network error caused by serialization`() =
        runTest(dispatcher) {
            server.enqueue(MockResponse().setBody(Fixtures.read("menu_price_renamed.json")))

            val result = sut.getMenu(forceRefresh = false)

            val error = assertIs<MenuError.Network>(result.exceptionOrNull())
            assertIs<SerializationException>(error.cause)
        }

    @Test
    fun `getMenu WHEN some drinks are favorite locally SHOULD merge isFavorite into the parsed drinks`() =
        runTest(dispatcher) {
            server.enqueue(MockResponse().setBody(Fixtures.read("menu.json")))
            val favoriteIds = setOf(Create.CAPPUCCINO.id, Create.MATCHA.id)
            favorites.favorites.value = favoriteIds
            val expected = MenuSnapshot(
                drinks = Create.sampleMenu().map { it.copy(isFavorite = it.id in favoriteIds) },
                isStale = false,
            )

            val result = sut.getMenu(forceRefresh = false)

            assertEquals(expected, result.getOrThrow())
        }

    @Test
    fun `getDrink WHEN backend returns the drink SHOULD parse it`() = runTest(dispatcher) {
        server.enqueue(MockResponse().setBody(Fixtures.read("drink_espresso.json")))

        val result = sut.getDrink(Create.ESPRESSO.id)

        assertEquals(Create.ESPRESSO, result.getOrThrow())
    }

    @Test
    fun `getDrink WHEN backend answers 404 and cache is cold SHOULD fail with NotFound for that id`() =
        runTest(dispatcher) {
            server.enqueue(MockResponse().setResponseCode(404))

            val result = sut.getDrink(DrinkId("unknown"))

            val error = assertIs<MenuError.NotFound>(result.exceptionOrNull())
            assertEquals(DrinkId("unknown"), error.id)
        }

    @Test
    fun `getDrink WHEN backend fails with 500 and menu is cached SHOULD return the cached drink`() =
        runTest(dispatcher) {
            server.enqueue(MockResponse().setBody(Fixtures.read("menu.json")))
            server.enqueue(MockResponse().setResponseCode(500))
            sut.getMenu(forceRefresh = false)

            val result = sut.getDrink(Create.FLAT_WHITE.id)

            assertEquals(Create.FLAT_WHITE, result.getOrThrow())
        }
}
