package ru.otus.barista.domain.usecase

import kotlinx.coroutines.flow.Flow
import ru.otus.barista.domain.model.DrinkId

/** Favorite flag of one drink, shared with the menu list (rule 8). */
interface ObserveIsFavoriteUseCase {
    operator fun invoke(id: DrinkId): Flow<Boolean>
}
