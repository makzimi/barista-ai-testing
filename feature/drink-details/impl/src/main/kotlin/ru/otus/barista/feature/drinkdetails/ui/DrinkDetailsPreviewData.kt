package ru.otus.barista.feature.drinkdetails.ui

import kotlinx.collections.immutable.persistentListOf
import ru.otus.barista.feature.drinkdetails.api.Size
import ru.otus.barista.feature.drinkdetails.presentation.AddOnRowState
import ru.otus.barista.feature.drinkdetails.presentation.DrinkDetailsState
import ru.otus.barista.feature.drinkdetails.presentation.DrinkDetailsStatus
import ru.otus.barista.feature.menu.api.AddOnId

/** Realistic sample states shared by the drink details previews. */
internal object DrinkDetailsPreviewData {

    val extraShot = AddOnRowState(
        id = AddOnId("extra_shot"),
        name = "Дополнительный шот",
        price = "+60.00 ₽",
        quantity = 1,
        limitReached = false,
    )

    val syrup = AddOnRowState(
        id = AddOnId("syrup"),
        name = "Сироп",
        price = "+40.00 ₽",
        quantity = 3,
        limitReached = true,
    )

    val oatMilk = AddOnRowState(
        id = AddOnId("oat_milk"),
        name = "Овсяное молоко",
        price = "+70.00 ₽",
        quantity = 0,
        limitReached = false,
    )

    /** Капучино M + 1 shot + 3 syrups = 219.90 × 1.25 + 60 + 120 = 454.875 → 454.88; −15% → 386.64375 → 386.64. */
    val cappuccino = DrinkDetailsState(
        status = DrinkDetailsStatus.Content,
        name = "Капучино",
        description = "Эспрессо с молоком и плотной молочной пеной",
        caffeineMg = 130,
        calories = 120,
        selectedSize = Size.M,
        addOns = persistentListOf(extraShot, syrup, oatMilk),
        totalPrice = "386.64 ₽",
        oldTotalPrice = "454.88 ₽",
        discountPercent = 15,
        discountLabel = "−15%",
        isFavorite = true,
        canAddToCart = true,
    )

    val icedLatteSoldOut = DrinkDetailsState(
        status = DrinkDetailsStatus.Content,
        name = "Айс латте",
        description = "Холодный латте со льдом",
        caffeineMg = 130,
        calories = 160,
        isSoldOut = true,
        selectedSize = Size.S,
        addOns = persistentListOf(oatMilk.copy(quantity = 0)),
        totalPrice = "229.90 ₽",
        canAddToCart = false,
    )
}
