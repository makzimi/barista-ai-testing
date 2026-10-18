package ru.otus.barista.data.impl.remote

import ru.otus.barista.core.network.NetworkFactory
import ru.otus.barista.data.model.MenuDto

/** Reads JSON fixtures from `src/test/resources/fixtures/` (a copy of the app's fake-backend `menu.json`). */
internal object Fixtures {

    fun read(name: String): String {
        val stream = checkNotNull(javaClass.classLoader?.getResourceAsStream("fixtures/$name")) {
            "Fixture fixtures/$name not found in src/test/resources"
        }
        return stream.bufferedReader().use { it.readText() }
    }

    /** The full 12-drink menu, parsed with the production JSON settings. */
    fun menuResponse(): MenuDto = NetworkFactory.json.decodeFromString(read("menu.json"))
}
