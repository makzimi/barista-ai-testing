// Contrast example for the lesson: the same behaviour as GetMenuUseCaseTest, written with mocks.
package ru.otus.barista.feature.menu.domain

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import ru.otus.barista.core.testing.Create
import ru.otus.barista.feature.menu.api.MenuError
import ru.otus.barista.feature.menu.api.MenuRepository
import ru.otus.barista.feature.menu.api.MenuSnapshot
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class GetMenuUseCaseMockkTest {

    private val repository: MenuRepository = mockk()
    private val sut = GetMenuUseCase(repository)

    @Test
    fun `WHEN repository has drinks SHOULD return them as a fresh snapshot`() = runTest {
        val snapshot = MenuSnapshot(drinks = listOf(Create.ESPRESSO, Create.MATCHA), isStale = false)
        coEvery { repository.getMenu(forceRefresh = false) } returns Result.success(snapshot)

        val result = sut(forceRefresh = false)

        assertEquals(snapshot, result.getOrThrow())
        coVerify(exactly = 1) { repository.getMenu(forceRefresh = false) }
    }

    @Test
    fun `WHEN repository fails with cold cache SHOULD return the Network error`() = runTest {
        coEvery { repository.getMenu(forceRefresh = true) } returns Result.failure(MenuError.Network())

        val result = sut(forceRefresh = true)

        assertIs<MenuError.Network>(result.exceptionOrNull())
        coVerify(exactly = 1) { repository.getMenu(forceRefresh = true) }
    }
}
