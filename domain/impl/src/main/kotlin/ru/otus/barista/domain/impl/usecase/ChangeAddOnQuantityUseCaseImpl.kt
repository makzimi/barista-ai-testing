package ru.otus.barista.domain.impl.usecase

import ru.otus.barista.domain.model.AddOnId
import ru.otus.barista.domain.model.DrinkConfiguration
import ru.otus.barista.domain.model.DrinkDetails
import ru.otus.barista.domain.usecase.ChangeAddOnQuantityUseCase

/**
 * Rule 6, pure: changes the quantity of [AddOnId] by `delta`, keeping it in `[0, MAX_ADD_ON_QUANTITY]`.
 * Going beyond the limit is ignored; add-ons the drink does not allow are ignored entirely.
 */
class ChangeAddOnQuantityUseCaseImpl : ChangeAddOnQuantityUseCase {

    override operator fun invoke(
        details: DrinkDetails,
        configuration: DrinkConfiguration,
        addOnId: AddOnId,
        delta: Int,
    ): DrinkConfiguration {
        if (details.drink.allowedAddOns.none { it.id == addOnId }) return configuration
        val quantity = (configuration.quantityOf(addOnId) + delta).coerceIn(0, DrinkConfiguration.MAX_ADD_ON_QUANTITY)
        val addOns = if (quantity == 0) configuration.addOns - addOnId else configuration.addOns + (addOnId to quantity)
        return configuration.copy(addOns = addOns)
    }
}
