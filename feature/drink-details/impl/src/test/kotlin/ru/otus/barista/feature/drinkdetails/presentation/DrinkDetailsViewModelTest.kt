package ru.otus.barista.feature.drinkdetails.presentation

import app.cash.turbine.test
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import ru.otus.barista.core.testing.Create
import ru.otus.barista.core.testing.FakeMenuRepository
import ru.otus.barista.core.testing.FixedClock
import ru.otus.barista.core.testing.MainDispatcherRule
import ru.otus.barista.feature.drinkdetails.api.Size
import ru.otus.barista.feature.drinkdetails.domain.CalculatePriceUseCase
import ru.otus.barista.feature.drinkdetails.domain.ChangeAddOnQuantityUseCase
import ru.otus.barista.feature.drinkdetails.domain.GetDrinkDetailsUseCase
import ru.otus.barista.feature.drinkdetails.domain.ObserveIsFavoriteUseCase
import ru.otus.barista.feature.drinkdetails.domain.ToggleFavoriteUseCase
import ru.otus.barista.feature.menu.api.AddOn
import ru.otus.barista.feature.menu.api.DrinkId
import ru.otus.barista.feature.menu.api.MenuError
import java.time.LocalTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

@OptIn(ExperimentalCoroutinesApi::class)
class DrinkDetailsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(StandardTestDispatcher())

    private val repository = FakeMenuRepository()
    private val clock = FixedClock(time = LocalTime.of(12, 0))

    private val espressoContent = DrinkDetailsState(
        status = DrinkDetailsStatus.Content,
        name = "Эспрессо",
        description = "Классический крепкий эспрессо из смеси арабики",
        caffeineMg = 65,
        calories = 5,
        selectedSize = Size.S,
        addOns = persistentListOf(espressoExtraShot(quantity = 0)),
        totalPrice = "119.90 ₽",
        canAddToCart = true,
    )

    @Test
    fun `opening WHEN repository has the drink SHOULD go from Loading to Content`() = runTest {
        val viewModel = createViewModel(Create.ESPRESSO.id)

        viewModel.state.test {
            assertEquals(DrinkDetailsState(), awaitItem())
            assertEquals(espressoContent, awaitItem())
        }
    }

    @Test
    fun `opening WHEN happy hour is active SHOULD show discounted total and old price`() = runTest {
        val expected = DrinkDetailsState(
            status = DrinkDetailsStatus.Content,
            name = "Капучино",
            description = "Эспрессо с молоком и плотной молочной пеной",
            caffeineMg = 130,
            calories = 120,
            addOns = persistentListOf(
                addOnRow(Create.EXTRA_SHOT, price = "+60.00 ₽"),
                addOnRow(Create.SYRUP, price = "+40.00 ₽"),
                addOnRow(Create.OAT_MILK, price = "+70.00 ₽"),
            ),
            totalPrice = "186.92 ₽",
            oldTotalPrice = "219.90 ₽",
            discountPercent = 15,
            discountLabel = "−15%",
            canAddToCart = true,
        )

        val viewModel = createLoadedViewModel(Create.CAPPUCCINO.id)

        assertEquals(expected, viewModel.state.value)
    }

    @Test
    fun `SelectSize WHEN size L is selected SHOULD recalculate the total price`() = runTest {
        val viewModel = createLoadedViewModel(Create.ESPRESSO.id)
        val expected = espressoContent.copy(selectedSize = Size.L, totalPrice = "179.85 ₽")

        viewModel.onAction(DrinkDetailsAction.SelectSize(Size.L))
        advanceUntilIdle()

        assertEquals(expected, viewModel.state.value)
    }

    @Test
    fun `IncrementAddOn WHEN pressed 4 times SHOULD stop at 3 and mark the row as limit reached`() = runTest {
        val viewModel = createLoadedViewModel(Create.ESPRESSO.id)
        val expected = espressoContent.copy(
            addOns = persistentListOf(espressoExtraShot(quantity = 3, limitReached = true)),
            totalPrice = "299.90 ₽",
        )

        repeat(4) { viewModel.onAction(DrinkDetailsAction.IncrementAddOn(Create.EXTRA_SHOT.id)) }
        advanceUntilIdle()

        assertEquals(expected, viewModel.state.value)
    }

    @Test
    fun `DecrementAddOn WHEN quantity is 2 SHOULD decrease quantity and price`() = runTest {
        val viewModel = createLoadedViewModel(Create.ESPRESSO.id)
        repeat(2) { viewModel.onAction(DrinkDetailsAction.IncrementAddOn(Create.EXTRA_SHOT.id)) }
        val expected = espressoContent.copy(
            addOns = persistentListOf(espressoExtraShot(quantity = 1)),
            totalPrice = "179.90 ₽",
        )

        viewModel.onAction(DrinkDetailsAction.DecrementAddOn(Create.EXTRA_SHOT.id))
        advanceUntilIdle()

        assertEquals(expected, viewModel.state.value)
    }

    @Test
    fun `opening WHEN drink is sold out SHOULD not allow adding to cart`() = runTest {
        val viewModel = createLoadedViewModel(Create.ICE_LATTE_SOLD_OUT.id)

        assertFalse(viewModel.state.value.canAddToCart)
    }

    @Test
    fun `AddToCart WHEN drink is available SHOULD emit AddedToCart with the drink name`() = runTest {
        val viewModel = createLoadedViewModel(Create.ESPRESSO.id)

        viewModel.onAction(DrinkDetailsAction.AddToCart)

        viewModel.events.test {
            assertEquals(DrinkDetailsEvent.AddedToCart("Эспрессо"), awaitItem())
        }
    }

    @Test
    fun `AddToCart WHEN drink is sold out SHOULD emit nothing`() = runTest {
        val viewModel = createLoadedViewModel(Create.ICE_LATTE_SOLD_OUT.id)

        viewModel.onAction(DrinkDetailsAction.AddToCart)

        viewModel.events.test {
            expectNoEvents()
        }
    }

    @Test
    fun `ToggleFavorite WHEN drink is not favorite SHOULD store it and show it as favorite`() = runTest {
        val viewModel = createLoadedViewModel(Create.ESPRESSO.id)

        viewModel.onAction(DrinkDetailsAction.ToggleFavorite)
        advanceUntilIdle()

        assertEquals(setOf(Create.ESPRESSO.id), repository.favorites.value)
        assertEquals(espressoContent.copy(isFavorite = true), viewModel.state.value)
    }

    @Test
    fun `ToggleFavorite WHEN saving fails SHOULD emit FAVORITE_UPDATE_FAILED message`() = runTest {
        repository.failSetFavorite = IllegalStateException("disk full")
        val viewModel = createLoadedViewModel(Create.ESPRESSO.id)

        viewModel.onAction(DrinkDetailsAction.ToggleFavorite)
        advanceUntilIdle()

        viewModel.events.test {
            assertEquals(DrinkDetailsEvent.ShowMessage(DrinkDetailsMessage.FAVORITE_UPDATE_FAILED), awaitItem())
        }
    }

    @Test
    fun `opening WHEN repository fails with network error SHOULD show network error`() = runTest {
        repository.failNextGetDrink = MenuError.Network()
        val expected = DrinkDetailsState(status = DrinkDetailsStatus.Error(DrinkDetailsErrorKind.NETWORK))

        val viewModel = createLoadedViewModel(Create.ESPRESSO.id)

        assertEquals(expected, viewModel.state.value)
    }

    @Test
    fun `opening WHEN drink does not exist SHOULD show not found error`() = runTest {
        val expected = DrinkDetailsState(status = DrinkDetailsStatus.Error(DrinkDetailsErrorKind.NOT_FOUND))

        val viewModel = createLoadedViewModel(DrinkId("unknown"))

        assertEquals(expected, viewModel.state.value)
    }

    @Test
    fun `Retry WHEN previous load failed SHOULD show Content`() = runTest {
        repository.failNextGetDrink = MenuError.Network()
        val viewModel = createLoadedViewModel(Create.ESPRESSO.id)

        viewModel.onAction(DrinkDetailsAction.Retry)
        advanceUntilIdle()

        assertEquals(espressoContent, viewModel.state.value)
    }

    @Test
    fun `Back WHEN pressed SHOULD emit NavigateBack`() = runTest {
        val viewModel = createLoadedViewModel(Create.ESPRESSO.id)

        viewModel.onAction(DrinkDetailsAction.Back)

        viewModel.events.test {
            assertEquals(DrinkDetailsEvent.NavigateBack, awaitItem())
        }
    }

    private fun createViewModel(id: DrinkId): DrinkDetailsViewModel = DrinkDetailsViewModel(
        drinkId = id,
        getDrinkDetails = GetDrinkDetailsUseCase(repository, clock),
        observeIsFavorite = ObserveIsFavoriteUseCase(repository),
        toggleFavorite = ToggleFavoriteUseCase(repository),
        calculatePrice = CalculatePriceUseCase(),
        changeAddOnQuantity = ChangeAddOnQuantityUseCase(),
        mapper = DrinkDetailsStateMapper(),
    )

    private fun TestScope.createLoadedViewModel(id: DrinkId): DrinkDetailsViewModel =
        createViewModel(id).also { advanceUntilIdle() }

    private fun espressoExtraShot(quantity: Int, limitReached: Boolean = false) =
        addOnRow(Create.EXTRA_SHOT, price = "+60.00 ₽", quantity = quantity, limitReached = limitReached)

    private fun addOnRow(addOn: AddOn, price: String, quantity: Int = 0, limitReached: Boolean = false) =
        AddOnRowState(id = addOn.id, name = addOn.name, price = price, quantity = quantity, limitReached = limitReached)
}
