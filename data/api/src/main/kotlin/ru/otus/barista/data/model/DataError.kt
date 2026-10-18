package ru.otus.barista.data.model

/** Failures of the data layer. The domain layer maps them to its own errors (`MenuError`). */
sealed class DataError(message: String, cause: Throwable? = null) : Exception(message, cause) {
    /** Remote failed and there was nothing cached to fall back to. */
    class Network(cause: Throwable? = null) : DataError("Backend is unavailable", cause)

    /** The backend does not know this drink. */
    class NotFound(val id: String) : DataError("Drink $id not found")
}
