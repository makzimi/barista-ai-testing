package ru.otus.barista.feature.menu.data.local

import ru.otus.barista.feature.menu.api.Drink
import java.util.concurrent.atomic.AtomicReference

/** Last successfully loaded menu (without favorites). Lives as long as the process. */
class InMemoryMenuCache {
    private val drinks = AtomicReference<List<Drink>?>(null)

    fun get(): List<Drink>? = drinks.get()

    fun put(value: List<Drink>) {
        drinks.set(value)
    }

    fun clear() {
        drinks.set(null)
    }
}
