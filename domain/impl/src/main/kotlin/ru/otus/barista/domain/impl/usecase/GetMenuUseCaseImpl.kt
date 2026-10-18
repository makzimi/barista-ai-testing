package ru.otus.barista.domain.impl.usecase

import kotlinx.coroutines.flow.first
import ru.otus.barista.data.repository.FavoritesRepository
import ru.otus.barista.data.repository.MenuRepository
import ru.otus.barista.domain.impl.mapper.DrinkMapper
import ru.otus.barista.domain.model.MenuSnapshot
import ru.otus.barista.domain.usecase.GetMenuUseCase

/** Loads the menu (rule 7 comes from the repository), merges the stored favorites (rule 8), maps DTOs to domain. */
class GetMenuUseCaseImpl(
    private val menuRepository: MenuRepository,
    private val favoritesRepository: FavoritesRepository,
    private val mapper: DrinkMapper,
) : GetMenuUseCase {

    override suspend operator fun invoke(forceRefresh: Boolean): Result<MenuSnapshot> =
        menuRepository.getMenu(forceRefresh).fold(
            onSuccess = { data -> Result.success(mapper.mapSnapshot(data, favoritesRepository.observe().first())) },
            onFailure = { error -> Result.failure(mapper.mapError(error)) },
        )
}
