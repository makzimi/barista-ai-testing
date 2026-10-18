@file:Suppress("NoUnusedImports", "UnusedImports", "UnusedPrivateProperty", "UnusedPrivateMember")

package ru.otus.barista.app.workshop

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import ru.otus.barista.app.TestMenuDependencies
import ru.otus.barista.core.testing.Create
import ru.otus.barista.core.testing.FixedClock
import ru.otus.barista.core.testing.MainDispatcherRule
import ru.otus.barista.feature.menu.di.MenuGraph
import ru.otus.barista.feature.menu.presentation.MenuAction
import ru.otus.barista.feature.menu.presentation.MenuItemState
import ru.otus.barista.feature.menu.presentation.MenuStatus
import ru.otus.barista.feature.menu.presentation.MenuViewModel
import java.time.LocalTime
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Live-coding copy of `MenuFeatureIntegrationTest` (LIVE_CODING_SCRIPT Part 2.2): every layer glued together —
 * real ViewModel, use case impls, `DrinkMapper`, `MenuRepositoryImpl`, Retrofit + JSON — with only the fake backend
 * at the edge. It lives in `:app` because only the app may see `:data:impl` and `:domain:impl`.
 * Uncomment one region at a time (select it, `Cmd + /`) and run it from the gutter.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MenuFeatureIntegrationWorkshopTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(StandardTestDispatcher())

    private val clock = FixedClock(time = LocalTime.of(17, 0))
    private val dependencies = TestMenuDependencies(clock, mainDispatcherRule.testDispatcher)
    private val graph = MenuGraph(dependencies)

    // region Step 1 — real ViewModel, use cases, DrinkMapper, MenuRepositoryImpl + fake backend → badge from JSON
    // @Test
    // fun `opening WHEN flat white happy hour is active at 17_00 SHOULD show its discount badge`() = runTest {
    //     val expected = MenuItemState(
    //         id = Create.FLAT_WHITE.id,
    //         name = "Флэт уайт",
    //         description = "Двойной ристретто и бархатное молоко",
    //         price = "191.92 ₽",
    //         oldPrice = "239.90 ₽",
    //         discountLabel = "−20%",
    //         isNew = false,
    //         isSoldOut = false,
    //         isFavorite = false,
    //     )
    //
    //     val viewModel = createLoadedViewModel()
    //
    //     assertEquals(MenuStatus.Content, viewModel.state.value.status)
    //     assertEquals(expected, viewModel.state.value.items.first { it.id == Create.FLAT_WHITE.id })
    // }
    // endregion

    // region Step 2 — the backend fails on refresh → content stays, stale banner is shown
    // @Test
    // fun `Refresh WHEN backend fails after a successful load SHOULD keep the content and show the stale banner`() =
    //     runTest {
    //         val viewModel = createLoadedViewModel()
    //         val expected = viewModel.state.value.copy(isStale = true)
    //         dependencies.backend.setFailNextRequest(true)
    //
    //         viewModel.onAction(MenuAction.Refresh)
    //         advanceUntilIdle()
    //
    //         assertEquals(expected, viewModel.state.value)
    //     }
    // endregion

    private fun TestScope.createLoadedViewModel(): MenuViewModel = graph.createViewModel().also { advanceUntilIdle() }
}
