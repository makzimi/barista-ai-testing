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
import ru.otus.barista.core.testing.Create
import ru.otus.barista.core.testing.FakeMenuRepository
import ru.otus.barista.core.testing.FavoriteCall
import ru.otus.barista.core.testing.FixedClock
import ru.otus.barista.core.testing.MainDispatcherRule
import ru.otus.barista.core.testing.TestDispatcherProvider
import ru.otus.barista.feature.menu.api.MenuError
import ru.otus.barista.feature.menu.domain.ApplyHappyHourUseCase
import ru.otus.barista.feature.menu.domain.FilterAndSortMenuUseCase
import ru.otus.barista.feature.menu.domain.GetMenuUseCase
import ru.otus.barista.feature.menu.domain.MenuSort
import ru.otus.barista.feature.menu.domain.ObserveFavoritesUseCase
import ru.otus.barista.feature.menu.domain.ToggleFavoriteUseCase
import java.time.LocalTime
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class MenuViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(StandardTestDispatcher())

    private val repository = FakeMenuRepository(listOf(Create.ESPRESSO, Create.MATCHA, Create.ICE_LATTE_SOLD_OUT))
    private val clock = FixedClock(time = LocalTime.of(12, 0))

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
    fun `opening WHEN repository has drinks SHOULD go from Loading to Content`() = runTest {
        val viewModel = createViewModel()

        viewModel.state.test {
            assertEquals(MenuState(status = MenuStatus.Loading), awaitItem())
            assertEquals(content, awaitItem())
        }
    }

    @Test
    fun `opening WHEN a drink has an active happy hour SHOULD show the discounted price and badge`() = runTest {
        repository.setDrinks(listOf(Create.CAPPUCCINO))
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
    fun `opening WHEN repository fails and cache is cold SHOULD show network error`() = runTest {
        repository.failNextGetMenu = MenuError.Network()
        val expected = MenuState(status = MenuStatus.Error(ErrorKind.NETWORK))

        val viewModel = createLoadedViewModel()

        assertEquals(expected, viewModel.state.value)
    }

    @Test
    fun `opening WHEN repository fails with an unexpected error SHOULD show unknown error`() = runTest {
        repository.failNextGetMenu = IllegalStateException("boom")
        val expected = MenuState(status = MenuStatus.Error(ErrorKind.UNKNOWN))

        val viewModel = createLoadedViewModel()

        assertEquals(expected, viewModel.state.value)
    }

    @Test
    fun `Retry WHEN the first load failed SHOULD show Content`() = runTest {
        repository.failNextGetMenu = MenuError.Network()
        val viewModel = createLoadedViewModel()

        viewModel.onAction(MenuAction.Retry)
        advanceUntilIdle()

        assertEquals(content, viewModel.state.value)
    }

    @Test
    fun `opening WHEN repository serves stale cache SHOULD show content with the stale banner`() = runTest {
        repository.failNextGetMenu = MenuError.Network()
        repository.staleOnFailure = true

        val viewModel = createLoadedViewModel()

        assertEquals(content.copy(isStale = true), viewModel.state.value)
    }

    @Test
    fun `ToggleFavoritesOnly WHEN nothing is favorite SHOULD show Empty NO_FAVORITES`() = runTest {
        val viewModel = createLoadedViewModel()
        val expected = MenuState(status = MenuStatus.Empty(EmptyReason.NO_FAVORITES), favoritesOnly = true)

        viewModel.onAction(MenuAction.ToggleFavoritesOnly)
        advanceUntilIdle()

        assertEquals(expected, viewModel.state.value)
    }

    @Test
    fun `Search WHEN query matches nothing SHOULD show Empty NOTHING_FOUND`() = runTest {
        val viewModel = createLoadedViewModel()
        val expected = MenuState(status = MenuStatus.Empty(EmptyReason.NOTHING_FOUND), query = "раф")

        viewModel.onAction(MenuAction.Search("раф"))
        advanceUntilIdle()

        assertEquals(expected, viewModel.state.value)
    }

    @Test
    fun `Search WHEN only 299 ms passed SHOULD update the query but not filter the list yet`() = runTest {
        val viewModel = createLoadedViewModel()

        viewModel.onAction(MenuAction.Search("матча"))
        advanceTimeBy(299)

        assertEquals(content.copy(query = "матча"), viewModel.state.value)
    }

    @Test
    fun `Search WHEN the 300 ms debounce passed SHOULD filter the list`() = runTest {
        val viewModel = createLoadedViewModel()
        val expected = content.copy(items = persistentListOf(matchaRow), query = "матча")

        viewModel.onAction(MenuAction.Search("матча"))
        advanceTimeBy(300)
        runCurrent()

        assertEquals(expected, viewModel.state.value)
    }

    @Test
    fun `SelectCategory WHEN TEA is selected SHOULD show only tea drinks`() = runTest {
        val viewModel = createLoadedViewModel()
        val expected = content.copy(items = persistentListOf(matchaRow), category = CategoryFilter.TEA)

        viewModel.onAction(MenuAction.SelectCategory(CategoryFilter.TEA))
        advanceUntilIdle()

        assertEquals(expected, viewModel.state.value)
    }

    @Test
    fun `ChangeSort WHEN sorting by price SHOULD show cheapest first and sold-out last`() = runTest {
        val viewModel = createLoadedViewModel()
        val expected = content.copy(
            items = persistentListOf(espressoRow, matchaRow, icedLatteRow),
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
    fun `ToggleFavorite WHEN drink is not favorite SHOULD save it as favorite`() = runTest {
        val viewModel = createLoadedViewModel()

        viewModel.onAction(MenuAction.ToggleFavorite(Create.ESPRESSO.id))
        advanceUntilIdle()

        assertEquals(listOf(FavoriteCall(Create.ESPRESSO.id, isFavorite = true)), repository.favoriteCalls)
    }

    @Test
    fun `ToggleFavorite WHEN drink is saved as favorite SHOULD show the heart on its row`() = runTest {
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
        repository.failSetFavorite = IllegalStateException("disk full")
        val viewModel = createLoadedViewModel()

        viewModel.onAction(MenuAction.ToggleFavorite(Create.ESPRESSO.id))
        advanceUntilIdle()

        viewModel.events.test {
            assertEquals(MenuEvent.ShowMessage(MenuMessage.FAVORITE_UPDATE_FAILED), awaitItem())
        }
    }

    @Test
    fun `Refresh WHEN pulled SHOULD force a refresh from the repository`() = runTest {
        val viewModel = createLoadedViewModel()

        viewModel.onAction(MenuAction.Refresh)
        advanceUntilIdle()

        assertEquals(listOf(false, true), repository.getMenuCalls)
    }

    @Test
    fun `Refresh WHEN it fails while content is shown SHOULD keep the content and stop refreshing`() = runTest {
        val viewModel = createLoadedViewModel()
        repository.failNextGetMenu = MenuError.Network()

        viewModel.onAction(MenuAction.Refresh)
        advanceUntilIdle()

        assertEquals(content, viewModel.state.value)
    }

    @Test
    fun `Refresh WHEN it fails while content is shown SHOULD emit REFRESH_FAILED message`() = runTest {
        val viewModel = createLoadedViewModel()
        repository.failNextGetMenu = MenuError.Network()

        viewModel.onAction(MenuAction.Refresh)
        advanceUntilIdle()

        viewModel.events.test {
            assertEquals(MenuEvent.ShowMessage(MenuMessage.REFRESH_FAILED), awaitItem())
        }
    }

    private fun createViewModel(): MenuViewModel = MenuViewModel(
        getMenu = GetMenuUseCase(repository),
        observeFavorites = ObserveFavoritesUseCase(repository),
        toggleFavorite = ToggleFavoriteUseCase(repository),
        applyHappyHour = ApplyHappyHourUseCase(clock),
        filterAndSort = FilterAndSortMenuUseCase(),
        mapper = MenuStateMapper(),
        dispatchers = TestDispatcherProvider(mainDispatcherRule.testDispatcher),
    )

    private fun TestScope.createLoadedViewModel(): MenuViewModel = createViewModel().also { advanceUntilIdle() }
}
