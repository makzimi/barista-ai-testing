package ru.otus.barista.core.testing

import ru.otus.barista.core.common.Money
import ru.otus.barista.feature.menu.api.AddOn
import ru.otus.barista.feature.menu.api.AddOnId
import ru.otus.barista.feature.menu.api.Category
import ru.otus.barista.feature.menu.api.Drink
import ru.otus.barista.feature.menu.api.DrinkId
import ru.otus.barista.feature.menu.api.HappyHour
import java.time.LocalTime

/** Object mother for test data: every parameter has a realistic default, override only what the test is about. */
object Create {

    fun drinkId(value: String = "cappuccino"): DrinkId = DrinkId(value)

    fun addOn(
        id: String = "extra_shot",
        name: String = "Дополнительный шот",
        price: String = "60.00",
    ): AddOn = AddOn(id = AddOnId(id), name = name, price = Money.of(price))

    fun happyHour(
        from: LocalTime = LocalTime.of(16, 0),
        to: LocalTime = LocalTime.of(18, 0),
        percent: Int = 20,
    ): HappyHour = HappyHour(from = from, to = to, percent = percent)

    @Suppress("LongParameterList")
    fun drink(
        id: String = "cappuccino",
        name: String = "Капучино",
        description: String = "Эспрессо с молоком и плотной молочной пеной",
        category: Category = Category.COFFEE,
        basePrice: String = "219.90",
        caffeineMg: Int = 130,
        calories: Int = 120,
        isNew: Boolean = false,
        isSoldOut: Boolean = false,
        allowedAddOns: List<AddOn> = listOf(addOn()),
        happyHour: HappyHour? = null,
        isFavorite: Boolean = false,
    ): Drink = Drink(
        id = DrinkId(id),
        name = name,
        description = description,
        category = category,
        basePrice = Money.of(basePrice),
        caffeineMg = caffeineMg,
        calories = calories,
        isNew = isNew,
        isSoldOut = isSoldOut,
        allowedAddOns = allowedAddOns,
        happyHour = happyHour,
        isFavorite = isFavorite,
    )
}
