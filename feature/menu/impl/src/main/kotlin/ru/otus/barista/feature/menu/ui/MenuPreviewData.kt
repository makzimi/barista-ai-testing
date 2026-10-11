package ru.otus.barista.feature.menu.ui

import kotlinx.collections.immutable.persistentListOf
import ru.otus.barista.feature.menu.api.DrinkId
import ru.otus.barista.feature.menu.presentation.MenuItemState
import ru.otus.barista.feature.menu.presentation.MenuState
import ru.otus.barista.feature.menu.presentation.MenuStatus

/** Realistic sample states shared by the menu previews (and therefore by the preview screenshot tests). */
internal object MenuPreviewData {

    val cappuccino = MenuItemState(
        id = DrinkId("cappuccino"),
        name = "Капучино",
        description = "Эспрессо с молоком и плотной молочной пеной",
        price = "186.92 ₽",
        oldPrice = "219.90 ₽",
        discountLabel = "−15%",
        isNew = false,
        isSoldOut = false,
        isFavorite = true,
    )

    val flatWhite = MenuItemState(
        id = DrinkId("flat_white"),
        name = "Флэт уайт",
        description = "Двойной ристретто и бархатное молоко",
        price = "239.90 ₽",
        oldPrice = null,
        discountLabel = null,
        isNew = false,
        isSoldOut = false,
        isFavorite = false,
    )

    val matchaLatte = MenuItemState(
        id = DrinkId("matcha_latte"),
        name = "Матча латте",
        description = "Японский зелёный чай матча с молоком",
        price = "259.90 ₽",
        oldPrice = null,
        discountLabel = null,
        isNew = false,
        isSoldOut = false,
        isFavorite = false,
    )

    val pumpkinLatte = MenuItemState(
        id = DrinkId("pumpkin_latte"),
        name = "Тыквенный латте",
        description = "Осенний латте с тыквенным пряным сиропом",
        price = "289.90 ₽",
        oldPrice = null,
        discountLabel = null,
        isNew = true,
        isSoldOut = false,
        isFavorite = false,
    )

    val icedLatte = MenuItemState(
        id = DrinkId("iced_latte"),
        name = "Айс латте",
        description = "Холодный латте со льдом",
        price = "229.90 ₽",
        oldPrice = null,
        discountLabel = null,
        isNew = false,
        isSoldOut = true,
        isFavorite = false,
    )

    val content = MenuState(
        status = MenuStatus.Content,
        items = persistentListOf(cappuccino, matchaLatte, pumpkinLatte, flatWhite, icedLatte),
    )
}
