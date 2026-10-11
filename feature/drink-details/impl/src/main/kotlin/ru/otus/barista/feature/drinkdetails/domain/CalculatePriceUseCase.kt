package ru.otus.barista.feature.drinkdetails.domain

import ru.otus.barista.core.common.Money
import ru.otus.barista.feature.drinkdetails.api.PriceBreakdown
import java.math.BigDecimal

private val ONE_HUNDRED: BigDecimal = BigDecimal.valueOf(100)

/**
 * Rule 5, pure: `price = base × sizeMultiplier + Σ(addOn.price × qty)`, then `× (1 − discount)`,
 * rounded half-up to kopecks once at the end, never below 0.
 *
 * Only add-ons from `drink.allowedAddOns` count; quantities are clamped to `[0, MAX_ADD_ON_QUANTITY]`;
 * a negative or `null` discount means no discount.
 */
class CalculatePriceUseCase {

    operator fun invoke(details: DrinkDetails, configuration: DrinkConfiguration): PriceBreakdown {
        val drink = details.drink
        val sizedBase = drink.basePrice.toBigDecimal() * configuration.size.priceMultiplier
        val addOnsTotal = drink.allowedAddOns.sumOf { addOn ->
            val quantity = configuration.quantityOf(addOn.id).coerceIn(0, DrinkConfiguration.MAX_ADD_ON_QUANTITY)
            addOn.price.toBigDecimal() * BigDecimal.valueOf(quantity.toLong())
        }
        val subtotal = sizedBase + addOnsTotal
        val percent = (details.discountPercent ?: 0).coerceAtLeast(0)
        val total = subtotal * (ONE_HUNDRED - BigDecimal.valueOf(percent.toLong())) / ONE_HUNDRED
        return PriceBreakdown(
            subtotal = Money.of(subtotal),
            discountPercent = percent,
            total = maxOf(Money.of(total), Money.ZERO),
        )
    }

    // Division by 100 always terminates, so plain divide() is exact; no rounding happens before Money.of().
    private operator fun BigDecimal.div(other: BigDecimal): BigDecimal = divide(other)
}
