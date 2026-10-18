package ru.otus.barista.domain.model

/** Failures of the menu use cases. */
sealed class MenuError(message: String, cause: Throwable? = null) : Exception(message, cause) {
    /** Remote failed and there was nothing cached to fall back to. */
    class Network(cause: Throwable? = null) : MenuError("Menu is unavailable", cause)

    /** The backend does not know this drink. */
    class NotFound(val id: DrinkId) : MenuError("Drink ${id.value} not found")
}
