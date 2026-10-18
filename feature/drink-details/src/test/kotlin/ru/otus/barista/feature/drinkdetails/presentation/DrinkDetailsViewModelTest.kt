package ru.otus.barista.feature.drinkdetails.presentation

import app.cash.turbine.test
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import ru.otus.barista.core.common.Money
import ru.otus.barista.core.testing.Create
import ru.otus.barista.core.testing.FakeCalculatePriceUseCase
import ru.otus.barista.core.testing.FakeChangeAddOnQuantityUseCase
import ru.otus.barista.core.testing.FakeGetDrinkDetailsUseCase
import ru.otus.barista.core.testing.FakeObserveIsFavoriteUseCase
import ru.otus.barista.core.testing.FakeToggleFavoriteUseCase
import ru.otus.barista.core.testing.MainDispatcherRule
import ru.otus.barista.domain.model.AddOn
import ru.otus.barista.domain.model.DrinkConfiguration
import ru.otus.barista.domain.model.DrinkDetails
import ru.otus.barista.domain.model.DrinkId
import ru.otus.barista.domain.model.MenuError
import ru.otus.barista.domain.model.PriceBreakdown
import ru.otus.barista.domain.model.Size
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

/**
 * The ViewModel against fakes of its use case interfaces. Price formula and add-on limits are the use cases' job
 * (tested in `:domain:impl`); here we check that the ViewModel asks them at the right moment and renders the answer.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DrinkDetailsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(StandardTestDispatcher())

    private val favorites = MutableStateFlow<Set<DrinkId>>(emptySet())
    private val getDrinkDetails = FakeGetDrinkDetailsUseCase()
    private val observeIsFavorite = FakeObserveIsFavoriteUseCase(favorites)
    private val toggleFavorite = FakeToggleFavoriteUseCase(favorites)
    private val calculatePrice = FakeCalculatePriceUseCase()
    private val changeAddOnQuantity = FakeChangeAddOnQuantityUseCase()

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
    fun `opening WHEN the drink loads SHOULD go from Loading to Content`() = runTest {
        val viewModel = createViewModel(Create.ESPRESSO.id)

        viewModel.state.test {
            assertEquals(DrinkDetailsState(), awaitItem())
            assertEquals(espressoContent, awaitItem())
        }
    }

    @Test
    fun `opening WHEN the price has a discount SHOULD show discounted total and old price`() = runTest {
        getDrinkDetails.setDetails(DrinkDetails(drink = Create.CAPPUCCINO, discountPercent = 15))
        calculatePrice.answer = { _, _ ->
            PriceBreakdown(subtotal = Money(21990), discountPercent = 15, total = Money(18692))
        }
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
    fun `SelectSize WHEN size L is selected SHOULD recalculate the price for size L and show it`() = runTest {
        calculatePrice.answer = { details, configuration ->
            val total = if (configuration.size == Size.L) Money(17985) else details.drink.basePrice
            PriceBreakdown(subtotal = total, discountPercent = 0, total = total)
        }
        val viewModel = createLoadedViewModel(Create.ESPRESSO.id)
        val expected = espressoContent.copy(selectedSize = Size.L, totalPrice = "179.85 ₽")

        viewModel.onAction(DrinkDetailsAction.SelectSize(Size.L))
        advanceUntilIdle()

        assertEquals(expected, viewModel.state.value)
        assertEquals(DrinkConfiguration(size = Size.L), calculatePrice.calls.last())
    }

    @Test
    fun `IncrementAddOn WHEN the use case caps the quantity at 3 SHOULD mark the row as limit reached`() = runTest {
        changeAddOnQuantity.answer = { configuration, addOnId, delta ->
            val quantity = (configuration.quantityOf(addOnId) + delta).coerceAtMost(3)
            configuration.copy(addOns = configuration.addOns + (addOnId to quantity))
        }
        val viewModel = createLoadedViewModel(Create.ESPRESSO.id)
        val expected = espressoContent.copy(
            addOns = persistentListOf(espressoExtraShot(quantity = 3, limitReached = true)),
        )

        repeat(4) { viewModel.onAction(DrinkDetailsAction.IncrementAddOn(Create.EXTRA_SHOT.id)) }
        advanceUntilIdle()

        assertEquals(expected, viewModel.state.value)
        assertEquals(DrinkConfiguration(addOns = mapOf(Create.EXTRA_SHOT.id to 3)), calculatePrice.calls.last())
    }

    @Test
    fun `DecrementAddOn WHEN pressed SHOULD ask the use case for delta -1 and show the new quantity`() = runTest {
        val viewModel = createLoadedViewModel(Create.ESPRESSO.id)
        repeat(2) { viewModel.onAction(DrinkDetailsAction.IncrementAddOn(Create.EXTRA_SHOT.id)) }
        val expected = espressoContent.copy(addOns = persistentListOf(espressoExtraShot(quantity = 1)))

        viewModel.onAction(DrinkDetailsAction.DecrementAddOn(Create.EXTRA_SHOT.id))
        advanceUntilIdle()

        assertEquals(expected, viewModel.state.value)
        assertEquals(Create.EXTRA_SHOT.id to -1, changeAddOnQuantity.calls.last())
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
    fun `ToggleFavorite WHEN drink is not favorite SHOULD toggle it and show it as favorite`() = runTest {
        val viewModel = createLoadedViewModel(Create.ESPRESSO.id)

        viewModel.onAction(DrinkDetailsAction.ToggleFavorite)
        advanceUntilIdle()

        assertEquals(setOf(Create.ESPRESSO.id), favorites.value)
        assertEquals(espressoContent.copy(isFavorite = true), viewModel.state.value)
    }

    @Test
    fun `ToggleFavorite WHEN saving fails SHOULD emit FAVORITE_UPDATE_FAILED message`() = runTest {
        toggleFavorite.failure = IllegalStateException("disk full")
        val viewModel = createLoadedViewModel(Create.ESPRESSO.id)

        viewModel.onAction(DrinkDetailsAction.ToggleFavorite)
        advanceUntilIdle()

        viewModel.events.test {
            assertEquals(DrinkDetailsEvent.ShowMessage(DrinkDetailsMessage.FAVORITE_UPDATE_FAILED), awaitItem())
        }
    }

    @Test
    fun `opening WHEN loading fails with network error SHOULD show network error`() = runTest {
        getDrinkDetails.failNext = MenuError.Network()
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
        getDrinkDetails.failNext = MenuError.Network()
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
        getDrinkDetails = getDrinkDetails,
        observeIsFavorite = observeIsFavorite,
        toggleFavorite = toggleFavorite,
        calculatePrice = calculatePrice,
        changeAddOnQuantity = changeAddOnQuantity,
        mapper = DrinkDetailsStateMapper(),
    )

    private fun TestScope.createLoadedViewModel(id: DrinkId): DrinkDetailsViewModel =
        createViewModel(id).also { advanceUntilIdle() }

    private fun espressoExtraShot(quantity: Int, limitReached: Boolean = false) =
        addOnRow(Create.EXTRA_SHOT, price = "+60.00 ₽", quantity = quantity, limitReached = limitReached)

    private fun addOnRow(addOn: AddOn, price: String, quantity: Int = 0, limitReached: Boolean = false) =
        AddOnRowState(id = addOn.id, name = addOn.name, price = price, quantity = quantity, limitReached = limitReached)
}
