package ru.otus.barista.feature.drinkdetails.domain

import ru.otus.barista.core.common.Clock
import ru.otus.barista.feature.menu.api.DrinkId
import ru.otus.barista.feature.menu.api.MenuRepository

/** Loads a drink and resolves its happy-hour discount at the current [Clock] time (rule 1). */
class GetDrinkDetailsUseCase(
    private val repository: MenuRepository,
    private val clock: Clock,
) {
    suspend operator fun invoke(id: DrinkId): Result<DrinkDetails> =
        repository.getDrink(id).map { drink ->
            val now = clock.localTime()
            DrinkDetails(
                drink = drink,
                discountPercent = drink.happyHour?.takeIf { it.isActiveAt(now) }?.percent,
            )
        }
}
