package ru.otus.barista.domain.impl.mapper

import ru.otus.barista.core.common.Money
import ru.otus.barista.data.model.AddOnDto
import ru.otus.barista.data.model.DataError
import ru.otus.barista.data.model.DrinkDto
import ru.otus.barista.data.model.HappyHourDto
import ru.otus.barista.data.model.MenuSnapshotData
import ru.otus.barista.domain.model.AddOn
import ru.otus.barista.domain.model.AddOnId
import ru.otus.barista.domain.model.Category
import ru.otus.barista.domain.model.Drink
import ru.otus.barista.domain.model.DrinkId
import ru.otus.barista.domain.model.HappyHour
import ru.otus.barista.domain.model.MenuError
import ru.otus.barista.domain.model.MenuSnapshot
import java.math.BigDecimal
import java.time.LocalTime

/**
 * The data → domain boundary: backend DTOs become domain models, data errors become domain errors.
 * Favorites are local data, merged here on every read (rule 8).
 */
class DrinkMapper {

    /** Drinks with a category this app version does not know are skipped instead of failing the whole menu. */
    fun mapSnapshot(data: MenuSnapshotData, favoriteIds: Set<String>): MenuSnapshot = MenuSnapshot(
        drinks = data.drinks.mapNotNull { mapDrink(it, favoriteIds) },
        isStale = data.isStale,
    )

    /** `null` when the backend sent a category this app version does not know. */
    fun mapDrink(dto: DrinkDto, favoriteIds: Set<String>): Drink? {
        val category = Category.entries.firstOrNull { it.name.equals(dto.category, ignoreCase = true) } ?: return null
        return Drink(
            id = DrinkId(dto.id),
            name = dto.name,
            description = dto.description,
            category = category,
            basePrice = dto.price.toMoney(),
            caffeineMg = dto.caffeineMg,
            calories = dto.calories,
            isNew = dto.isNew,
            isSoldOut = dto.isSoldOut,
            allowedAddOns = dto.addOns.map { it.toDomain() },
            happyHour = dto.happyHour?.toDomain(),
            isFavorite = dto.id in favoriteIds,
        )
    }

    /** [DataError]s become [MenuError]s; anything else (a bug, not a data failure) is passed through unchanged. */
    fun mapError(error: Throwable): Throwable = when (error) {
        is DataError.Network -> MenuError.Network(error.cause)
        is DataError.NotFound -> MenuError.NotFound(DrinkId(error.id))
        else -> error
    }

    private fun AddOnDto.toDomain(): AddOn = AddOn(id = AddOnId(id), name = name, price = price.toMoney())

    private fun HappyHourDto.toDomain(): HappyHour = HappyHour(
        from = LocalTime.parse(from),
        to = LocalTime.parse(to),
        percent = percent,
    )

    // BigDecimal.valueOf(Double) goes through Double.toString, so 189.9 becomes exactly 189.9 (not 189.8999…).
    private fun Double.toMoney(): Money = Money.of(BigDecimal.valueOf(this))
}
