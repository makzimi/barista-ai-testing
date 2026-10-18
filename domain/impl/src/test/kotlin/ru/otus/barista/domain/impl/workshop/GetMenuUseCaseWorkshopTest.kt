@file:Suppress("NoUnusedImports", "UnusedImports", "UnusedPrivateProperty")

package ru.otus.barista.domain.impl.workshop

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import ru.otus.barista.core.common.Money
import ru.otus.barista.core.testing.Create
import ru.otus.barista.core.testing.FakeFavoritesRepository
import ru.otus.barista.core.testing.FakeMenuRepository
import ru.otus.barista.core.testing.FixedClock
import ru.otus.barista.data.model.MenuSnapshotData
import ru.otus.barista.data.repository.FavoritesRepository
import ru.otus.barista.data.repository.MenuRepository
import ru.otus.barista.domain.impl.mapper.DrinkMapper
import ru.otus.barista.domain.impl.usecase.ApplyHappyHourUseCaseImpl
import ru.otus.barista.domain.impl.usecase.FilterAndSortMenuUseCaseImpl
import ru.otus.barista.domain.impl.usecase.GetMenuUseCaseImpl
import ru.otus.barista.domain.model.MenuFilter
import ru.otus.barista.domain.model.MenuItem
import ru.otus.barista.domain.model.MenuSnapshot
import ru.otus.barista.domain.model.MenuSort
import java.time.LocalTime
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Live-coding copy for LIVE_CODING_SCRIPT Part 1.2: use cases over data-layer fakes instead of mocks.
 * `GetMenuUseCaseImpl` loads DTOs from [FakeMenuRepository], merges [FakeFavoritesRepository] and maps to domain;
 * steps 1–2 chain the real implementations the way the ViewModel does: load → apply happy hour → filter and sort.
 * Uncomment one region at a time (select it, `Cmd + /`) and run it from the gutter.
 */
class GetMenuUseCaseWorkshopTest {

    private val menuRepository = FakeMenuRepository()
    private val favoritesRepository = FakeFavoritesRepository()
    private val clock = FixedClock(time = LocalTime.of(12, 0))
    private val sut = GetMenuUseCaseImpl(menuRepository, favoritesRepository, DrinkMapper())
    private val applyHappyHour = ApplyHappyHourUseCaseImpl(clock)
    private val filterAndSort = FilterAndSortMenuUseCaseImpl()

    // region Step 1 — FakeMenuRepository + FixedClock: a drink inside its happy-hour window gets discountPercent
    // @Test
    // fun `WHEN clock is inside the happy-hour window SHOULD give the drink a discountPercent`() = runTest {
    //     menuRepository.setDrinks(listOf(Create.FLAT_WHITE_DTO))
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
    //     val cheapSoldOut = Create.drinkDto(id = "iced_latte", name = "Айс латте", price = 1.0, isSoldOut = true)
    //     val others = listOf(Create.ESPRESSO_DTO, Create.MATCHA_DTO)
    //     menuRepository.setDrinks(listOf(cheapSoldOut) + others)
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

    // region Step 3 — favorites stored on the device are merged into the drinks (rule 8 lives in the domain now)
    // @Test
    // fun `WHEN a drink is stored as favorite SHOULD return it with isFavorite true`() = runTest {
    //     menuRepository.setDrinks(listOf(Create.ESPRESSO_DTO, Create.MATCHA_DTO))
    //     favoritesRepository.favorites.value = setOf("matcha_latte")
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

    // region Step 4 (contrast) — Step 3 with MockK: longer, restates the data, breaks on refactor
    // @Test
    // fun `WHEN a drink is stored as favorite SHOULD return it with isFavorite true using mocks`() = runTest {
    //     val mockMenuRepository = mockk<MenuRepository>()
    //     val mockFavoritesRepository = mockk<FavoritesRepository>()
    //     val mockSut = GetMenuUseCaseImpl(mockMenuRepository, mockFavoritesRepository, DrinkMapper())
    //     val dtos = listOf(Create.ESPRESSO_DTO, Create.MATCHA_DTO)
    //     coEvery { mockMenuRepository.getMenu(forceRefresh = false) } returns
    //         Result.success(MenuSnapshotData(drinks = dtos, isStale = false))
    //     every { mockFavoritesRepository.observe() } returns flowOf(setOf("matcha_latte"))
    //     val expected = MenuSnapshot(
    //         drinks = listOf(Create.ESPRESSO, Create.MATCHA.copy(isFavorite = true)),
    //         isStale = false,
    //     )
    //
    //     val result = mockSut(forceRefresh = false)
    //
    //     assertEquals(expected, result.getOrThrow())
    //     coVerify(exactly = 1) { mockMenuRepository.getMenu(forceRefresh = false) }
    // }
    // endregion
}
