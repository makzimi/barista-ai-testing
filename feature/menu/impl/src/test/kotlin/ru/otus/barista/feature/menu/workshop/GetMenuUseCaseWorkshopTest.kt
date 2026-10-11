@file:Suppress("NoUnusedImports", "UnusedImports", "UnusedPrivateProperty")

package ru.otus.barista.feature.menu.workshop

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import ru.otus.barista.core.common.Money
import ru.otus.barista.core.testing.Create
import ru.otus.barista.core.testing.FakeMenuRepository
import ru.otus.barista.core.testing.FixedClock
import ru.otus.barista.feature.menu.api.MenuRepository
import ru.otus.barista.feature.menu.api.MenuSnapshot
import ru.otus.barista.feature.menu.domain.ApplyHappyHourUseCase
import ru.otus.barista.feature.menu.domain.FilterAndSortMenuUseCase
import ru.otus.barista.feature.menu.domain.GetMenuUseCase
import ru.otus.barista.feature.menu.domain.MenuFilter
import ru.otus.barista.feature.menu.domain.MenuItem
import ru.otus.barista.feature.menu.domain.MenuSort
import java.time.LocalTime
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Live-coding copy for LIVE_CODING_SCRIPT Part 1.2: use cases with a fake repository instead of mocks.
 * `GetMenuUseCase` only loads the snapshot; the happy-hour and sorting rules live in the two neighbouring use cases,
 * so steps 1–2 run the same pipeline the ViewModel runs: load → apply happy hour → filter and sort.
 * Uncomment one region at a time (select it, `Cmd + /`) and run it from the gutter.
 */
class GetMenuUseCaseWorkshopTest {

    private val repository = FakeMenuRepository()
    private val clock = FixedClock(time = LocalTime.of(12, 0))
    private val sut = GetMenuUseCase(repository)
    private val applyHappyHour = ApplyHappyHourUseCase(clock)
    private val filterAndSort = FilterAndSortMenuUseCase()

    // region Step 1 — FakeMenuRepository + FixedClock: a drink inside its happy-hour window gets discountPercent
    // @Test
    // fun `WHEN clock is inside the happy-hour window SHOULD give the drink a discountPercent`() = runTest {
    //     repository.setDrinks(listOf(Create.FLAT_WHITE))
    //     clock.time = LocalTime.of(17, 0)
    //     val expected = listOf(MenuItem(drink = Create.FLAT_WHITE, price = Money(19192), discountPercent = 20))
    //
    //     val items = applyHappyHour(sut(forceRefresh = false).getOrThrow().drinks)
    //
    //     assertEquals(expected, items)
    // }
    // endregion

    // region Step 2 — sold-out drinks are last regardless of the sort
    // @Test
    // fun `WHEN the sold-out drink is the cheapest SHOULD still put it last`() = runTest {
    //     val cheapSoldOut = Create.ICE_LATTE_SOLD_OUT.copy(basePrice = Money(100))
    //     repository.setDrinks(listOf(cheapSoldOut, Create.MATCHA, Create.ESPRESSO))
    //     val expected = listOf(Create.ESPRESSO.id, Create.MATCHA.id, Create.ICE_LATTE_SOLD_OUT.id)
    //
    //     val items = filterAndSort(
    //         applyHappyHour(sut(forceRefresh = false).getOrThrow().drinks),
    //         MenuFilter(sort = MenuSort.BY_PRICE),
    //     )
    //
    //     assertEquals(expected, items.map { it.drink.id })
    // }
    // endregion

    // region Step 3 — favorites stored in the repository are merged into the drinks
    // @Test
    // fun `WHEN a drink is stored as favorite SHOULD return it with isFavorite true`() = runTest {
    //     repository.setDrinks(listOf(Create.ESPRESSO, Create.MATCHA))
    //     repository.favorites.value = setOf(Create.MATCHA.id)
    //     val expected = MenuSnapshot(
    //         drinks = listOf(Create.ESPRESSO, Create.MATCHA.copy(isFavorite = true)),
    //         isStale = false,
    //     )
    //
    //     val result = sut(forceRefresh = false)
    //
    //     assertEquals(expected, result.getOrThrow())
    // }
    // endregion

    // region Step 4 (contrast) — Step 3 with MockK: longer, restates the merge itself, breaks on refactor
    // @Test
    // fun `WHEN a drink is stored as favorite SHOULD return it with isFavorite true using mocks`() = runTest {
    //     val mockRepository = mockk<MenuRepository>()
    //     val mockSut = GetMenuUseCase(mockRepository)
    //     val snapshot = MenuSnapshot(
    //         drinks = listOf(Create.ESPRESSO, Create.MATCHA.copy(isFavorite = true)),
    //         isStale = false,
    //     )
    //     coEvery { mockRepository.getMenu(forceRefresh = false) } returns Result.success(snapshot)
    //     every { mockRepository.observeFavorites() } returns flowOf(setOf(Create.MATCHA.id))
    //
    //     val result = mockSut(forceRefresh = false)
    //
    //     assertEquals(snapshot, result.getOrThrow())
    //     coVerify(exactly = 1) { mockRepository.getMenu(forceRefresh = false) }
    // }
    // endregion
}
