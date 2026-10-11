package ru.otus.barista.core.testing

import ru.otus.barista.core.common.Money
import ru.otus.barista.feature.menu.api.AddOn
import ru.otus.barista.feature.menu.api.AddOnId
import ru.otus.barista.feature.menu.api.Category
import ru.otus.barista.feature.menu.api.Drink
import ru.otus.barista.feature.menu.api.DrinkId
import ru.otus.barista.feature.menu.api.HappyHour
import java.time.LocalTime

/**
 * Object mother for test data: every parameter has a realistic default, override only what the test is about.
 *
 * The constants ([CAPPUCCINO], [ESPRESSO], …) and [sampleMenu] mirror `app/src/main/assets/fake-backend/menu.json`
 * exactly as `MenuRepositoryImpl` maps it (`isFavorite = false`), so they can be compared with parsed data.
 */
object Create {

    // region Add-ons (menu.json)
    val EXTRA_SHOT: AddOn = addOn(id = "extra_shot", name = "Дополнительный шот", price = Money(6000))
    val SYRUP: AddOn = addOn(id = "syrup", name = "Сироп", price = Money(4000))
    val OAT_MILK: AddOn = addOn(id = "oat_milk", name = "Овсяное молоко", price = Money(7000))
    val WHIPPED_CREAM: AddOn = addOn(id = "whipped_cream", name = "Взбитые сливки", price = Money(5000))
    // endregion

    // region Drinks (menu.json, in file order)
    val ESPRESSO: Drink = drink(
        id = "espresso",
        name = "Эспрессо",
        description = "Классический крепкий эспрессо из смеси арабики",
        basePrice = Money(11990),
        caffeineMg = 65,
        calories = 5,
        allowedAddOns = listOf(EXTRA_SHOT),
    )

    val AMERICANO: Drink = drink(
        id = "americano",
        name = "Американо",
        description = "Эспрессо, разбавленный горячей водой",
        basePrice = Money(14990),
        caffeineMg = 130,
        calories = 10,
        allowedAddOns = listOf(EXTRA_SHOT, SYRUP),
    )

    /** Happy hour 00:00–23:59 at 15 %: discounted at any [FixedClock] time except 23:59. */
    val CAPPUCCINO: Drink = drink(
        id = "cappuccino",
        name = "Капучино",
        description = "Эспрессо с молоком и плотной молочной пеной",
        basePrice = Money(21990),
        caffeineMg = 130,
        calories = 120,
        allowedAddOns = listOf(EXTRA_SHOT, SYRUP, OAT_MILK),
        happyHour = happyHour(from = LocalTime.of(0, 0), to = LocalTime.of(23, 59), percent = 15),
    )

    val LATTE: Drink = drink(
        id = "latte",
        name = "Латте",
        description = "Мягкий кофе с большим количеством молока",
        basePrice = Money(22990),
        caffeineMg = 130,
        calories = 190,
        allowedAddOns = listOf(EXTRA_SHOT, SYRUP, OAT_MILK),
    )

    /** Happy hour 16:00–18:00 at 20 %. */
    val FLAT_WHITE: Drink = drink(
        id = "flat_white",
        name = "Флэт уайт",
        description = "Двойной ристретто и бархатное молоко",
        basePrice = Money(23990),
        caffeineMg = 195,
        calories = 110,
        allowedAddOns = listOf(EXTRA_SHOT, SYRUP, OAT_MILK),
        happyHour = happyHour(from = LocalTime.of(16, 0), to = LocalTime.of(18, 0), percent = 20),
    )

    val RAF: Drink = drink(
        id = "raf",
        name = "Раф",
        description = "Эспрессо, взбитый со сливками и ванильным сахаром",
        basePrice = Money(26990),
        caffeineMg = 65,
        calories = 310,
        allowedAddOns = listOf(EXTRA_SHOT, SYRUP, WHIPPED_CREAM),
    )

    val MATCHA: Drink = drink(
        id = "matcha_latte",
        name = "Матча латте",
        description = "Японский зелёный чай матча с молоком",
        category = Category.TEA,
        basePrice = Money(25990),
        caffeineMg = 70,
        calories = 150,
        allowedAddOns = listOf(SYRUP, OAT_MILK),
    )

