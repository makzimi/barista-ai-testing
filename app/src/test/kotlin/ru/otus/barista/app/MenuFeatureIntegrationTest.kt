package ru.otus.barista.app

import app.cash.turbine.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import ru.otus.barista.core.designsystem.BaristaImages
import ru.otus.barista.core.testing.Create
import ru.otus.barista.core.testing.FixedClock
import ru.otus.barista.core.testing.MainDispatcherRule
import ru.otus.barista.feature.menu.di.MenuGraph
import ru.otus.barista.feature.menu.presentation.ErrorKind
import ru.otus.barista.feature.menu.presentation.MenuAction
import ru.otus.barista.feature.menu.presentation.MenuItemState
import ru.otus.barista.feature.menu.presentation.MenuStatus
import ru.otus.barista.feature.menu.presentation.MenuViewModel
import java.time.LocalTime
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * "Sociable" test of the whole menu stack, in `:app` because only the app may see every `impl`:
 * real [MenuGraph] → ViewModel → use case impls → `DrinkMapper` → `MenuRepositoryImpl` → Retrofit + JSON,
 * with only the backend (the app's fake backend interceptor), the favorites file, the clock and the dispatchers faked.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MenuFeatureIntegrationTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(StandardTestDispatcher())

    private val clock = FixedClock(time = LocalTime.of(17, 0))
    private val dependencies = TestMenuDependencies(clock, mainDispatcherRule.testDispatcher)
    private val graph = MenuGraph(dependencies)

    @Test
    fun `opening WHEN backend menu has happy hours active at 17_00 SHOULD show discount badges from the JSON`() =
        runTest {
            val expected = listOf(
                MenuItemState(
                    id = Create.CAPPUCCINO.id,
                    imageRes = BaristaImages.drink("cappuccino"),
                    name = "Капучино",
                    description = "Эспрессо с молоком и плотной молочной пеной",
                    price = "186.92 ₽",
                    oldPrice = "219.90 ₽",
                    discountLabel = "−15%",
                    isNew = false,
                    isSoldOut = false,
                    isFavorite = false,
                ),
                MenuItemState(
                    id = Create.FLAT_WHITE.id,
                    imageRes = BaristaImages.drink("flat_white"),
                    name = "Флэт уайт",
                    description = "Двойной ристретто и бархатное молоко",
                    price = "191.92 ₽",
                    oldPrice = "239.90 ₽",
                    discountLabel = "−20%",
                    isNew = false,
                    isSoldOut = false,
                    isFavorite = false,
                ),
            )

            val viewModel = createLoadedViewModel()

            assertEquals(MenuStatus.Content, viewModel.state.value.status)
            assertEquals(expected, viewModel.state.value.items.filter { it.discountLabel != null })
        }

    @Test
    fun `opening WHEN backend returns the full menu SHOULD show 12 drinks with the sold-out one last`() = runTest {
        val viewModel = createLoadedViewModel()

        assertEquals(12, viewModel.state.value.items.size)
        assertEquals(Create.ICE_LATTE_SOLD_OUT.id, viewModel.state.value.items.last().id)
    }

    @Test
    fun `Refresh WHEN backend fails after a successful load SHOULD keep the content and show the stale banner`() =
        runTest {
            val viewModel = createLoadedViewModel()
            val expected = viewModel.state.value.copy(isStale = true)
            dependencies.backend.setFailNextRequest(true)

            viewModel.onAction(MenuAction.Refresh)
            advanceUntilIdle()

            assertEquals(expected, viewModel.state.value)
        }

    @Test
    fun `Refresh WHEN backend fails after a successful load SHOULD not emit any message`() = runTest {
        val viewModel = createLoadedViewModel()
        dependencies.backend.setFailNextRequest(true)

        viewModel.onAction(MenuAction.Refresh)
        advanceUntilIdle()

        viewModel.events.test {
            expectNoEvents()
        }
    }

    @Test
    fun `opening WHEN backend fails and nothing is cached SHOULD show network error`() = runTest {
        dependencies.backend.setFailNextRequest(true)

        val viewModel = createLoadedViewModel()

        assertEquals(MenuStatus.Error(ErrorKind.NETWORK, BaristaImages.error), viewModel.state.value.status)
    }

    @Test
    fun `Retry WHEN backend is back after a failure SHOULD show content and call GET menu twice`() = runTest {
        dependencies.backend.setFailNextRequest(true)
        val viewModel = createLoadedViewModel()

        viewModel.onAction(MenuAction.Retry)
        advanceUntilIdle()

        assertEquals(MenuStatus.Content, viewModel.state.value.status)
        assertEquals(listOf("/menu", "/menu"), dependencies.requests)
    }

    @Test
    fun `ToggleFavorite WHEN a drink is tapped SHOULD store it on disk and show the heart`() = runTest {
        val viewModel = createLoadedViewModel()

        viewModel.onAction(MenuAction.ToggleFavorite(Create.MATCHA.id))
        advanceUntilIdle()

        assertEquals(setOf("matcha_latte"), dependencies.storedFavoriteIds())
        assertEquals(listOf(Create.MATCHA.id), viewModel.state.value.items.filter { it.isFavorite }.map { it.id })
    }

    @Test
    fun `ToggleFavorite WHEN tapped twice SHOULD remove the drink from favorites`() = runTest {
        val viewModel = createLoadedViewModel()
        viewModel.onAction(MenuAction.ToggleFavorite(Create.MATCHA.id))
        advanceUntilIdle()

        viewModel.onAction(MenuAction.ToggleFavorite(Create.MATCHA.id))
        advanceUntilIdle()

        assertEquals(emptySet(), dependencies.storedFavoriteIds())
        assertEquals(emptyList(), viewModel.state.value.items.filter { it.isFavorite })
    }

    private fun TestScope.createLoadedViewModel(): MenuViewModel = graph.createViewModel().also { advanceUntilIdle() }
}
