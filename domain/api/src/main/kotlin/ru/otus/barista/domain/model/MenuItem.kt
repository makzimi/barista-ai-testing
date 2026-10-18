package ru.otus.barista.domain.model

import ru.otus.barista.core.common.Money

/**
 * A drink as shown in the list: [price] is the size-S price after the happy-hour discount,
 * [discountPercent] is non-null only while the drink's happy hour is active.
 */
data class MenuItem(
    val drink: Drink,
    val price: Money,
    val discountPercent: Int?,
)
