package ru.otus.barista.domain.usecase

import ru.otus.barista.domain.model.DrinkDetails
import ru.otus.barista.domain.model.DrinkId

/** One drink with its favorite flag and the happy-hour discount active at load time (rule 1). */
interface GetDrinkDetailsUseCase {
    suspend operator fun invoke(id: DrinkId): Result<DrinkDetails>
}
