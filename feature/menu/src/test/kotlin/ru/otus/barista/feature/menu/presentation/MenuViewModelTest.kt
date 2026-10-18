package ru.otus.barista.feature.menu.presentation

import app.cash.turbine.test
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import ru.otus.barista.core.common.Money
import ru.otus.barista.core.testing.Create
import ru.otus.barista.core.testing.FakeApplyHappyHourUseCase
import ru.otus.barista.core.testing.FakeFilterAndSortMenuUseCase
import ru.otus.barista.core.testing.FakeGetMenuUseCase
import ru.otus.barista.core.testing.FakeObserveFavoritesUseCase
import ru.otus.barista.core.testing.FakeToggleFavoriteUseCase
import ru.otus.barista.core.testing.MainDispatcherRule
import ru.otus.barista.core.testing.TestDispatcherProvider
import ru.otus.barista.core.testing.ToggleFavoriteCall
import ru.otus.barista.domain.model.Category
import ru.otus.barista.domain.model.MenuError
import ru.otus.barista.domain.model.MenuFilter
import ru.otus.barista.domain.model.MenuSnapshot
import ru.otus.barista.domain.model.MenuSort
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The ViewModel against fakes of its use case interfaces — the only thing a feature sees of the domain.
 * Business rules (discounts, filtering, sorting) are the use cases' job and are tested in `:domain:impl`;
 * here each fake returns exactly what the scenario needs, and we check what the ViewModel does with it.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MenuViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(StandardTestDispatcher())

    private val getMenu = FakeGetMenuUseCase(
        MenuSnapshot(drinks = listOf(Create.MATCHA, Create.ESPRESSO, Create.ICE_LATTE_SOLD_OUT), isStale = false),
    )
    private val observeFavorites = FakeObserveFavoritesUseCase()
    private val toggleFavorite = FakeToggleFavoriteUseCase(observeFavorites.favorites)
    private val applyHappyHour = FakeApplyHappyHourUseCase()
    private val filterAndSort = FakeFilterAndSortMenuUseCase()

    private val matchaRow = MenuItemState(
        id = Create.MATCHA.id,
        name = "Матча латте",
        description = "Японский зелёный чай матча с молоком",
        price = "259.90 ₽",
        oldPrice = null,
        discountLabel = null,
        isNew = false,
        isSoldOut = false,
        isFavorite = false,
    )
    private val espressoRow = MenuItemState(
        id = Create.ESPRESSO.id,
        name = "Эспрессо",
        description = "Классический крепкий эспрессо из смеси арабики",
        price = "119.90 ₽",
        oldPrice = null,
        discountLabel = null,
        isNew = false,
        isSoldOut = false,
        isFavorite = false,
    )
    private val icedLatteRow = MenuItemState(
        id = Create.ICE_LATTE_SOLD_OUT.id,
        name = "Айс латте",
        description = "Холодный латте со льдом",
        price = "229.90 ₽",
        oldPrice = null,
        discountLabel = null,
        isNew = false,
        isSoldOut = true,
        isFavorite = false,
    )
    private val content = MenuState(
        status = MenuStatus.Content,
        items = persistentListOf(matchaRow, espressoRow, icedLatteRow),
    )

    @Test
    fun `opening WHEN the menu loads SHOULD go from Loading to Content`() = runTest {
        val viewModel = createViewModel()

        viewModel.state.test {
            assertEquals(MenuState(status = MenuStatus.Loading), awaitItem())
            assertEquals(content, awaitItem())
        }
    }

    @Test
    fun `opening WHEN the happy-hour use case discounts a drink SHOULD show the discounted price and badge`() =
        runTest {
            getMenu.snapshot = MenuSnapshot(drinks = listOf(Create.CAPPUCCINO), isStale = false)
            applyHappyHour.setDiscount(Create.CAPPUCCINO.id, price = Money(18692), percent = 15)
            val expected = MenuState(
                status = MenuStatus.Content,
                items = persistentListOf(
                    MenuItemState(
                        id = Create.CAPPUCCINO.id,
                        name = "Капучино",
                        description = "Эспрессо с молоком и плотной молочной пеной",
                        price = "186.92 ₽",
                        oldPrice = "219.90 ₽",
                        discountLabel = "−15%",
                        isNew = false,
                        isSoldOut = false,
                        isFavorite = false,
                    ),
                ),
            )

            val viewModel = createLoadedViewModel()

            assertEquals(expected, viewModel.state.value)
        }

    @Test
    fun `opening WHEN loading fails with a network error SHOULD show network error`() = runTest {
        getMenu.failNext = MenuError.Network()
        val expected = MenuState(status = MenuStatus.Error(ErrorKind.NETWORK))

        val viewModel = createLoadedViewModel()

        assertEquals(expected, viewModel.state.value)
    }

    @Test
    fun `opening WHEN loading fails with an unexpected error SHOULD show unknown error`() = runTest {
        getMenu.failNext = IllegalStateException("boom")
        val expected = MenuState(status = MenuStatus.Error(ErrorKind.UNKNOWN))

        val viewModel = createLoadedViewModel()

        assertEquals(expected, viewModel.state.value)
    }

    @Test
    fun `Retry WHEN the first load failed SHOULD show Content`() = runTest {
        getMenu.failNext = MenuError.Network()
        val viewModel = createLoadedViewModel()

        viewModel.onAction(MenuAction.Retry)
        advanceUntilIdle()

        assertEquals(content, viewModel.state.value)
    }

    @Test
    fun `opening WHEN the snapshot is stale SHOULD show content with the stale banner`() = runTest {
        getMenu.snapshot = getMenu.snapshot.copy(isStale = true)

        val viewModel = createLoadedViewModel()

        assertEquals(content.copy(isStale = true), viewModel.state.value)
    }

    @Test
    fun `ToggleFavoritesOnly WHEN nothing is favorite SHOULD show Empty NO_FAVORITES`() = runTest {
        filterAndSort.answer = { items, filter ->
            if (filter.favoritesOnly) items.filter { it.drink.isFavorite } else items
        }
        val viewModel = createLoadedViewModel()
        val expected = MenuState(status = MenuStatus.Empty(EmptyReason.NO_FAVORITES), favoritesOnly = true)

        viewModel.onAction(MenuAction.ToggleFavoritesOnly)
        advanceUntilIdle()

        assertEquals(expected, viewModel.state.value)
    }

    @Test
    fun `Search WHEN the use case finds nothing for the query SHOULD show Empty NOTHING_FOUND`() = runTest {
        filterAndSort.answer = { items, filter -> if (filter.query.isEmpty()) items else emptyList() }
        val viewModel = createLoadedViewModel()
        val expected = MenuState(status = MenuStatus.Empty(EmptyReason.NOTHING_FOUND), query = "раф")

        viewModel.onAction(MenuAction.Search("раф"))
        advanceUntilIdle()

        assertEquals(expected, viewModel.state.value)
    }

    @Test
    fun `Search WHEN only 299 ms passed SHOULD update the query but not ask to filter by it yet`() = runTest {
        val viewModel = createLoadedViewModel()

        viewModel.onAction(MenuAction.Search("матча"))
        advanceTimeBy(299)

        assertEquals(content.copy(query = "матча"), viewModel.state.value)
        assertEquals("", filterAndSort.filters.last().query)
    }

    @Test
    fun `Search WHEN the 300 ms debounce passed SHOULD show what the use case returned for the query`() = runTest {
        filterAndSort.answer = { items, filter ->
            if (filter.query == "матча") items.filter { it.drink.id == Create.MATCHA.id } else items
        }
        val viewModel = createLoadedViewModel()
        val expected = content.copy(items = persistentListOf(matchaRow), query = "матча")

        viewModel.onAction(MenuAction.Search("матча"))
        advanceTimeBy(300)
        runCurrent()

        assertEquals(expected, viewModel.state.value)
    }

    @Test
    fun `SelectCategory WHEN TEA is selected SHOULD filter by the TEA category`() = runTest {
        val viewModel = createLoadedViewModel()

        viewModel.onAction(MenuAction.SelectCategory(CategoryFilter.TEA))
        advanceUntilIdle()

        assertEquals(MenuFilter(category = Category.TEA), filterAndSort.filters.last())
        assertEquals(CategoryFilter.TEA, viewModel.state.value.category)
    }

    @Test
    fun `ChangeSort WHEN sorting by price SHOULD show the items in the order the use case returned`() = runTest {
        filterAndSort.answer = { items, filter -> if (filter.sort == MenuSort.BY_PRICE) items.reversed() else items }
        val viewModel = createLoadedViewModel()
        val expected = content.copy(
            items = persistentListOf(icedLatteRow, espressoRow, matchaRow),
            sort = MenuSort.BY_PRICE,
        )

        viewModel.onAction(MenuAction.ChangeSort(MenuSort.BY_PRICE))
        advanceUntilIdle()

        assertEquals(expected, viewModel.state.value)
    }

    @Test
    fun `OpenDrink WHEN a row is clicked SHOULD emit NavigateToDrink once`() = runTest {
        val viewModel = createLoadedViewModel()

        viewModel.onAction(MenuAction.OpenDrink(Create.ESPRESSO.id))

        viewModel.events.test {
            assertEquals(MenuEvent.NavigateToDrink(Create.ESPRESSO.id), awaitItem())
        }
    }

    @Test
    fun `ToggleFavorite WHEN drink is not favorite SHOULD ask to toggle it from not favorite`() = runTest {
        val viewModel = createLoadedViewModel()

        viewModel.onAction(MenuAction.ToggleFavorite(Create.ESPRESSO.id))
        advanceUntilIdle()

        assertEquals(listOf(ToggleFavoriteCall(Create.ESPRESSO.id, isFavoriteNow = false)), toggleFavorite.calls)
    }

    @Test
    fun `ToggleFavorite WHEN favorites emit the drink SHOULD show the heart on its row`() = runTest {
        val viewModel = createLoadedViewModel()
        val expected = content.copy(
            items = persistentListOf(matchaRow, espressoRow.copy(isFavorite = true), icedLatteRow),
        )

        viewModel.onAction(MenuAction.ToggleFavorite(Create.ESPRESSO.id))
        advanceUntilIdle()

        assertEquals(expected, viewModel.state.value)
    }

    @Test
    fun `ToggleFavorite WHEN saving fails SHOULD emit FAVORITE_UPDATE_FAILED message`() = runTest {
        toggleFavorite.failure = IllegalStateException("disk full")
        val viewModel = createLoadedViewModel()

        viewModel.onAction(MenuAction.ToggleFavorite(Create.ESPRESSO.id))
        advanceUntilIdle()

        viewModel.events.test {
            assertEquals(MenuEvent.ShowMessage(MenuMessage.FAVORITE_UPDATE_FAILED), awaitItem())
        }
    }

    @Test
    fun `Refresh WHEN pulled SHOULD load the menu again with force refresh`() = runTest {
        val viewModel = createLoadedViewModel()

        viewModel.onAction(MenuAction.Refresh)
        advanceUntilIdle()

        assertEquals(listOf(false, true), getMenu.calls)
    }

    @Test
    fun `Refresh WHEN it fails while content is shown SHOULD keep the content and stop refreshing`() = runTest {
        val viewModel = createLoadedViewModel()
        getMenu.failNext = MenuError.Network()

        viewModel.onAction(MenuAction.Refresh)
        advanceUntilIdle()

        assertEquals(content, viewModel.state.value)
    }

    @Test
    fun `Refresh WHEN it fails while content is shown SHOULD emit REFRESH_FAILED message`() = runTest {
        val viewModel = createLoadedViewModel()
        getMenu.failNext = MenuError.Network()

        viewModel.onAction(MenuAction.Refresh)
        advanceUntilIdle()

        viewModel.events.test {
            assertEquals(MenuEvent.ShowMessage(MenuMessage.REFRESH_FAILED), awaitItem())
        }
    }

    private fun createViewModel(): MenuViewModel = MenuViewModel(
        getMenu = getMenu,
        observeFavorites = observeFavorites,
        toggleFavorite = toggleFavorite,
        applyHappyHour = applyHappyHour,
        filterAndSort = filterAndSort,
        mapper = MenuStateMapper(),
        dispatchers = TestDispatcherProvider(mainDispatcherRule.testDispatcher),
    )

    private fun TestScope.createLoadedViewModel(): MenuViewModel = createViewModel().also { advanceUntilIdle() }
}
