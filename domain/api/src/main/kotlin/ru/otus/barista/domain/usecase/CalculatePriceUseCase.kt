package ru.otus.barista.domain.usecase

import ru.otus.barista.domain.model.DrinkConfiguration
import ru.otus.barista.domain.model.DrinkDetails
import ru.otus.barista.domain.model.PriceBreakdown

/** Rule 5: the price of a configured drink. */
interface CalculatePriceUseCase {
    operator fun invoke(details: DrinkDetails, configuration: DrinkConfiguration): PriceBreakdown
}
