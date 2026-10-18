package ru.otus.barista.domain.impl.usecase

import ru.otus.barista.core.common.Money
import ru.otus.barista.core.testing.Create
import ru.otus.barista.core.testing.FixedClock
import ru.otus.barista.domain.model.MenuItem
import java.time.LocalTime
import kotlin.test.Test
import kotlin.test.assertEquals

class ApplyHappyHourUseCaseImplTest {

    private val clock = FixedClock()
    private val sut = ApplyHappyHourUseCaseImpl(clock)

    @Test
    fun `WHEN time is inside the happy-hour window SHOULD discount the price`() {
        clock.time = LocalTime.of(17, 0)
        val drink = Create.drink(basePrice = Money(25000), happyHour = Create.happyHour(percent = 20))
        val expected = listOf(MenuItem(drink = drink, price = Money(20000), discountPercent = 20))

        val items = sut(listOf(drink))

        assertEquals(expected, items)
    }

    @Test
    fun `WHEN time equals the window start SHOULD discount the price`() {
        clock.time = LocalTime.of(16, 0)
        val drink = Create.drink(basePrice = Money(25000), happyHour = Create.happyHour(percent = 20))
        val expected = listOf(MenuItem(drink = drink, price = Money(20000), discountPercent = 20))

        val items = sut(listOf(drink))

        assertEquals(expected, items)
    }

    @Test
    fun `WHEN time equals the window end SHOULD keep the base price`() {
        clock.time = LocalTime.of(18, 0)
        val drink = Create.drink(basePrice = Money(25000), happyHour = Create.happyHour(percent = 20))
        val expected = listOf(MenuItem(drink = drink, price = Money(25000), discountPercent = null))

        val items = sut(listOf(drink))

        assertEquals(expected, items)
    }

    @Test
    fun `WHEN drink has no happy hour SHOULD keep the base price`() {
        clock.time = LocalTime.of(17, 0)
        val drink = Create.drink(basePrice = Money(25000), happyHour = null)
        val expected = listOf(MenuItem(drink = drink, price = Money(25000), discountPercent = null))

        val items = sut(listOf(drink))

        assertEquals(expected, items)
    }

    @Test
    fun `WHEN window wraps midnight and time is after midnight SHOULD discount the price`() {
        clock.time = LocalTime.of(1, 0)
        val happyHour = Create.happyHour(from = LocalTime.of(22, 0), to = LocalTime.of(2, 0), percent = 10)
        val drink = Create.drink(basePrice = Money(20000), happyHour = happyHour)
        val expected = listOf(MenuItem(drink = drink, price = Money(18000), discountPercent = 10))

        val items = sut(listOf(drink))

        assertEquals(expected, items)
    }

    @Test
    fun `WHEN window wraps midnight and time is in the afternoon SHOULD keep the base price`() {
        clock.time = LocalTime.of(15, 0)
        val happyHour = Create.happyHour(from = LocalTime.of(22, 0), to = LocalTime.of(2, 0), percent = 10)
        val drink = Create.drink(basePrice = Money(20000), happyHour = happyHour)
        val expected = listOf(MenuItem(drink = drink, price = Money(20000), discountPercent = null))

        val items = sut(listOf(drink))

        assertEquals(expected, items)
    }

    @Test
    fun `WHEN window start equals its end SHOULD never discount`() {
        clock.time = LocalTime.of(10, 0)
        val happyHour = Create.happyHour(from = LocalTime.of(10, 0), to = LocalTime.of(10, 0), percent = 50)
        val drink = Create.drink(basePrice = Money(20000), happyHour = happyHour)
        val expected = listOf(MenuItem(drink = drink, price = Money(20000), discountPercent = null))

        val items = sut(listOf(drink))

        assertEquals(expected, items)
    }

    @Test
    fun `WHEN discounted price has half a kopeck SHOULD round half up`() {
        clock.time = LocalTime.of(12, 0)
        val expected = listOf(MenuItem(drink = Create.CAPPUCCINO, price = Money(18692), discountPercent = 15))

        val items = sut(listOf(Create.CAPPUCCINO))

        assertEquals(expected, items)
    }
}
