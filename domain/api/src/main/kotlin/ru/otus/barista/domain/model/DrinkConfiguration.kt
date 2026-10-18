package ru.otus.barista.domain.model

/** What the user picked: a size and add-on quantities (absent key = 0). */
data class DrinkConfiguration(
    val size: Size = Size.S,
    val addOns: Map<AddOnId, Int> = emptyMap(),
) {
    fun quantityOf(id: AddOnId): Int = addOns[id] ?: 0

    companion object {
        /** Rule 6: at most this many of one add-on. */
        const val MAX_ADD_ON_QUANTITY: Int = 3
    }
}
