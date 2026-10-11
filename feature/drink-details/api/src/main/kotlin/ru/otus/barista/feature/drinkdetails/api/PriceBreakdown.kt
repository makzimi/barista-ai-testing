package ru.otus.barista.feature.drinkdetails.api

import ru.otus.barista.core.common.Money

/**
 * Result of the price formula (rule 5).
 * [subtotal] = base × size multiplier + Σ add-on price × quantity; [total] = subtotal × (1 − discount), never below 0.
 */
data class PriceBreakdown(
    val subtotal: Money,
    val discountPercent: Int,
    val total: Money,
) {
    val hasDiscount: Boolean
        get() = discountPercent > 0
}
