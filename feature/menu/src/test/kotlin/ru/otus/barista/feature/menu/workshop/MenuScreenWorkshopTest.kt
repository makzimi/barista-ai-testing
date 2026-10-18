@file:Suppress("NoUnusedImports", "UnusedImports", "UnusedPrivateProperty")

package ru.otus.barista.feature.menu.workshop

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import ru.otus.barista.feature.menu.presentation.CategoryFilter
import ru.otus.barista.feature.menu.presentation.EmptyReason
import ru.otus.barista.feature.menu.presentation.MenuAction
import ru.otus.barista.feature.menu.presentation.MenuState
import ru.otus.barista.feature.menu.presentation.MenuStatus
import ru.otus.barista.feature.menu.ui.MenuPreviewData
import ru.otus.barista.feature.menu.ui.MenuScreen
import ru.otus.barista.feature.menu.ui.MenuTestTags
import kotlin.test.assertEquals

/**
 * Live-coding copy of `ui/MenuScreenTest` (LIVE_CODING_SCRIPT Part 3.1): Compose UI tests on the JVM (Robolectric).
 * Uncomment one region at a time (select it, `Cmd + /`) and run it from the gutter.
 * The screen is stateless: we hand it a ready State and record what it sends back through `onAction`.
 */
// region Step 1 — also uncomment the runner (JUnit rejects a @RunWith class that has no @Test yet)
// @RunWith(RobolectricTestRunner::class)
// endregion
@Config(sdk = [34])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class MenuScreenWorkshopTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val actions = mutableListOf<MenuAction>()

    // region Step 1 — render a Content state, find rows by test tag
    // @Test
    // fun `MenuScreen WHEN state is Content SHOULD show a row per drink`() {
    //     val state = MenuPreviewData.content
    //
    //     composeRule.setContent { MenuScreen(state = state, onAction = actions::add) }
    //
    //     composeRule.onNodeWithTag(MenuTestTags.row(MenuPreviewData.cappuccino.id)).assertIsDisplayed()
    //     composeRule.onNodeWithTag(MenuTestTags.row(MenuPreviewData.matchaLatte.id)).assertIsDisplayed()
    // }
    // endregion

    // region Step 2 — a click becomes an Action: OpenDrink(id)
    // @Test
    // fun `MenuScreen WHEN row clicked SHOULD send OpenDrink with the drink id`() {
    //     composeRule.setContent { MenuScreen(state = MenuPreviewData.content, onAction = actions::add) }
    //
    //     composeRule.onNodeWithTag(MenuTestTags.row(MenuPreviewData.cappuccino.id)).performClick()
    //
    //     assertEquals(listOf<MenuAction>(MenuAction.OpenDrink(MenuPreviewData.cappuccino.id)), actions)
    // }
    // endregion

    // region Step 3 — chip "Чай" sends SelectCategory(TEA)
    // @Test
    // fun `MenuScreen WHEN tea chip clicked SHOULD send SelectCategory TEA`() {
    //     composeRule.setContent { MenuScreen(state = MenuPreviewData.content, onAction = actions::add) }
    //
    //     composeRule.onNodeWithTag(MenuTestTags.chip(CategoryFilter.TEA)).performClick()
    //
    //     assertEquals(listOf<MenuAction>(MenuAction.SelectCategory(CategoryFilter.TEA)), actions)
    // }
    // endregion

    // region Step 4 — the Empty state explains why it is empty
    // @Test
    // fun `MenuScreen WHEN state is Empty with NO_FAVORITES SHOULD show the no-favorites reason`() {
    //     val state = MenuState(status = MenuStatus.Empty(EmptyReason.NO_FAVORITES), favoritesOnly = true)
    //
    //     composeRule.setContent { MenuScreen(state = state, onAction = actions::add) }
    //
    //     composeRule.onNodeWithText("Пока нет избранного").assertIsDisplayed()
    // }
    // endregion
}
