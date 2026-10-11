package ru.otus.barista.core.common

import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MoneyTest {

    @Test
    fun `of WHEN amount has two decimals SHOULD keep it exactly`() {
        val money = Money.of("189.90")

        assertEquals(Money(18990), money)
    }

    @Test
    fun `of WHEN the third decimal is 5 SHOULD round half up`() {
        val money = Money.of("186.915")

        assertEquals(Money(18692), money)
    }

    @Test
    fun `of WHEN the third decimal is 4 SHOULD round down`() {
        val money = Money.of("186.914")

        assertEquals(Money(18691), money)
    }

    @Test
    fun `of WHEN amount is negative with half a kopeck SHOULD round away from zero`() {
        val money = Money.of(BigDecimal("-1.005"))

        assertEquals(Money(-101), money)
    }

    @Test
    fun `of WHEN amount is a whole number SHOULD convert rubles to kopecks`() {
        val money = Money.of(BigDecimal.valueOf(350))

        assertEquals(Money(35000), money)
    }

    @Test
    fun `format WHEN amount has kopecks SHOULD print two decimals and the ruble sign`() {
        val text = Money(35050).format()

        assertEquals("350.50 ₽", text)
    }

    @Test
    fun `format WHEN amount is less than a ruble SHOULD print a leading zero`() {
        val text = Money(5).format()

        assertEquals("0.05 ₽", text)
    }

    @Test
    fun `format WHEN amount is zero SHOULD print 0_00`() {
        val text = Money.ZERO.format()

        assertEquals("0.00 ₽", text)
    }

    @Test
    fun `arithmetic WHEN adding subtracting and multiplying SHOULD work in kopecks`() {
        val result = (Money(21990) + Money(4000)) * 2 - Money(1)

        assertEquals(Money(51979), result)
    }

    @Test
    fun `compareTo WHEN amounts differ SHOULD order by kopecks`() {
        val isCheaper = Money(11990) < Money(11991)

        assertTrue(isCheaper)
    }
}
