package ru.otus.barista.domain.impl.usecase

import kotlinx.coroutines.test.runTest
import ru.otus.barista.core.testing.Create
import ru.otus.barista.core.testing.FakeFavoritesRepository
import ru.otus.barista.core.testing.FavoriteCall
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ToggleFavoriteUseCaseImplTest {

    private val favoritesRepository = FakeFavoritesRepository()
    private val sut = ToggleFavoriteUseCaseImpl(favoritesRepository)

    @Test
    fun `WHEN the drink is not favorite SHOULD store it and return true`() = runTest {
        val result = sut(Create.ESPRESSO.id, isFavoriteNow = false)

        assertEquals(true, result.getOrThrow())
        assertEquals(listOf(FavoriteCall("espresso", isFavorite = true)), favoritesRepository.calls)
    }

    @Test
    fun `WHEN the drink is favorite SHOULD remove it and return false`() = runTest {
        favoritesRepository.favorites.value = setOf("espresso")

        val result = sut(Create.ESPRESSO.id, isFavoriteNow = true)

        assertEquals(false, result.getOrThrow())
        assertEquals(emptySet(), favoritesRepository.favorites.value)
    }

    @Test
    fun `WHEN storing fails SHOULD return the failure instead of throwing`() = runTest {
        favoritesRepository.failSet = IllegalStateException("disk full")

        val result = sut(Create.ESPRESSO.id, isFavoriteNow = false)

        assertIs<IllegalStateException>(result.exceptionOrNull())
    }
}
