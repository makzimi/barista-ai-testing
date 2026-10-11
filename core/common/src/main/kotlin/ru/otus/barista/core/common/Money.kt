package ru.otus.barista.core.common

import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Locale

/**
 * Amount of money in rubles, stored as whole kopecks (cents) to avoid floating-point errors.
 * Formatting is locale-independent on purpose: `Money(35050).format() == "350.50 ₽"` on every device.
 */
@JvmInline
value class Money(val cents: Long) : Comparable<Money> {

    operator fun plus(other: Money): Money = Money(cents + other.cents)

    operator fun minus(other: Money): Money = Money(cents - other.cents)

    operator fun times(quantity: Int): Money = Money(cents * quantity)

    override fun compareTo(other: Money): Int = cents.compareTo(other.cents)

    fun toBigDecimal(): BigDecimal = BigDecimal.valueOf(cents, CENTS_SCALE)

    fun format(): String = String.format(Locale.ROOT, "%.2f ₽", toBigDecimal())

    companion object {
        const val CENTS_SCALE: Int = 2

        val ZERO: Money = Money(0)

        /** Rounds [amount] (in rubles) half-up to whole kopecks. */
        fun of(amount: BigDecimal): Money =
            Money(amount.setScale(CENTS_SCALE, RoundingMode.HALF_UP).unscaledValue().toLong())

        /** `Money.of("189.90")`; handy for fixtures and previews. */
        fun of(amount: String): Money = of(BigDecimal(amount))
    }
}
