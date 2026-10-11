package ru.otus.barista.feature.drinkdetails.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import ru.otus.barista.core.designsystem.DesignSystemTestTags
import ru.otus.barista.feature.drinkdetails.api.Size
import ru.otus.barista.feature.drinkdetails.presentation.DrinkDetailsAction
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class DrinkDetailsScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val actions = mutableListOf<DrinkDetailsAction>()

    @Test
    fun `DrinkDetailsScreen WHEN drink is sold out SHOULD disable the add-to-cart button`() {
        val state = DrinkDetailsPreviewData.icedLatteSoldOut

        composeRule.setContent { DrinkDetailsScreen(state = state, onAction = actions::add) }

        composeRule.onNodeWithTag(DrinkDetailsTestTags.ADD_TO_CART).assertIsNotEnabled()
    }

    @Test
    fun `DrinkDetailsScreen WHEN drink is available SHOULD enable the add-to-cart button`() {
        val state = DrinkDetailsPreviewData.cappuccino

        composeRule.setContent { DrinkDetailsScreen(state = state, onAction = actions::add) }

        composeRule.onNodeWithTag(DrinkDetailsTestTags.ADD_TO_CART).assertIsEnabled()
    }

    @Test
    fun `DrinkDetailsScreen WHEN size L clicked SHOULD send SelectSize L`() {
        val state = DrinkDetailsPreviewData.cappuccino
        composeRule.setContent { DrinkDetailsScreen(state = state, onAction = actions::add) }

        composeRule.onNodeWithTag(DrinkDetailsTestTags.size(Size.L)).performScrollTo().performClick()

        assertEquals(listOf<DrinkDetailsAction>(DrinkDetailsAction.SelectSize(Size.L)), actions)
    }

    @Test
    fun `DrinkDetailsScreen WHEN add-on plus clicked SHOULD send IncrementAddOn for that add-on`() {
        val addOn = DrinkDetailsPreviewData.extraShot
        val state = DrinkDetailsPreviewData.cappuccino
        composeRule.setContent { DrinkDetailsScreen(state = state, onAction = actions::add) }

        composeRule.onNodeWithTag(DrinkDetailsTestTags.addOnIncrement(addOn.id)).performScrollTo().performClick()

        assertEquals(listOf<DrinkDetailsAction>(DrinkDetailsAction.IncrementAddOn(addOn.id)), actions)
    }

    @Test
    fun `DrinkDetailsScreen WHEN add-on limit reached SHOULD show Максимум 3 and disable plus`() {
        val state = DrinkDetailsPreviewData.cappuccino
        val syrup = DrinkDetailsPreviewData.syrup
        val inSyrupRow = hasAnyAncestor(hasTestTag(DrinkDetailsTestTags.addOnRow(syrup.id)))

        composeRule.setContent { DrinkDetailsScreen(state = state, onAction = actions::add) }

        composeRule
            .onNode(hasText("Максимум 3") and inSyrupRow)
            .performScrollTo()
            .assertIsDisplayed()
        composeRule.onNodeWithTag(DrinkDetailsTestTags.addOnIncrement(syrup.id)).assertIsNotEnabled()
    }

    @Test
    fun `DrinkDetailsScreen WHEN state has a total SHOULD show that total in the cart bar`() {
        val state = DrinkDetailsPreviewData.cappuccino.copy(totalPrice = "512.00 ₽")
        val inCartBar = hasAnyAncestor(hasTestTag(DrinkDetailsTestTags.TOTAL_PRICE))

        composeRule.setContent { DrinkDetailsScreen(state = state, onAction = actions::add) }

        composeRule.onNode(hasTestTag(DesignSystemTestTags.PRICE) and inCartBar).assertTextEquals("512.00 ₽")
    }

    @Test
    fun `DrinkDetailsScreen WHEN favorite clicked SHOULD send ToggleFavorite`() {
        val state = DrinkDetailsPreviewData.cappuccino
        composeRule.setContent { DrinkDetailsScreen(state = state, onAction = actions::add) }

        composeRule.onNodeWithTag(DrinkDetailsTestTags.FAVORITE).performClick()

        assertEquals(listOf<DrinkDetailsAction>(DrinkDetailsAction.ToggleFavorite), actions)
    }
}
