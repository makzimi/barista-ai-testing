package ru.otus.barista.domain.impl.usecase

import ru.otus.barista.core.common.Money
import ru.otus.barista.core.testing.Create
import ru.otus.barista.domain.model.DrinkConfiguration
import ru.otus.barista.domain.model.DrinkDetails
import ru.otus.barista.domain.model.PriceBreakdown
import ru.otus.barista.domain.model.Size
import kotlin.test.Test
import kotlin.test.assertEquals

class CalculatePriceUseCaseImplTest {

    private val sut = CalculatePriceUseCaseImpl()

    @Test
    fun `WHEN size S and no add-ons SHOULD return the base price`() {
        val details = DrinkDetails(drink = Create.drink(basePrice = Money(30000)), discountPercent = null)
        val expected = PriceBreakdown(subtotal = Money(30000), discountPercent = 0, total = Money(30000))

        val price = sut(details, DrinkConfiguration(size = Size.S))

        assertEquals(expected, price)
    }

    @Test
    fun `WHEN size M SHOULD multiply the base price by 1_25`() {
        val details = DrinkDetails(drink = Create.drink(basePrice = Money(30000)), discountPercent = null)
        val expected = PriceBreakdown(subtotal = Money(37500), discountPercent = 0, total = Money(37500))

        val price = sut(details, DrinkConfiguration(size = Size.M))

        assertEquals(expected, price)
    }

    @Test
    fun `WHEN size L and two syrups SHOULD add syrups to the multiplied base price`() {
        val details = DrinkDetails(
            drink = Create.drink(basePrice = Money(30000), allowedAddOns = listOf(Create.SYRUP)),
            discountPercent = null,
        )
        val configuration = DrinkConfiguration(size = Size.L, addOns = mapOf(Create.SYRUP.id to 2))
        val expected = PriceBreakdown(subtotal = Money(53000), discountPercent = 0, total = Money(53000))

        val price = sut(details, configuration)

        assertEquals(expected, price)
    }

    @Test
    fun `WHEN happy hour is active SHOULD apply the discount after add-ons`() {
        val details = DrinkDetails(
            drink = Create.drink(basePrice = Money(20000), allowedAddOns = listOf(Create.EXTRA_SHOT)),
            discountPercent = 15,
        )
        val configuration = DrinkConfiguration(size = Size.M, addOns = mapOf(Create.EXTRA_SHOT.id to 1))
        val expected = PriceBreakdown(subtotal = Money(31000), discountPercent = 15, total = Money(26350))

        val price = sut(details, configuration)

        assertEquals(expected, price)
    }

    @Test
    fun `WHEN the discounted total ends with half a kopeck SHOULD round half up`() {
        val details = DrinkDetails(drink = Create.drink(basePrice = Money(21990)), discountPercent = 15)
        val expected = PriceBreakdown(subtotal = Money(21990), discountPercent = 15, total = Money(18692))

        val price = sut(details, DrinkConfiguration(size = Size.S))

        assertEquals(expected, price)
    }

    @Test
    fun `WHEN the sized price has fractions of a kopeck SHOULD round only once at the end`() {
        val details = DrinkDetails(drink = Create.drink(basePrice = Money(1010)), discountPercent = 50)
        val expected = PriceBreakdown(subtotal = Money(1263), discountPercent = 50, total = Money(631))

        val price = sut(details, DrinkConfiguration(size = Size.M))

        assertEquals(expected, price)
    }

    @Test
    fun `WHEN discount is above 100 percent SHOULD return zero total`() {
        val details = DrinkDetails(drink = Create.drink(basePrice = Money(30000)), discountPercent = 150)
        val expected = PriceBreakdown(subtotal = Money(30000), discountPercent = 150, total = Money.ZERO)

        val price = sut(details, DrinkConfiguration(size = Size.S))

        assertEquals(expected, price)
    }

    @Test
    fun `WHEN discount is negative SHOULD ignore it`() {
        val details = DrinkDetails(drink = Create.drink(basePrice = Money(30000)), discountPercent = -10)
        val expected = PriceBreakdown(subtotal = Money(30000), discountPercent = 0, total = Money(30000))

        val price = sut(details, DrinkConfiguration(size = Size.S))

        assertEquals(expected, price)
    }

    @Test
    fun `WHEN add-on quantity is above the limit SHOULD count only 3`() {
        val details = DrinkDetails(
            drink = Create.drink(basePrice = Money(30000), allowedAddOns = listOf(Create.SYRUP)),
            discountPercent = null,
        )
        val configuration = DrinkConfiguration(size = Size.S, addOns = mapOf(Create.SYRUP.id to 5))
        val expected = PriceBreakdown(subtotal = Money(42000), discountPercent = 0, total = Money(42000))

        val price = sut(details, configuration)

        assertEquals(expected, price)
    }

    @Test
    fun `WHEN add-on quantity is negative SHOULD count it as 0`() {
        val details = DrinkDetails(
            drink = Create.drink(basePrice = Money(30000), allowedAddOns = listOf(Create.SYRUP)),
            discountPercent = null,
        )
        val configuration = DrinkConfiguration(size = Size.S, addOns = mapOf(Create.SYRUP.id to -2))
        val expected = PriceBreakdown(subtotal = Money(30000), discountPercent = 0, total = Money(30000))

        val price = sut(details, configuration)

        assertEquals(expected, price)
    }

    @Test
    fun `WHEN configuration has an add-on the drink does not allow SHOULD ignore it`() {
        val details = DrinkDetails(
            drink = Create.drink(basePrice = Money(11990), allowedAddOns = listOf(Create.EXTRA_SHOT)),
            discountPercent = null,
        )
        val configuration = DrinkConfiguration(size = Size.S, addOns = mapOf(Create.OAT_MILK.id to 1))
        val expected = PriceBreakdown(subtotal = Money(11990), discountPercent = 0, total = Money(11990))

        val price = sut(details, configuration)

        assertEquals(expected, price)
    }
}
