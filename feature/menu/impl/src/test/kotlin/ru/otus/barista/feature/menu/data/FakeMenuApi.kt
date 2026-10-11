package ru.otus.barista.feature.menu.data

import ru.otus.barista.feature.menu.data.remote.MenuApi
import ru.otus.barista.feature.menu.data.remote.dto.DrinkDto
import ru.otus.barista.feature.menu.data.remote.dto.MenuResponseDto

/**
 * The outer boundary of the menu feature, faked at the Retrofit interface (Drinkit's TestBed style).
 *
 * - [menuResponse] is what `GET menu` returns (default: the real `menu.json` fixture);
 * - `GET drinks/{id}` returns the drink with that id from [menuResponse];
 * - [failNext] makes the next call (any endpoint) throw once;
 * - every call is recorded in [calls] as `"menu"` / `"drinks/<id>"`.
 */
class FakeMenuApi(
    var menuResponse: MenuResponseDto = Fixtures.menuResponse(),
) : MenuApi {

    var failNext: Throwable? = null

    val calls: MutableList<String> = mutableListOf()

    override suspend fun getMenu(): MenuResponseDto {
        record("menu")
        return menuResponse
    }

    override suspend fun getDrink(id: String): DrinkDto {
        val endpoint = "drinks/$id"
        record(endpoint)
        return menuResponse.drinks.firstOrNull { it.id == id } ?: unarranged(endpoint)
    }

    private fun record(endpoint: String) {
        calls += endpoint
        failNext?.let { error ->
            failNext = null
            throw error
        }
    }

    private fun unarranged(endpoint: String): Nothing =
        error("FakeMenuApi: $endpoint was called, but the test did not arrange a response for it")
}
