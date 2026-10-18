package ru.otus.barista.feature.menu.presentation

import ru.otus.barista.core.designsystem.R
import ru.otus.barista.core.testing.Create
import ru.otus.barista.domain.model.Drink
import ru.otus.barista.domain.model.MenuError
import ru.otus.barista.domain.model.MenuItem
import kotlin.test.Test
import kotlin.test.assertEquals

/** Which illustration the mapper puts into State: the composables only render the `@DrawableRes` id. */
class MenuStateMapperTest {

    private val mapper = MenuStateMapper()

    @Test
    fun `mapItem WHEN the drink id has an illustration SHOULD put that illustration into imageRes`() {
        val item = menuItem(Create.MATCHA)

        val state = mapper.mapItem(item)

        assertEquals(R.drawable.drink_matcha_latte, state.imageRes)
    }

    @Test
    fun `mapItem WHEN the drink id is unknown SHOULD fall back to the cappuccino illustration`() {
        val item = menuItem(Create.drink(id = "unicorn_frappe"))

        val state = mapper.mapItem(item)

        assertEquals(R.drawable.drink_cappuccino, state.imageRes)
    }

    @Test
    fun `mapContent WHEN the backend menu is empty SHOULD show MENU_EMPTY with its illustration`() {
        val expected = MenuStatus.Empty(EmptyReason.MENU_EMPTY, R.drawable.state_menu_empty)

        val state = mapper.mapContent(MenuState(), allItems = emptyList(), visibleItems = emptyList(), isStale = false)

        assertEquals(expected, state.status)
    }

    @Test
    fun `mapContent WHEN favorites only and nothing is favorite SHOULD show NO_FAVORITES with its illustration`() {
        val expected = MenuStatus.Empty(EmptyReason.NO_FAVORITES, R.drawable.state_no_favorites)

        val state = mapper.mapContent(
            state = MenuState(favoritesOnly = true),
            allItems = listOf(menuItem(Create.ESPRESSO)),
            visibleItems = emptyList(),
            isStale = false,
        )

        assertEquals(expected, state.status)
    }

    @Test
    fun `mapContent WHEN the filter matches nothing SHOULD show NOTHING_FOUND with its illustration`() {
        val expected = MenuStatus.Empty(EmptyReason.NOTHING_FOUND, R.drawable.state_nothing_found)

        val state = mapper.mapContent(
            state = MenuState(query = "раф"),
            allItems = listOf(menuItem(Create.ESPRESSO)),
            visibleItems = emptyList(),
            isStale = false,
        )

        assertEquals(expected, state.status)
    }

    @Test
    fun `mapError WHEN the error is a network error SHOULD show NETWORK with the error illustration`() {
        val expected = MenuStatus.Error(ErrorKind.NETWORK, R.drawable.state_error)

        val state = mapper.mapError(MenuState(), MenuError.Network())

        assertEquals(expected, state.status)
    }

    private fun menuItem(drink: Drink): MenuItem =
        MenuItem(drink = drink, price = drink.basePrice, discountPercent = null)
}
