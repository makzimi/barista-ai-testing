package ru.otus.barista.data.impl.repository

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import ru.otus.barista.core.testing.TestDispatcherProvider
import ru.otus.barista.data.impl.local.FakeFavoritesLocalDataSource
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class FavoritesRepositoryImplTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private val local = FakeFavoritesLocalDataSource(initial = setOf("espresso"))
    private val sut = FavoritesRepositoryImpl(local, TestDispatcherProvider(dispatcher))

    @Test
    fun `observe WHEN a drink is stored locally SHOULD emit its id`() = runTest(dispatcher) {
        val favorites = sut.observe().first()

        assertEquals(setOf("espresso"), favorites)
    }

    @Test
    fun `set WHEN a drink is marked favorite SHOULD store it next to the existing ones`() = runTest(dispatcher) {
        sut.set("matcha_latte", isFavorite = true)

        assertEquals(setOf("espresso", "matcha_latte"), local.favorites.value)
    }

    @Test
    fun `set WHEN a favorite is removed SHOULD drop only that id`() = runTest(dispatcher) {
        local.favorites.value = setOf("espresso", "matcha_latte")

        sut.set("espresso", isFavorite = false)

        assertEquals(setOf("matcha_latte"), sut.observe().first())
    }
}
