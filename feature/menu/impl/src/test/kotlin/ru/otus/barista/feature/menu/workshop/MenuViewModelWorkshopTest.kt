@file:Suppress("NoUnusedImports", "UnusedImports", "UnusedPrivateProperty", "UnusedPrivateMember")

package ru.otus.barista.feature.menu.workshop

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
import ru.otus.barista.core.testing.FixedClock
import ru.otus.barista.core.testing.MainDispatcherRule
import ru.otus.barista.core.testing.TestDispatcherProvider
import ru.otus.barista.feature.menu.api.MenuError
import ru.otus.barista.feature.menu.domain.ApplyHappyHourUseCase
import ru.otus.barista.feature.menu.domain.FilterAndSortMenuUseCase
import ru.otus.barista.feature.menu.domain.GetMenuUseCase
import ru.otus.barista.feature.menu.domain.ObserveFavoritesUseCase
import ru.otus.barista.feature.menu.domain.ToggleFavoriteUseCase
import ru.otus.barista.feature.menu.presentation.EmptyReason
import ru.otus.barista.feature.menu.presentation.ErrorKind
import ru.otus.barista.feature.menu.presentation.MenuAction
import ru.otus.barista.feature.menu.presentation.MenuEvent
import ru.otus.barista.feature.menu.presentation.MenuItemState
import ru.otus.barista.feature.menu.presentation.MenuState
import ru.otus.barista.feature.menu.presentation.MenuStateMapper
import ru.otus.barista.feature.menu.presentation.MenuStatus
import ru.otus.barista.feature.menu.presentation.MenuViewModel
import java.time.LocalTime
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Live-coding copy of `MenuViewModelTest` (LIVE_CODING_SCRIPT Part 1.3): the ViewModel as a state machine.
 * Uncomment one region at a time (select it, `Cmd + /`) and run it from the gutter.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MenuViewModelWorkshopTest {

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

    // region Step 1 — Loading → Content (Turbine `state.test { }` + MainDispatcherRule)
    // @Test
    // fun `opening WHEN repository has drinks SHOULD go from Loading to Content`() = runTest {
    //     val viewModel = createViewModel()
    //
    //     viewModel.state.test {
    //         assertEquals(MenuState(status = MenuStatus.Loading), awaitItem())
    //         assertEquals(content, awaitItem())
    //     }
    // }
    // endregion

    // region Step 2 — favorites filter on, nothing is favorite → Empty(NO_FAVORITES)
    // @Test
    // fun `ToggleFavoritesOnly WHEN nothing is favorite SHOULD show Empty NO_FAVORITES`() = runTest {
    //     val viewModel = createLoadedViewModel()
    //     val expected = MenuState(status = MenuStatus.Empty(EmptyReason.NO_FAVORITES), favoritesOnly = true)
    //
    //     viewModel.onAction(MenuAction.ToggleFavoritesOnly)
    //     advanceUntilIdle()
    //
    //     assertEquals(expected, viewModel.state.value)
    // }
    // endregion

    // region Step 3 — repository error with a cold cache → Error, then Retry → Content
    // @Test
    // fun `opening WHEN repository fails and cache is cold SHOULD show network error`() = runTest {
    //     repository.failNextGetMenu = MenuError.Network()
    //     val expected = MenuState(status = MenuStatus.Error(ErrorKind.NETWORK))
    //
    //     val viewModel = createLoadedViewModel()
    //
    //     assertEquals(expected, viewModel.state.value)
    // }
    //
    // @Test
    // fun `Retry WHEN the first load failed SHOULD show Content`() = runTest {
    //     repository.failNextGetMenu = MenuError.Network()
    //     val viewModel = createLoadedViewModel()
    //
    //     viewModel.onAction(MenuAction.Retry)
    //     advanceUntilIdle()
    //
    //     assertEquals(content, viewModel.state.value)
    // }
    // endregion

    // region Step 4 — search debounce on virtual time: 299 ms nothing, 300 ms filtered
    // @Test
    // fun `Search WHEN only 299 ms passed SHOULD update the query but not filter the list yet`() = runTest {
    //     val viewModel = createLoadedViewModel()
    //
    //     viewModel.onAction(MenuAction.Search("матча"))
    //     advanceTimeBy(299)
    //
    //     assertEquals(content.copy(query = "матча"), viewModel.state.value)
    // }
    //
    // @Test
    // fun `Search WHEN the 300 ms debounce passed SHOULD filter the list`() = runTest {
    //     val viewModel = createLoadedViewModel()
    //     val expected = content.copy(items = persistentListOf(matchaRow), query = "матча")
    //
    //     viewModel.onAction(MenuAction.Search("матча"))
    //     advanceTimeBy(299)
    //     advanceTimeBy(1)
    //     runCurrent()
    //
    //     assertEquals(expected, viewModel.state.value)
    // }
    // endregion

    // region Step 5 — OpenDrink → one-shot NavigateToDrink event
    // @Test
    // fun `OpenDrink WHEN a row is clicked SHOULD emit NavigateToDrink once`() = runTest {
    //     val viewModel = createLoadedViewModel()
    //
    //     viewModel.onAction(MenuAction.OpenDrink(Create.ESPRESSO.id))
    //
    //     viewModel.events.test {
    //         assertEquals(MenuEvent.NavigateToDrink(Create.ESPRESSO.id), awaitItem())
    //     }
    // }
    // endregion

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
