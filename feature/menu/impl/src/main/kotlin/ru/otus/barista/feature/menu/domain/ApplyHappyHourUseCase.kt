package ru.otus.barista.feature.menu.domain

import ru.otus.barista.core.common.Clock
import ru.otus.barista.core.common.Money
import ru.otus.barista.feature.menu.api.Drink
import java.math.BigDecimal

private val ONE_HUNDRED: BigDecimal = BigDecimal.valueOf(100)

/**
 * Rule 1 (happy hour): a drink's discount applies only while `clock.localTime()` is inside `[from, to)`.
 * Pure apart from the injected [Clock]: same clock time → same result.
 */
class ApplyHappyHourUseCase(
    private val clock: Clock,
) {
    operator fun invoke(drinks: List<Drink>): List<MenuItem> {
        val now = clock.localTime()
        return drinks.map { drink ->
            val percent = drink.happyHour?.takeIf { it.isActiveAt(now) }?.percent
            MenuItem(
                drink = drink,
                price = if (percent == null) drink.basePrice else drink.basePrice.discounted(percent),
                discountPercent = percent,
            )
        }
    }

    private fun Money.discounted(percent: Int): Money {
        val factor = (ONE_HUNDRED - BigDecimal.valueOf(percent.toLong())).divide(ONE_HUNDRED)
        return maxOf(Money.of(toBigDecimal() * factor), Money.ZERO)
    }
}
