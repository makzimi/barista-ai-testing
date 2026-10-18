// Contrast example for the lesson: the same behaviour as GetMenuUseCaseImplTest, written with mocks.
package ru.otus.barista.domain.impl.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import ru.otus.barista.core.testing.Create
import ru.otus.barista.data.model.DataError
import ru.otus.barista.data.model.MenuSnapshotData
import ru.otus.barista.data.repository.FavoritesRepository
import ru.otus.barista.data.repository.MenuRepository
import ru.otus.barista.domain.impl.mapper.DrinkMapper
import ru.otus.barista.domain.model.MenuError
import ru.otus.barista.domain.model.MenuSnapshot
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class GetMenuUseCaseImplMockkTest {

    private val menuRepository: MenuRepository = mockk()
    private val favoritesRepository: FavoritesRepository = mockk()
    private val sut = GetMenuUseCaseImpl(menuRepository, favoritesRepository, DrinkMapper())

    @Test
    fun `WHEN repository has drinks SHOULD return them as a fresh snapshot`() = runTest {
        val dtos = listOf(Create.ESPRESSO_DTO, Create.MATCHA_DTO)
        coEvery { menuRepository.getMenu(forceRefresh = false) } returns
            Result.success(MenuSnapshotData(drinks = dtos, isStale = false))
        every { favoritesRepository.observe() } returns flowOf(emptySet())
        val expected = MenuSnapshot(drinks = listOf(Create.ESPRESSO, Create.MATCHA), isStale = false)

        val result = sut(forceRefresh = false)

        assertEquals(expected, result.getOrThrow())
        coVerify(exactly = 1) { menuRepository.getMenu(forceRefresh = false) }
    }

    @Test
    fun `WHEN repository fails with cold cache SHOULD return the Network error`() = runTest {
        coEvery { menuRepository.getMenu(forceRefresh = true) } returns Result.failure(DataError.Network())

        val result = sut(forceRefresh = true)

        assertIs<MenuError.Network>(result.exceptionOrNull())
        coVerify(exactly = 1) { menuRepository.getMenu(forceRefresh = true) }
    }
}
