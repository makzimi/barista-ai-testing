package ru.otus.barista.domain.usecase

import kotlinx.coroutines.flow.Flow
import ru.otus.barista.domain.model.DrinkId

/** Ids of the favorite drinks; emits on every change (rule 8). */
interface ObserveFavoritesUseCase {
    operator fun invoke(): Flow<Set<DrinkId>>
}
