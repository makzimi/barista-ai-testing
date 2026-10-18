package ru.otus.barista.domain.model

import ru.otus.barista.core.common.Money

@JvmInline
value class DrinkId(val value: String)

enum class Category {
    COFFEE,
    TEA,
    SEASONAL,
}

/**
 * A drink as the app knows it. [isFavorite] is not sent by the server: the domain layer merges it from
 * local storage on every read (rule 8). [allowedAddOns] lists the only add-ons this drink can take (rule 6).
 */
data class Drink(
    val id: DrinkId,
    val name: String,
    val description: String,
    val category: Category,
    val basePrice: Money,
    val caffeineMg: Int,
    val calories: Int,
    val isNew: Boolean,
    val isSoldOut: Boolean,
    val allowedAddOns: List<AddOn>,
    val happyHour: HappyHour?,
    val isFavorite: Boolean,
)
