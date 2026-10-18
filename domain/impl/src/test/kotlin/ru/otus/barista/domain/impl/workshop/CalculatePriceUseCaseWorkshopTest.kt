@file:Suppress("NoUnusedImports", "UnusedImports", "UnusedPrivateProperty")

package ru.otus.barista.domain.impl.workshop

import ru.otus.barista.core.common.Money
import ru.otus.barista.core.testing.Create
import ru.otus.barista.domain.impl.usecase.CalculatePriceUseCaseImpl
import ru.otus.barista.domain.model.DrinkConfiguration
import ru.otus.barista.domain.model.DrinkDetails
import ru.otus.barista.domain.model.PriceBreakdown
import ru.otus.barista.domain.model.Size
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Live-coding copy of `CalculatePriceUseCaseImplTest` (LIVE_CODING_SCRIPT Part 1.1).
 * Uncomment one region at a time (select it, `Cmd + /`) and run it from the gutter.
 */
class CalculatePriceUseCaseWorkshopTest {

    private val sut = CalculatePriceUseCaseImpl()

    // region Step 1 — base price for size S, no doubles
    // @Test
    // fun `WHEN size S and no add-ons SHOULD return the base price`() {
    //     val details = DrinkDetails(drink = Create.drink(basePrice = Money(30000)), discountPercent = null)
    //     val expected = PriceBreakdown(subtotal = Money(30000), discountPercent = 0, total = Money(30000))
    //
    //     val price = sut(details, DrinkConfiguration(size = Size.S))
    //
    //     assertEquals(expected, price)
    // }
    // endregion

    // region Step 2 — the formula: size L multiplies the base, add-ons are added on top
    // @Test
    // fun `WHEN size L and two syrups SHOULD add syrups to the multiplied base price`() {
    //     val details = DrinkDetails(
    //         drink = Create.drink(basePrice = Money(30000), allowedAddOns = listOf(Create.SYRUP)),
    //         discountPercent = null,
    //     )
    //     val configuration = DrinkConfiguration(size = Size.L, addOns = mapOf(Create.SYRUP.id to 2))
    //     val expected = PriceBreakdown(subtotal = Money(53000), discountPercent = 0, total = Money(53000))
    //
    //     val price = sut(details, configuration)
    //
    //     assertEquals(expected, price)
    // }
    // endregion

    // region Step 3 — happy-hour discount after add-ons, HALF_UP rounding once at the end
    // @Test
    // fun `WHEN happy hour is active SHOULD apply the discount after add-ons`() {
    //     val details = DrinkDetails(
    //         drink = Create.drink(basePrice = Money(20000), allowedAddOns = listOf(Create.EXTRA_SHOT)),
    //         discountPercent = 15,
    //     )
    //     val configuration = DrinkConfiguration(size = Size.M, addOns = mapOf(Create.EXTRA_SHOT.id to 1))
    //     val expected = PriceBreakdown(subtotal = Money(31000), discountPercent = 15, total = Money(26350))
    //
    //     val price = sut(details, configuration)
    //
    //     assertEquals(expected, price)
    // }
    //
    // @Test
    // fun `WHEN the discounted total ends with half a kopeck SHOULD round half up`() {
    //     val details = DrinkDetails(drink = Create.drink(basePrice = Money(21990)), discountPercent = 15)
    //     val expected = PriceBreakdown(subtotal = Money(21990), discountPercent = 15, total = Money(18692))
    //
    //     val price = sut(details, DrinkConfiguration(size = Size.S))
    //
    //     assertEquals(expected, price)
    // }
    // endregion

    // region Step 4 — total never below zero, for every size
    // @Test
    // fun `WHEN discount is above 100 percent SHOULD return zero total for every size`() {
    //     val details = DrinkDetails(drink = Create.drink(basePrice = Money(30000)), discountPercent = 150)
    //     val expected = listOf(Money.ZERO, Money.ZERO, Money.ZERO)
    //
    //     val totals = Size.entries.map { size -> sut(details, DrinkConfiguration(size = size)).total }
    //
    //     assertEquals(expected, totals)
    // }
    // endregion
}
