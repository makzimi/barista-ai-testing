package ru.otus.barista.domain.impl.usecase

import kotlinx.coroutines.flow.first
import ru.otus.barista.core.common.Clock
import ru.otus.barista.data.repository.FavoritesRepository
import ru.otus.barista.data.repository.MenuRepository
import ru.otus.barista.domain.impl.mapper.DrinkMapper
import ru.otus.barista.domain.model.DrinkDetails
import ru.otus.barista.domain.model.DrinkId
import ru.otus.barista.domain.model.MenuError
import ru.otus.barista.domain.usecase.GetDrinkDetailsUseCase

/** Loads a drink with its favorite flag and resolves its happy-hour discount at the current [Clock] time (rule 1). */
class GetDrinkDetailsUseCaseImpl(
    private val menuRepository: MenuRepository,
    private val favoritesRepository: FavoritesRepository,
    private val mapper: DrinkMapper,
    private val clock: Clock,
) : GetDrinkDetailsUseCase {

    override suspend operator fun invoke(id: DrinkId): Result<DrinkDetails> =
        menuRepository.getDrink(id.value).fold(
            onSuccess = { dto ->
                val drink = mapper.mapDrink(dto, favoritesRepository.observe().first())
                    ?: return Result.failure(MenuError.NotFound(id))
                val now = clock.localTime()
                val discountPercent = drink.happyHour?.takeIf { it.isActiveAt(now) }?.percent
                Result.success(DrinkDetails(drink = drink, discountPercent = discountPercent))
            },
            onFailure = { error -> Result.failure(mapper.mapError(error)) },
        )
}
