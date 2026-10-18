package ru.otus.barista.domain.model

/** A drink plus the happy-hour discount that is active right now (`null` = no discount). */
data class DrinkDetails(
    val drink: Drink,
    val discountPercent: Int?,
)
