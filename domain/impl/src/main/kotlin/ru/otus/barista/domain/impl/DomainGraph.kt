package ru.otus.barista.domain.impl

import ru.otus.barista.core.common.Clock
import ru.otus.barista.data.repository.FavoritesRepository
import ru.otus.barista.data.repository.MenuRepository
import ru.otus.barista.domain.impl.mapper.DrinkMapper
import ru.otus.barista.domain.impl.usecase.ApplyHappyHourUseCaseImpl
import ru.otus.barista.domain.impl.usecase.CalculatePriceUseCaseImpl
import ru.otus.barista.domain.impl.usecase.ChangeAddOnQuantityUseCaseImpl
import ru.otus.barista.domain.impl.usecase.FilterAndSortMenuUseCaseImpl
import ru.otus.barista.domain.impl.usecase.GetDrinkDetailsUseCaseImpl
import ru.otus.barista.domain.impl.usecase.GetMenuUseCaseImpl
import ru.otus.barista.domain.impl.usecase.ObserveFavoritesUseCaseImpl
import ru.otus.barista.domain.impl.usecase.ObserveIsFavoriteUseCaseImpl
import ru.otus.barista.domain.impl.usecase.ToggleFavoriteUseCaseImpl
import ru.otus.barista.domain.usecase.ApplyHappyHourUseCase
import ru.otus.barista.domain.usecase.CalculatePriceUseCase
import ru.otus.barista.domain.usecase.ChangeAddOnQuantityUseCase
import ru.otus.barista.domain.usecase.FilterAndSortMenuUseCase
import ru.otus.barista.domain.usecase.GetDrinkDetailsUseCase
import ru.otus.barista.domain.usecase.GetMenuUseCase
import ru.otus.barista.domain.usecase.ObserveFavoritesUseCase
import ru.otus.barista.domain.usecase.ObserveIsFavoriteUseCase
import ru.otus.barista.domain.usecase.ToggleFavoriteUseCase

/** Manual DI for the domain layer: every use case, typed as its `:domain:api` interface. */
class DomainGraph(
    menuRepository: MenuRepository,
    favoritesRepository: FavoritesRepository,
    clock: Clock,
) {
    private val mapper = DrinkMapper()

    val getMenu: GetMenuUseCase = GetMenuUseCaseImpl(menuRepository, favoritesRepository, mapper)
    val observeFavorites: ObserveFavoritesUseCase = ObserveFavoritesUseCaseImpl(favoritesRepository)
    val toggleFavorite: ToggleFavoriteUseCase = ToggleFavoriteUseCaseImpl(favoritesRepository)
    val applyHappyHour: ApplyHappyHourUseCase = ApplyHappyHourUseCaseImpl(clock)
    val filterAndSortMenu: FilterAndSortMenuUseCase = FilterAndSortMenuUseCaseImpl()
    val getDrinkDetails: GetDrinkDetailsUseCase =
        GetDrinkDetailsUseCaseImpl(menuRepository, favoritesRepository, mapper, clock)
    val observeIsFavorite: ObserveIsFavoriteUseCase = ObserveIsFavoriteUseCaseImpl(favoritesRepository)
    val calculatePrice: CalculatePriceUseCase = CalculatePriceUseCaseImpl()
    val changeAddOnQuantity: ChangeAddOnQuantityUseCase = ChangeAddOnQuantityUseCaseImpl()
}
