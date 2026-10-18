package ru.otus.barista.data.impl.repository

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
import ru.otus.barista.data.impl.local.InMemoryMenuCache
import ru.otus.barista.data.impl.remote.Fixtures
import ru.otus.barista.data.impl.remote.MenuApi
import ru.otus.barista.data.model.DataError
import ru.otus.barista.data.model.MenuSnapshotData
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

/**
 * Integration test of the data layer: real Retrofit + OkHttp + kotlinx-serialization (built by [NetworkFactory],
 * exactly as in production) against a local [MockWebServer], real [InMemoryMenuCache].
 * Results are DTOs: mapping to domain models is the domain layer's job (`DrinkMapperTest`).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MenuRepositoryImplTest {

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

    @Test
    fun `getMenu WHEN backend returns the menu JSON SHOULD parse all drinks`() = runTest(dispatcher) {
        server.enqueue(MockResponse().setBody(Fixtures.read("menu.json")))
        val expected = MenuSnapshotData(drinks = Create.sampleMenuDto(), isStale = false)

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
        val expected = MenuSnapshotData(drinks = Create.sampleMenuDto(), isStale = false)

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
            val expected = MenuSnapshotData(drinks = Create.sampleMenuDto(), isStale = true)

            val result = sut.getMenu(forceRefresh = true)

            assertEquals(expected, result.getOrThrow())
        }

    @Test
    fun `getMenu WHEN backend fails with 500 and cache is cold SHOULD fail with Network error`() = runTest(dispatcher) {
        server.enqueue(MockResponse().setResponseCode(500))

        val result = sut.getMenu(forceRefresh = false)

        assertIs<DataError.Network>(result.exceptionOrNull())
    }

    @Test
    fun `getMenu WHEN a required JSON field is renamed SHOULD fail with Network error caused by serialization`() =
        runTest(dispatcher) {
            server.enqueue(MockResponse().setBody(Fixtures.read("menu_price_renamed.json")))

            val result = sut.getMenu(forceRefresh = false)

            val error = assertIs<DataError.Network>(result.exceptionOrNull())
            assertIs<SerializationException>(error.cause)
        }

    @Test
    fun `getDrink WHEN backend returns the drink SHOULD parse it`() = runTest(dispatcher) {
        server.enqueue(MockResponse().setBody(Fixtures.read("drink_espresso.json")))
        val expected = Create.ESPRESSO_DTO

        val result = sut.getDrink("espresso")

        assertEquals(expected, result.getOrThrow())
    }

    @Test
    fun `getDrink WHEN backend answers 404 and cache is cold SHOULD fail with NotFound for that id`() =
        runTest(dispatcher) {
            server.enqueue(MockResponse().setResponseCode(404))

            val result = sut.getDrink("unknown")

            val error = assertIs<DataError.NotFound>(result.exceptionOrNull())
            assertEquals("unknown", error.id)
        }

    @Test
    fun `getDrink WHEN backend fails with 500 and cache is cold SHOULD fail with Network error`() =
        runTest(dispatcher) {
            server.enqueue(MockResponse().setResponseCode(500))

            val result = sut.getDrink("espresso")

            assertIs<DataError.Network>(result.exceptionOrNull())
        }

    @Test
    fun `getDrink WHEN backend fails with 500 and menu is cached SHOULD return the cached drink`() =
        runTest(dispatcher) {
            server.enqueue(MockResponse().setBody(Fixtures.read("menu.json")))
            server.enqueue(MockResponse().setResponseCode(500))
            sut.getMenu(forceRefresh = false)
            val expected = Create.FLAT_WHITE_DTO

            val result = sut.getDrink("flat_white")

            assertEquals(expected, result.getOrThrow())
        }
}
