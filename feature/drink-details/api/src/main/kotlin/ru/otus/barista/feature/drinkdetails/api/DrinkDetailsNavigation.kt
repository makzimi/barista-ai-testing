package ru.otus.barista.feature.drinkdetails.api

/** Navigation contract of the drink details feature. Registration lives in `:feature:drink-details:impl`. */
const val DRINK_ID_ARG: String = "drinkId"

const val DRINK_DETAILS_ROUTE: String = "drink/{$DRINK_ID_ARG}"

fun drinkDetailsRoute(drinkId: String): String = "drink/$drinkId"
