package ru.otus.barista.feature.menu.domain

import ru.otus.barista.feature.menu.api.Drink
import ru.otus.barista.feature.menu.api.DrinkId

/** Re-applies the latest favorites (rule 8) to an already loaded list of drinks. */
fun List<Drink>.withFavorites(favoriteIds: Set<DrinkId>): List<Drink> =
    map { drink -> drink.copy(isFavorite = drink.id in favoriteIds) }
