package ru.otus.barista.data.impl.local

import ru.otus.barista.data.model.DrinkDto
import java.util.concurrent.atomic.AtomicReference

/** Last successfully loaded menu, exactly as the backend sent it. Lives as long as the process. */
internal class InMemoryMenuCache {
    private val drinks = AtomicReference<List<DrinkDto>?>(null)

    fun get(): List<DrinkDto>? = drinks.get()

    fun put(value: List<DrinkDto>) {
        drinks.set(value)
    }

    fun clear() {
        drinks.set(null)
    }
}
