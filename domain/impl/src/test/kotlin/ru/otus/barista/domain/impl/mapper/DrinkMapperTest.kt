package ru.otus.barista.domain.impl.mapper

import ru.otus.barista.core.common.Money
import ru.otus.barista.core.testing.Create
import ru.otus.barista.data.model.DataError
import ru.otus.barista.data.model.MenuSnapshotData
import ru.otus.barista.domain.model.AddOn
import ru.otus.barista.domain.model.AddOnId
import ru.otus.barista.domain.model.DrinkId
import ru.otus.barista.domain.model.HappyHour
import ru.otus.barista.domain.model.MenuError
import ru.otus.barista.domain.model.MenuSnapshot
import java.io.IOException
import java.time.LocalTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertSame

/** The data → domain boundary: pure, no fakes needed. */
class DrinkMapperTest {

    private val sut = DrinkMapper()

    @Test
    fun `mapSnapshot WHEN given the menu json DTOs SHOULD return the sample menu`() {
        val data = MenuSnapshotData(drinks = Create.sampleMenuDto(), isStale = true)
        val expected = MenuSnapshot(drinks = Create.sampleMenu(), isStale = true)

        val snapshot = sut.mapSnapshot(data, favoriteIds = emptySet())

        assertEquals(expected, snapshot)
    }

    @Test
    fun `mapDrink WHEN the DTO has a happy-hour window SHOULD parse HH mm into local times`() {
        val dto = Create.drinkDto(happyHour = Create.happyHourDto(from = "22:00", to = "02:30", percent = 10))
        val expected = HappyHour(from = LocalTime.of(22, 0), to = LocalTime.of(2, 30), percent = 10)

        val drink = sut.mapDrink(dto, favoriteIds = emptySet())

        assertEquals(expected, drink?.happyHour)
    }

    @Test
    fun `mapDrink WHEN the DTO has add-ons SHOULD map ids names and prices in kopecks`() {
        val dto = Create.drinkDto(addOns = listOf(Create.addOnDto(id = "syrup", name = "Сироп", price = 39.9)))
        val expected = listOf(AddOn(id = AddOnId("syrup"), name = "Сироп", price = Money(3990)))

        val drink = sut.mapDrink(dto, favoriteIds = emptySet())

        assertEquals(expected, drink?.allowedAddOns)
    }

    @Test
    fun `mapDrink WHEN the DTO is sold out SHOULD keep the sold-out flag`() {
        val dto = Create.drinkDto(isSoldOut = true)

        val drink = sut.mapDrink(dto, favoriteIds = emptySet())

        assertEquals(true, drink?.isSoldOut)
    }

    @Test
    fun `mapDrink WHEN the id is in the favorites SHOULD mark the drink favorite`() {
        val dto = Create.drinkDto(id = "cappuccino")

        val drink = sut.mapDrink(dto, favoriteIds = setOf("cappuccino"))

        assertEquals(true, drink?.isFavorite)
    }

    @Test
    fun `mapDrink WHEN the category is unknown to this app version SHOULD return null`() {
        val dto = Create.drinkDto(category = "FERMENTED")

        val drink = sut.mapDrink(dto, favoriteIds = emptySet())

        assertNull(drink)
    }

    @Test
    fun `mapError WHEN data layer reports Network SHOULD return MenuError Network with the same cause`() {
        val cause = IOException("backend is down")

        val error = sut.mapError(DataError.Network(cause))

        assertSame(cause, assertIs<MenuError.Network>(error).cause)
    }

    @Test
    fun `mapError WHEN data layer reports NotFound SHOULD return MenuError NotFound with a DrinkId`() {
        val error = sut.mapError(DataError.NotFound("unknown"))

        assertEquals(DrinkId("unknown"), assertIs<MenuError.NotFound>(error).id)
    }

    @Test
    fun `mapError WHEN the failure is not a data error SHOULD pass it through unchanged`() {
        val bug = IllegalStateException("boom")

        val error = sut.mapError(bug)

        assertSame(bug, error)
    }
}
