package ru.otus.barista.data.impl.repository

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import ru.otus.barista.core.testing.Create
import ru.otus.barista.core.testing.TestDispatcherProvider
import ru.otus.barista.data.impl.local.InMemoryMenuCache
import ru.otus.barista.data.impl.remote.FakeMenuApi
import java.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The same repository with the backend faked at the Retrofit interface ([FakeMenuApi]) instead of HTTP:
 * no JSON, no sockets — the cache and fallback rules only. `MenuRepositoryImplTest` covers the HTTP contract.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MenuRepositoryImplWithFakeApiTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private val api = FakeMenuApi()
    private val sut = MenuRepositoryImpl(api, InMemoryMenuCache(), TestDispatcherProvider(dispatcher))

    @Test
    fun `getMenu WHEN called twice without force refresh SHOULD call the API once`() = runTest(dispatcher) {
        sut.getMenu(forceRefresh = false)

        sut.getMenu(forceRefresh = false)

        assertEquals(listOf("menu"), api.calls)
    }

    @Test
    fun `getMenu WHEN force refresh is requested on a warm cache SHOULD call the API again`() = runTest(dispatcher) {
        sut.getMenu(forceRefresh = false)

        sut.getMenu(forceRefresh = true)

        assertEquals(listOf("menu", "menu"), api.calls)
    }

    @Test
    fun `getDrink WHEN the API fails and the menu is cached SHOULD return the cached drink`() = runTest(dispatcher) {
        sut.getMenu(forceRefresh = false)
        api.failNext = IOException("backend is down")
        val expected = Create.MATCHA_DTO

        val result = sut.getDrink("matcha_latte")

        assertEquals(expected, result.getOrThrow())
    }
}
