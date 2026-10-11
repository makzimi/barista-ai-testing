package ru.otus.barista.feature.menu.data.remote

import ru.otus.barista.core.common.Money
import ru.otus.barista.feature.menu.api.AddOn
import ru.otus.barista.feature.menu.api.AddOnId
import ru.otus.barista.feature.menu.api.Category
import ru.otus.barista.feature.menu.api.Drink
import ru.otus.barista.feature.menu.api.DrinkId
import ru.otus.barista.feature.menu.api.HappyHour
import ru.otus.barista.feature.menu.data.remote.dto.AddOnDto
import ru.otus.barista.feature.menu.data.remote.dto.DrinkDto
import ru.otus.barista.feature.menu.data.remote.dto.HappyHourDto
import ru.otus.barista.feature.menu.data.remote.dto.MenuResponseDto
import java.math.BigDecimal
import java.time.LocalTime

/** Drinks with a category this app version does not know are skipped instead of failing the whole menu. */
fun MenuResponseDto.toDomain(): List<Drink> = drinks.mapNotNull { it.toDomainOrNull() }

/** `isFavorite` is always `false` here: favorites are local data merged by the repository. */
fun DrinkDto.toDomainOrNull(): Drink? {
    val category = Category.entries.firstOrNull { it.name.equals(category, ignoreCase = true) } ?: return null
    return Drink(
        id = DrinkId(id),
        name = name,
        description = description,
        category = category,
        basePrice = price.toMoney(),
        caffeineMg = caffeineMg,
        calories = calories,
        isNew = isNew,
        isSoldOut = isSoldOut,
        allowedAddOns = addOns.map { it.toDomain() },
        happyHour = happyHour?.toDomain(),
        isFavorite = false,
    )
}

fun AddOnDto.toDomain(): AddOn = AddOn(id = AddOnId(id), name = name, price = price.toMoney())

fun HappyHourDto.toDomain(): HappyHour = HappyHour(
    from = LocalTime.parse(from),
    to = LocalTime.parse(to),
    percent = percent,
)

// BigDecimal.valueOf(Double) goes through Double.toString, so 189.9 becomes exactly 189.9 (not 189.8999…).
private fun Double.toMoney(): Money = Money.of(BigDecimal.valueOf(this))
