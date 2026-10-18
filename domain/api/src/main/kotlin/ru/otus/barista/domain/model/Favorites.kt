package ru.otus.barista.domain.model

/** Re-applies the latest favorites (rule 8) to an already loaded list of drinks. */
fun List<Drink>.withFavorites(favoriteIds: Set<DrinkId>): List<Drink> =
    map { drink -> drink.copy(isFavorite = drink.id in favoriteIds) }
