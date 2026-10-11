package ru.otus.barista.feature.drinkdetails.domain

import ru.otus.barista.core.testing.Create
import ru.otus.barista.feature.drinkdetails.api.Size
import kotlin.test.Test
import kotlin.test.assertEquals

class ChangeAddOnQuantityUseCaseTest {

    private val sut = ChangeAddOnQuantityUseCase()

    private val details = DrinkDetails(
        drink = Create.drink(allowedAddOns = listOf(Create.EXTRA_SHOT, Create.SYRUP)),
        discountPercent = null,
    )

    @Test
    fun `WHEN add-on is not selected and delta is 1 SHOULD set quantity 1`() {
        val configuration = DrinkConfiguration(size = Size.M)
        val expected = DrinkConfiguration(size = Size.M, addOns = mapOf(Create.SYRUP.id to 1))

        val result = sut(details, configuration, Create.SYRUP.id, delta = 1)

        assertEquals(expected, result)
    }

    @Test
    fun `WHEN quantity is 0 and delta is -1 SHOULD keep the add-on unselected`() {
        val configuration = DrinkConfiguration(addOns = mapOf(Create.EXTRA_SHOT.id to 1))

        val result = sut(details, configuration, Create.SYRUP.id, delta = -1)

        assertEquals(configuration, result)
    }

    @Test
    fun `WHEN quantity is 1 and delta is -1 SHOULD remove the add-on`() {
        val configuration = DrinkConfiguration(addOns = mapOf(Create.SYRUP.id to 1))
        val expected = DrinkConfiguration(addOns = emptyMap())

        val result = sut(details, configuration, Create.SYRUP.id, delta = -1)

        assertEquals(expected, result)
    }

    @Test
    fun `WHEN quantity is at the limit of 3 and delta is 1 SHOULD keep 3`() {
        val configuration = DrinkConfiguration(addOns = mapOf(Create.SYRUP.id to 3))

        val result = sut(details, configuration, Create.SYRUP.id, delta = 1)

        assertEquals(configuration, result)
    }

    @Test
    fun `WHEN quantity is 2 and delta is 1 SHOULD reach the limit of 3`() {
        val configuration = DrinkConfiguration(addOns = mapOf(Create.SYRUP.id to 2))
        val expected = DrinkConfiguration(addOns = mapOf(Create.SYRUP.id to DrinkConfiguration.MAX_ADD_ON_QUANTITY))

        val result = sut(details, configuration, Create.SYRUP.id, delta = 1)

        assertEquals(expected, result)
    }

    @Test
    fun `WHEN add-on is not allowed for the drink SHOULD return the configuration unchanged`() {
        val configuration = DrinkConfiguration(addOns = mapOf(Create.SYRUP.id to 1))

        val result = sut(details, configuration, Create.OAT_MILK.id, delta = 1)

        assertEquals(configuration, result)
    }
}
