package ru.otus.barista.data.model

/** What the data layer returns for the menu. [isStale] = remote failed and cached data was served (rule 7). */
data class MenuSnapshotData(
    val drinks: List<DrinkDto>,
    val isStale: Boolean,
)
