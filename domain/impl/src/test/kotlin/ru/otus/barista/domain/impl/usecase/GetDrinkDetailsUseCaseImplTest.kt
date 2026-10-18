package ru.otus.barista.domain.impl.usecase

import kotlinx.coroutines.test.runTest
import ru.otus.barista.core.testing.Create
import ru.otus.barista.core.testing.FakeFavoritesRepository
import ru.otus.barista.core.testing.FakeMenuRepository
import ru.otus.barista.core.testing.FixedClock
import ru.otus.barista.data.model.DataError
import ru.otus.barista.domain.impl.mapper.DrinkMapper
import ru.otus.barista.domain.model.DrinkDetails
import ru.otus.barista.domain.model.DrinkId
import ru.otus.barista.domain.model.MenuError
import java.time.LocalTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class GetDrinkDetailsUseCaseImplTest {

    private val menuRepository = FakeMenuRepository()
    private val favoritesRepository = FakeFavoritesRepository()
    private val clock = FixedClock(time = LocalTime.of(12, 0))
    private val sut = GetDrinkDetailsUseCaseImpl(menuRepository, favoritesRepository, DrinkMapper(), clock)

    @Test
    fun `WHEN the drink has no happy hour SHOULD return it without a discount`() = runTest {
        val expected = DrinkDetails(drink = Create.ESPRESSO, discountPercent = null)

        val result = sut(Create.ESPRESSO.id)

        assertEquals(expected, result.getOrThrow())
    }

    @Test
    fun `WHEN the clock is inside the happy-hour window SHOULD return the active discount`() = runTest {
        clock.time = LocalTime.of(17, 0)
        val expected = DrinkDetails(drink = Create.FLAT_WHITE, discountPercent = 20)

        val result = sut(Create.FLAT_WHITE.id)

        assertEquals(expected, result.getOrThrow())
    }

    @Test
    fun `WHEN the drink is stored as favorite SHOULD return it with isFavorite true`() = runTest {
        favoritesRepository.favorites.value = setOf("matcha_latte")
        val expected = DrinkDetails(drink = Create.MATCHA.copy(isFavorite = true), discountPercent = null)

        val result = sut(Create.MATCHA.id)

        assertEquals(expected, result.getOrThrow())
    }

    @Test
    fun `WHEN the repository does not know the drink SHOULD fail with MenuError NotFound for that id`() = runTest {
        val result = sut(DrinkId("unknown"))

        val error = assertIs<MenuError.NotFound>(result.exceptionOrNull())
        assertEquals(DrinkId("unknown"), error.id)
    }

    @Test
    fun `WHEN the backend sends an unknown category SHOULD fail with MenuError NotFound`() = runTest {
        menuRepository.setDrinks(listOf(Create.drinkDto(id = "kombucha", category = "FERMENTED")))

        val result = sut(DrinkId("kombucha"))

        assertIs<MenuError.NotFound>(result.exceptionOrNull())
    }

    @Test
    fun `WHEN the repository fails with a network error SHOULD fail with MenuError Network`() = runTest {
        menuRepository.failNextGetDrink = DataError.Network()

        val result = sut(Create.ESPRESSO.id)

        assertIs<MenuError.Network>(result.exceptionOrNull())
    }
}
