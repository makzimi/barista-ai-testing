package ru.otus.barista.feature.drinkdetails.presentation

import ru.otus.barista.core.designsystem.R
import ru.otus.barista.core.testing.Create
import ru.otus.barista.domain.model.Drink
import ru.otus.barista.domain.model.DrinkConfiguration
import ru.otus.barista.domain.model.DrinkDetails
import ru.otus.barista.domain.model.MenuError
import ru.otus.barista.domain.model.PriceBreakdown
import kotlin.test.Test
import kotlin.test.assertEquals

/** Which illustrations the mapper puts into State: the composables only render the `@DrawableRes` ids. */
class DrinkDetailsStateMapperTest {

    private val mapper = DrinkDetailsStateMapper()

    @Test
    fun `mapContent WHEN the drink id has an illustration SHOULD put that illustration into imageRes`() {
        val drink = Create.ESPRESSO

        val state = mapContent(drink)

        assertEquals(R.drawable.drink_espresso, state.imageRes)
    }

    @Test
    fun `mapContent WHEN the drink id is unknown SHOULD fall back to the cappuccino illustration`() {
        val drink = Create.drink(id = "unicorn_frappe")

        val state = mapContent(drink)

        assertEquals(R.drawable.drink_cappuccino, state.imageRes)
    }

    @Test
    fun `mapContent WHEN the drink has syrup, oat milk and whipped cream SHOULD give every add-on row its icon`() {
        val drink = Create.drink(allowedAddOns = listOf(Create.SYRUP, Create.OAT_MILK, Create.WHIPPED_CREAM))
        val expected = listOf(R.drawable.addon_syrup, R.drawable.addon_oat_milk, R.drawable.addon_whipped_cream)

        val state = mapContent(drink)

        assertEquals(expected, state.addOns.map { it.imageRes })
    }

    @Test
    fun `mapError WHEN the drink is not found SHOULD show NOT_FOUND with the error illustration`() {
        val expected = DrinkDetailsStatus.Error(DrinkDetailsErrorKind.NOT_FOUND, R.drawable.state_error)

        val state = mapper.mapError(DrinkDetailsState(), MenuError.NotFound(Create.drinkId()))

        assertEquals(expected, state.status)
    }

    private fun mapContent(drink: Drink): DrinkDetailsState = mapper.mapContent(
        details = DrinkDetails(drink = drink, discountPercent = null),
        configuration = DrinkConfiguration(),
        price = PriceBreakdown(subtotal = drink.basePrice, discountPercent = 0, total = drink.basePrice),
        isFavorite = false,
    )
}
