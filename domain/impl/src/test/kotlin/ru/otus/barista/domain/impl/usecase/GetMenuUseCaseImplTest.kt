package ru.otus.barista.domain.impl.usecase

import kotlinx.coroutines.test.runTest
import ru.otus.barista.core.testing.Create
import ru.otus.barista.core.testing.FakeFavoritesRepository
import ru.otus.barista.core.testing.FakeMenuRepository
import ru.otus.barista.data.model.DataError
import ru.otus.barista.domain.impl.mapper.DrinkMapper
import ru.otus.barista.domain.model.MenuError
import ru.otus.barista.domain.model.MenuSnapshot
import java.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertSame
import kotlin.test.assertTrue

/** Real use case + real [DrinkMapper] over the data-layer fakes: DTOs in, domain models out. */
class GetMenuUseCaseImplTest {

    private val menuRepository = FakeMenuRepository()
    private val favoritesRepository = FakeFavoritesRepository()
    private val sut = GetMenuUseCaseImpl(menuRepository, favoritesRepository, DrinkMapper())

    @Test
    fun `WHEN repository has the menu SHOULD return it mapped to domain drinks as a fresh snapshot`() = runTest {
        val expected = MenuSnapshot(drinks = Create.sampleMenu(), isStale = false)

        val result = sut(forceRefresh = false)

        assertEquals(expected, result.getOrThrow())
    }

    @Test
    fun `WHEN a drink is stored as favorite SHOULD return it with isFavorite true`() = runTest {
        menuRepository.setDrinks(listOf(Create.ESPRESSO_DTO, Create.MATCHA_DTO))
        favoritesRepository.favorites.value = setOf("matcha_latte")
        val expected = MenuSnapshot(
            drinks = listOf(Create.ESPRESSO, Create.MATCHA.copy(isFavorite = true)),
            isStale = false,
        )

        val result = sut(forceRefresh = false)

        assertEquals(expected, result.getOrThrow())
    }

    @Test
    fun `WHEN repository serves stale cache SHOULD propagate isStale`() = runTest {
        menuRepository.setDrinks(listOf(Create.ESPRESSO_DTO))
        menuRepository.failNextGetMenu = DataError.Network()
        menuRepository.staleOnFailure = true
        val expected = MenuSnapshot(drinks = listOf(Create.ESPRESSO), isStale = true)

        val result = sut(forceRefresh = true)

        assertEquals(expected, result.getOrThrow())
    }

    @Test
    fun `WHEN repository fails with cold cache SHOULD return MenuError Network with the original cause`() = runTest {
        val cause = IOException("backend is down")
        menuRepository.failNextGetMenu = DataError.Network(cause)

        val result = sut(forceRefresh = true)

        val error = assertIs<MenuError.Network>(result.exceptionOrNull())
        assertSame(cause, error.cause)
    }

    @Test
    fun `WHEN backend sends an unknown category SHOULD skip that drink and keep the rest`() = runTest {
        val unknown = Create.drinkDto(id = "kombucha", category = "FERMENTED")
        menuRepository.setDrinks(listOf(unknown, Create.ESPRESSO_DTO))
        val expected = MenuSnapshot(drinks = listOf(Create.ESPRESSO), isStale = false)

        val result = sut(forceRefresh = false)

        assertEquals(expected, result.getOrThrow())
    }

    @Test
    fun `WHEN force refresh is requested SHOULD pass it to the repository`() = runTest {
        val result = sut(forceRefresh = true)

        assertEquals(listOf(true), menuRepository.getMenuCalls)
        assertTrue(result.isSuccess)
    }
}
