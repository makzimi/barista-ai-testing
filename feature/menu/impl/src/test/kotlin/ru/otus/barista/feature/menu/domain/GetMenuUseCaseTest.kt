package ru.otus.barista.feature.menu.domain

import kotlinx.coroutines.test.runTest
import ru.otus.barista.core.testing.Create
import ru.otus.barista.core.testing.FakeMenuRepository
import ru.otus.barista.feature.menu.api.MenuError
import ru.otus.barista.feature.menu.api.MenuSnapshot
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class GetMenuUseCaseTest {

    private val repository = FakeMenuRepository()
    private val sut = GetMenuUseCase(repository)

    @Test
    fun `WHEN repository has drinks SHOULD return them as a fresh snapshot`() = runTest {
        repository.setDrinks(listOf(Create.ESPRESSO, Create.MATCHA))
        val expected = MenuSnapshot(drinks = listOf(Create.ESPRESSO, Create.MATCHA), isStale = false)

        val result = sut(forceRefresh = false)

        assertEquals(expected, result.getOrThrow())
    }

    @Test
    fun `WHEN a drink is stored as favorite SHOULD return it with isFavorite true`() = runTest {
        repository.setDrinks(listOf(Create.ESPRESSO, Create.MATCHA))
        repository.favorites.value = setOf(Create.MATCHA.id)
        val expected = MenuSnapshot(
            drinks = listOf(Create.ESPRESSO, Create.MATCHA.copy(isFavorite = true)),
            isStale = false,
        )

        val result = sut(forceRefresh = false)

        assertEquals(expected, result.getOrThrow())
    }

    @Test
    fun `WHEN repository serves stale cache SHOULD propagate isStale`() = runTest {
        repository.setDrinks(listOf(Create.ESPRESSO))
        repository.failNextGetMenu = MenuError.Network()
        repository.staleOnFailure = true
        val expected = MenuSnapshot(drinks = listOf(Create.ESPRESSO), isStale = true)

        val result = sut(forceRefresh = true)

        assertEquals(expected, result.getOrThrow())
    }

    @Test
    fun `WHEN repository fails with cold cache SHOULD return the Network error`() = runTest {
        repository.failNextGetMenu = MenuError.Network()

        val result = sut(forceRefresh = true)

        assertIs<MenuError.Network>(result.exceptionOrNull())
    }

    @Test
    fun `WHEN force refresh is requested SHOULD pass it to the repository`() = runTest {
        val result = sut(forceRefresh = true)

        assertEquals(listOf(true), repository.getMenuCalls)
        assertTrue(result.isSuccess)
    }
}
