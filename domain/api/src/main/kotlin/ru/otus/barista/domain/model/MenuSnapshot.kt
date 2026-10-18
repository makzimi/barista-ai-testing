package ru.otus.barista.domain.model

/** The menu as the app shows it. [isStale] = remote failed and cached data was served (rule 7). */
data class MenuSnapshot(
    val drinks: List<Drink>,
    val isStale: Boolean,
)
