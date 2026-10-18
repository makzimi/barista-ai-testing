package ru.otus.barista.domain.impl.usecase

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import ru.otus.barista.core.testing.Create
import ru.otus.barista.core.testing.FakeFavoritesRepository
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveIsFavoriteUseCaseImplTest {

    private val favoritesRepository = FakeFavoritesRepository()
    private val sut = ObserveIsFavoriteUseCaseImpl(favoritesRepository)

    @Test
    fun `WHEN the drink becomes favorite SHOULD emit false then true`() = runTest {
        sut(Create.ESPRESSO.id).test {
            assertEquals(false, awaitItem())

            favoritesRepository.favorites.value = setOf("espresso")

            assertEquals(true, awaitItem())
        }
    }

    @Test
    fun `WHEN another drink changes SHOULD not emit again`() = runTest {
        sut(Create.ESPRESSO.id).test {
            assertEquals(false, awaitItem())

            favoritesRepository.favorites.value = setOf("matcha_latte")

            expectNoEvents()
        }
    }
}
