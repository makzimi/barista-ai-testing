package ru.otus.barista.feature.drinkdetails.di

import ru.otus.barista.domain.usecase.CalculatePriceUseCase
import ru.otus.barista.domain.usecase.ChangeAddOnQuantityUseCase
import ru.otus.barista.domain.usecase.GetDrinkDetailsUseCase
import ru.otus.barista.domain.usecase.ObserveIsFavoriteUseCase
import ru.otus.barista.domain.usecase.ToggleFavoriteUseCase

/** What the drink details feature needs: use case interfaces only (`:domain:api`). Implemented by `AppGraph`. */
interface DrinkDetailsDependencies {
    val getDrinkDetails: GetDrinkDetailsUseCase
    val observeIsFavorite: ObserveIsFavoriteUseCase
    val toggleFavorite: ToggleFavoriteUseCase
    val calculatePrice: CalculatePriceUseCase
    val changeAddOnQuantity: ChangeAddOnQuantityUseCase
}
