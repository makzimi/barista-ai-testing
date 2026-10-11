package ru.otus.barista.feature.drinkdetails.domain

import ru.otus.barista.feature.menu.api.Drink

/** A drink plus the happy-hour discount that is active right now (`null` = no discount). */
data class DrinkDetails(
    val drink: Drink,
    val discountPercent: Int?,
)
