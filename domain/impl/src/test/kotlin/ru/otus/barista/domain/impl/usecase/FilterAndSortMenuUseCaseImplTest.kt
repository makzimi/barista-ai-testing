package ru.otus.barista.domain.impl.usecase

import ru.otus.barista.core.common.Money
import ru.otus.barista.core.testing.Create
import ru.otus.barista.domain.model.Category
import ru.otus.barista.domain.model.Drink
import ru.otus.barista.domain.model.MenuFilter
import ru.otus.barista.domain.model.MenuItem
import ru.otus.barista.domain.model.MenuSort
import kotlin.test.Test
import kotlin.test.assertEquals

class FilterAndSortMenuUseCaseImplTest {

    private val sut = FilterAndSortMenuUseCaseImpl()

    private val espresso = item(Create.ESPRESSO)
    private val cappuccino = item(Create.CAPPUCCINO)
    private val matcha = item(Create.MATCHA)
    private val cocoa = item(Create.COCOA)
    private val icedLatte = item(Create.ICE_LATTE_SOLD_OUT)

    @Test
    fun `WHEN category is TEA SHOULD keep only tea drinks`() {
        val items = listOf(espresso, matcha, cocoa)

        val result = sut(items, MenuFilter(category = Category.TEA))

        assertEquals(listOf(matcha), result)
    }

    @Test
    fun `WHEN category is null SHOULD keep every drink`() {
        val items = listOf(espresso, matcha, cocoa)
        val expected = listOf(cocoa, matcha, espresso)

        val result = sut(items, MenuFilter(category = null))

        assertEquals(expected, result)
    }

    @Test
    fun `WHEN favorites only is on SHOULD keep only favorite drinks`() {
        val favoriteCappuccino = item(Create.CAPPUCCINO.copy(isFavorite = true))
        val items = listOf(espresso, favoriteCappuccino, matcha)

        val result = sut(items, MenuFilter(favoritesOnly = true))

        assertEquals(listOf(favoriteCappuccino), result)
    }

    @Test
    fun `WHEN query matches the name in another case SHOULD keep that drink`() {
        val items = listOf(espresso, cappuccino, matcha)

        val result = sut(items, MenuFilter(query = "МАТЧА"))

        assertEquals(listOf(matcha), result)
    }

    @Test
    fun `WHEN query matches only the description SHOULD keep that drink`() {
        val items = listOf(espresso, cappuccino, matcha)

        val result = sut(items, MenuFilter(query = "арабики"))

        assertEquals(listOf(espresso), result)
    }

    @Test
    fun `WHEN query is blank SHOULD keep every drink`() {
        val items = listOf(espresso, matcha)
        val expected = listOf(matcha, espresso)

        val result = sut(items, MenuFilter(query = "   "))

        assertEquals(expected, result)
    }

    @Test
    fun `WHEN sort is by name SHOULD order alphabetically ignoring case`() {
        val lowercase = item(Create.drink(id = "lower", name = "абрикосовый раф"))
        val items = listOf(espresso, matcha, lowercase, cappuccino)
        val expected = listOf(lowercase, cappuccino, matcha, espresso)

        val result = sut(items, MenuFilter(sort = MenuSort.BY_NAME))

        assertEquals(expected, result)
    }

    @Test
    fun `WHEN sort is by price SHOULD order by displayed price ascending`() {
        val discountedCappuccino = item(Create.CAPPUCCINO, price = Money(10000))
        val items = listOf(matcha, espresso, discountedCappuccino)
        val expected = listOf(discountedCappuccino, espresso, matcha)

        val result = sut(items, MenuFilter(sort = MenuSort.BY_PRICE))

        assertEquals(expected, result)
    }

    @Test
    fun `WHEN sort is by price and prices are equal SHOULD keep the original order`() {
        val blackTea = item(Create.BLACK_TEA)
        val greenTea = item(Create.GREEN_TEA)
        val items = listOf(blackTea, greenTea)

        val result = sut(items, MenuFilter(sort = MenuSort.BY_PRICE))

        assertEquals(listOf(blackTea, greenTea), result)
    }

    @Test
    fun `WHEN a sold-out drink is the cheapest SHOULD still put it last`() {
        val cheapSoldOut = item(Create.ICE_LATTE_SOLD_OUT, price = Money(100))
        val items = listOf(cheapSoldOut, matcha, espresso)
        val expected = listOf(espresso, matcha, cheapSoldOut)

        val result = sut(items, MenuFilter(sort = MenuSort.BY_PRICE))

        assertEquals(expected, result)
    }

    @Test
    fun `WHEN sorting by name SHOULD put sold-out drinks last`() {
        val items = listOf(icedLatte, espresso, cappuccino)
        val expected = listOf(cappuccino, espresso, icedLatte)

        val result = sut(items, MenuFilter(sort = MenuSort.BY_NAME))

        assertEquals(expected, result)
    }

    private fun item(drink: Drink, price: Money = drink.basePrice) =
        MenuItem(drink = drink, price = price, discountPercent = null)
}
