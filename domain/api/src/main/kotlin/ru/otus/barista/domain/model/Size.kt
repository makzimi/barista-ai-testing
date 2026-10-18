package ru.otus.barista.domain.model

import java.math.BigDecimal

/** Cup size. The base price is multiplied by [priceMultiplier] (rule 5). */
enum class Size(val priceMultiplier: BigDecimal, val volumeMl: Int) {
    S(BigDecimal("1.00"), volumeMl = 250),
    M(BigDecimal("1.25"), volumeMl = 350),
    L(BigDecimal("1.50"), volumeMl = 450),
}