    val BLACK_TEA: Drink = drink(
        id = "black_tea",
        name = "Чай чёрный",
        description = "Ассам с мягким солодовым вкусом",
        category = Category.TEA,
        basePrice = Money(13990),
        caffeineMg = 45,
        calories = 0,
        allowedAddOns = listOf(SYRUP),
    )

    /** Happy hour 08:00–11:00 at 10 %. */
    val GREEN_TEA: Drink = drink(
        id = "green_tea",
        name = "Чай зелёный",
        description = "Сенча с травяными нотами",
        category = Category.TEA,
        basePrice = Money(13990),
        caffeineMg = 30,
        calories = 0,
        allowedAddOns = listOf(SYRUP),
        happyHour = happyHour(from = LocalTime.of(8, 0), to = LocalTime.of(11, 0), percent = 10),
    )

    val COCOA: Drink = drink(
        id = "cocoa",
        name = "Какао",
        description = "Горячий шоколадный напиток на молоке",
        category = Category.SEASONAL,
        basePrice = Money(18990),
        caffeineMg = 10,
        calories = 260,
        allowedAddOns = listOf(SYRUP, OAT_MILK, WHIPPED_CREAM),
    )

    val PUMPKIN_LATTE: Drink = drink(
        id = "pumpkin_latte",
        name = "Тыквенный латте",
        description = "Осенний латте с тыквенным пряным сиропом",
        category = Category.SEASONAL,
        basePrice = Money(28990),
        caffeineMg = 130,
        calories = 280,
        isNew = true,
        allowedAddOns = listOf(EXTRA_SHOT, SYRUP, OAT_MILK, WHIPPED_CREAM),
    )

    val ICE_LATTE_SOLD_OUT: Drink = drink(
        id = "iced_latte",
        name = "Айс латте",
        description = "Холодный латте со льдом",
        basePrice = Money(22990),
        caffeineMg = 130,
        calories = 160,
        isSoldOut = true,
        allowedAddOns = listOf(EXTRA_SHOT, SYRUP, OAT_MILK),
    )
    // endregion

    /** The 12 drinks of `menu.json`, in file order. */
    fun sampleMenu(): List<Drink> = listOf(
        ESPRESSO,
        AMERICANO,
        CAPPUCCINO,
        LATTE,
        FLAT_WHITE,
        RAF,
        MATCHA,
        BLACK_TEA,
        GREEN_TEA,
        COCOA,
        PUMPKIN_LATTE,
        ICE_LATTE_SOLD_OUT,
    )

    fun drinkId(value: String = "cappuccino"): DrinkId = DrinkId(value)

    fun addOn(
        id: String = "extra_shot",
        name: String = "Дополнительный шот",
        price: Money = Money(6000),
    ): AddOn = AddOn(id = AddOnId(id), name = name, price = price)

    fun happyHour(
        from: LocalTime = LocalTime.of(16, 0),
        to: LocalTime = LocalTime.of(18, 0),
        percent: Int = 20,
    ): HappyHour = HappyHour(from = from, to = to, percent = percent)

    @Suppress("LongParameterList")
    fun drink(
        id: String = "cappuccino",
        name: String = "Капучино",
        basePrice: Money = Money(35000),
        category: Category = Category.COFFEE,
        isSoldOut: Boolean = false,
        isNew: Boolean = false,
        happyHour: HappyHour? = null,
        allowedAddOns: List<AddOn> = listOf(EXTRA_SHOT, SYRUP, OAT_MILK),
        caffeineMg: Int = 80,
        calories: Int = 120,
        description: String = "Эспрессо с молоком и плотной молочной пеной",
        isFavorite: Boolean = false,
    ): Drink = Drink(
        id = DrinkId(id),
        name = name,
        description = description,
        category = category,
        basePrice = basePrice,
        caffeineMg = caffeineMg,
        calories = calories,
        isNew = isNew,
        isSoldOut = isSoldOut,
        allowedAddOns = allowedAddOns,
        happyHour = happyHour,
        isFavorite = isFavorite,
    )
}
