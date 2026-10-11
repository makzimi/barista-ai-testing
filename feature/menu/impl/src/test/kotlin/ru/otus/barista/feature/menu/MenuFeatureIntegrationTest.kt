package ru.otus.barista.feature.menu

import app.cash.turbine.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import ru.otus.barista.core.testing.Create
import ru.otus.barista.core.testing.FixedClock
import ru.otus.barista.core.testing.MainDispatcherRule
import ru.otus.barista.core.testing.TestDispatcherProvider
import ru.otus.barista.feature.menu.data.FakeFavoritesLocalDataSource
import ru.otus.barista.feature.menu.data.FakeMenuApi
import ru.otus.barista.feature.menu.di.MenuGraph
import ru.otus.barista.feature.menu.presentation.ErrorKind
import ru.otus.barista.feature.menu.presentation.MenuAction
import ru.otus.barista.feature.menu.presentation.MenuItemState
import ru.otus.barista.feature.menu.presentation.MenuStatus
import ru.otus.barista.feature.menu.presentation.MenuViewModel
import java.io.IOException
import java.time.LocalTime
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * "Sociable" test of the whole menu feature: real [MenuGraph] → ViewModel → use cases → `MenuRepositoryImpl` →
 * DTO mapping, with only the backend ([FakeMenuApi]), the favorites storage, the clock and the dispatchers faked.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MenuFeatureIntegrationTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(StandardTestDispatcher())

    private val api = FakeMenuApi()
    private val favorites = FakeFavoritesLocalDataSource()
    private val clock = FixedClock(time = LocalTime.of(17, 0))
    private val graph = MenuGraph(
        TestMenuDependencies(
            api = api,
            favorites = favorites,
            clock = clock,
            dispatchers = TestDispatcherProvider(mainDispatcherRule.testDispatcher),
        ),
    )

    @Test
    fun `opening WHEN backend menu has happy hours active at 17_00 SHOULD show discount badges from the DTOs`() =
        runTest {
            val expected = listOf(
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
                MenuItemState(
                    id = Create.FLAT_WHITE.id,
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
            api.failNext = IOException("backend is down")

            viewModel.onAction(MenuAction.Refresh)
            advanceUntilIdle()

            assertEquals(expected, viewModel.state.value)
        }

    @Test
    fun `Refresh WHEN backend fails after a successful load SHOULD not emit any message`() = runTest {
        val viewModel = createLoadedViewModel()
        api.failNext = IOException("backend is down")

        viewModel.onAction(MenuAction.Refresh)
        advanceUntilIdle()

        viewModel.events.test {
            expectNoEvents()
        }
    }

    @Test
    fun `opening WHEN backend fails and nothing is cached SHOULD show network error`() = runTest {
        api.failNext = IOException("backend is down")

        val viewModel = createLoadedViewModel()

        assertEquals(MenuStatus.Error(ErrorKind.NETWORK), viewModel.state.value.status)
    }

    @Test
    fun `Retry WHEN backend is back after a failure SHOULD show content and call GET menu twice`() = runTest {
        api.failNext = IOException("backend is down")
        val viewModel = createLoadedViewModel()

        viewModel.onAction(MenuAction.Retry)
        advanceUntilIdle()

        assertEquals(MenuStatus.Content, viewModel.state.value.status)
        assertEquals(listOf("menu", "menu"), api.calls)
    }

    @Test
    fun `ToggleFavorite WHEN a drink is tapped SHOULD store it through the repository and show the heart`() = runTest {
        val viewModel = createLoadedViewModel()

        viewModel.onAction(MenuAction.ToggleFavorite(Create.MATCHA.id))
        advanceUntilIdle()

        assertEquals(setOf(Create.MATCHA.id), favorites.favorites.value)
        assertEquals(listOf(Create.MATCHA.id), viewModel.state.value.items.filter { it.isFavorite }.map { it.id })
    }

    @Test
    fun `ToggleFavorite WHEN tapped twice SHOULD remove the drink from favorites`() = runTest {
        val viewModel = createLoadedViewModel()
        viewModel.onAction(MenuAction.ToggleFavorite(Create.MATCHA.id))
        advanceUntilIdle()

        viewModel.onAction(MenuAction.ToggleFavorite(Create.MATCHA.id))
        advanceUntilIdle()

        assertEquals(emptySet(), favorites.favorites.value)
        assertEquals(emptyList(), viewModel.state.value.items.filter { it.isFavorite })
    }

    private fun TestScope.createLoadedViewModel(): MenuViewModel = graph.createViewModel().also { advanceUntilIdle() }
}
