package ru.otus.barista.feature.menu.domain

import ru.otus.barista.core.common.Money
import ru.otus.barista.feature.menu.api.Drink

/**
 * A drink as shown in the list: [price] is the size-S price after the happy-hour discount,
 * [discountPercent] is non-null only while the drink's happy hour is active.
 */
data class MenuItem(
    val drink: Drink,
    val price: Money,
    val discountPercent: Int?,
)
