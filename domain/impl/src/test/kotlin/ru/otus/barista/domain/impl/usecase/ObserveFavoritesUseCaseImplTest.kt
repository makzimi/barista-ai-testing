package ru.otus.barista.domain.impl.usecase

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import ru.otus.barista.core.testing.Create
import ru.otus.barista.core.testing.FakeFavoritesRepository
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveFavoritesUseCaseImplTest {

    private val favoritesRepository = FakeFavoritesRepository(initial = setOf("espresso", "matcha_latte"))
    private val sut = ObserveFavoritesUseCaseImpl(favoritesRepository)

    @Test
    fun `WHEN ids are stored SHOULD emit them as DrinkIds`() = runTest {
        val favorites = sut().first()

        assertEquals(setOf(Create.ESPRESSO.id, Create.MATCHA.id), favorites)
    }
}
