package ru.otus.barista.domain.usecase

import ru.otus.barista.domain.model.AddOnId
import ru.otus.barista.domain.model.DrinkConfiguration
import ru.otus.barista.domain.model.DrinkDetails

/** Rule 6: changes an add-on quantity within `[0, MAX_ADD_ON_QUANTITY]`; disallowed add-ons are ignored. */
interface ChangeAddOnQuantityUseCase {
    operator fun invoke(
        details: DrinkDetails,
        configuration: DrinkConfiguration,
        addOnId: AddOnId,
        delta: Int,
    ): DrinkConfiguration
}
