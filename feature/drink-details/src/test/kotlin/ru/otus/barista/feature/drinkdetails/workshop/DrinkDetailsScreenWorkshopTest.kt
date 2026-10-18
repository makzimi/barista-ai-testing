@file:Suppress("NoUnusedImports", "UnusedImports", "UnusedPrivateProperty")

package ru.otus.barista.feature.drinkdetails.workshop

import androidx.compose.ui.test.assertIsNotEnabled
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
import ru.otus.barista.domain.model.Size
import ru.otus.barista.feature.drinkdetails.presentation.DrinkDetailsAction
import ru.otus.barista.feature.drinkdetails.ui.DrinkDetailsPreviewData
import ru.otus.barista.feature.drinkdetails.ui.DrinkDetailsScreen
import ru.otus.barista.feature.drinkdetails.ui.DrinkDetailsTestTags
import kotlin.test.assertEquals

/**
 * Live-coding copy of `ui/DrinkDetailsScreenTest` (LIVE_CODING_SCRIPT Part 3.2).
 * Uncomment one region at a time (select it, `Cmd + /`) and run it from the gutter.
 * The same assertions would run on an emulator from `androidTest`; `maestro/menu-to-details.yaml` is the E2E flow.
 */
// region Step 1 — also uncomment the runner (JUnit rejects a @RunWith class that has no @Test yet)
// @RunWith(RobolectricTestRunner::class)
// endregion
@Config(sdk = [34])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class DrinkDetailsScreenWorkshopTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val actions = mutableListOf<DrinkDetailsAction>()

    // region Step 1 — sold out: the "add to cart" button is disabled
    // @Test
    // fun `DrinkDetailsScreen WHEN drink is sold out SHOULD disable the add-to-cart button`() {
    //     val state = DrinkDetailsPreviewData.icedLatteSoldOut
    //
    //     composeRule.setContent { DrinkDetailsScreen(state = state, onAction = actions::add) }
    //
    //     composeRule.onNodeWithTag(DrinkDetailsTestTags.ADD_TO_CART).assertIsNotEnabled()
    // }
    // endregion

    // region Step 2 — taps become Actions: size L, "+" on an add-on
    // @Test
    // fun `DrinkDetailsScreen WHEN size L clicked SHOULD send SelectSize L`() {
    //     val state = DrinkDetailsPreviewData.cappuccino
    //     composeRule.setContent { DrinkDetailsScreen(state = state, onAction = actions::add) }
    //
    //     composeRule.onNodeWithTag(DrinkDetailsTestTags.size(Size.L)).performScrollTo().performClick()
    //
    //     assertEquals(listOf<DrinkDetailsAction>(DrinkDetailsAction.SelectSize(Size.L)), actions)
    // }
    //
    // @Test
    // fun `DrinkDetailsScreen WHEN add-on plus clicked SHOULD send IncrementAddOn for that add-on`() {
    //     val addOn = DrinkDetailsPreviewData.extraShot
    //     val state = DrinkDetailsPreviewData.cappuccino
    //     composeRule.setContent { DrinkDetailsScreen(state = state, onAction = actions::add) }
    //
    //     composeRule.onNodeWithTag(DrinkDetailsTestTags.addOnIncrement(addOn.id)).performScrollTo().performClick()
    //
    //     assertEquals(listOf<DrinkDetailsAction>(DrinkDetailsAction.IncrementAddOn(addOn.id)), actions)
    // }
    // endregion
}
