package ru.otus.barista.feature.menu.ui

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
import ru.otus.barista.core.designsystem.DesignSystemTestTags
import ru.otus.barista.feature.menu.presentation.CategoryFilter
import ru.otus.barista.feature.menu.presentation.MenuAction
import ru.otus.barista.feature.menu.presentation.MenuState
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class MenuScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val actions = mutableListOf<MenuAction>()

    @Test
    fun `MenuScreen WHEN state is Content SHOULD show a row per drink`() {
        val state = MenuPreviewData.content

        composeRule.setContent { MenuScreen(state = state, onAction = actions::add) }

        composeRule.onNodeWithTag(MenuTestTags.row(MenuPreviewData.cappuccino.id)).assertIsDisplayed()
        composeRule.onNodeWithTag(MenuTestTags.row(MenuPreviewData.matchaLatte.id)).assertIsDisplayed()
    }

    @Test
    fun `MenuScreen WHEN row clicked SHOULD send OpenDrink with the drink id`() {
        composeRule.setContent { MenuScreen(state = MenuPreviewData.content, onAction = actions::add) }

        composeRule.onNodeWithTag(MenuTestTags.row(MenuPreviewData.cappuccino.id)).performClick()

        assertEquals(listOf<MenuAction>(MenuAction.OpenDrink(MenuPreviewData.cappuccino.id)), actions)
    }

    @Test
    fun `MenuScreen WHEN tea chip clicked SHOULD send SelectCategory TEA`() {
        composeRule.setContent { MenuScreen(state = MenuPreviewData.content, onAction = actions::add) }

        composeRule.onNodeWithTag(MenuTestTags.chip(CategoryFilter.TEA)).performClick()

        assertEquals(listOf<MenuAction>(MenuAction.SelectCategory(CategoryFilter.TEA)), actions)
    }

    @Test
    fun `MenuScreen WHEN favorite icon clicked SHOULD send ToggleFavorite for that drink only`() {
        composeRule.setContent { MenuScreen(state = MenuPreviewData.content, onAction = actions::add) }

        composeRule.onNodeWithTag(MenuTestTags.favorite(MenuPreviewData.cappuccino.id)).performClick()

        assertEquals(listOf<MenuAction>(MenuAction.ToggleFavorite(MenuPreviewData.cappuccino.id)), actions)
    }

    @Test
    fun `MenuScreen WHEN state is Empty with NO_FAVORITES SHOULD show the no-favorites reason`() {
        val state = MenuState(status = MenuPreviewData.noFavorites, favoritesOnly = true)

        composeRule.setContent { MenuScreen(state = state, onAction = actions::add) }

        composeRule.onNodeWithText("Пока нет избранного").assertIsDisplayed()
    }

    @Test
    fun `MenuScreen WHEN state is Error SHOULD show the retry button`() {
        val state = MenuState(status = MenuPreviewData.networkError)

        composeRule.setContent { MenuScreen(state = state, onAction = actions::add) }

        composeRule.onNodeWithTag(DesignSystemTestTags.RETRY).assertIsDisplayed()
    }

    @Test
    fun `MenuScreen WHEN retry clicked on Error SHOULD send Retry`() {
        val state = MenuState(status = MenuPreviewData.networkError)
        composeRule.setContent { MenuScreen(state = state, onAction = actions::add) }

        composeRule.onNodeWithTag(DesignSystemTestTags.RETRY).performClick()

        assertEquals(listOf<MenuAction>(MenuAction.Retry), actions)
    }

    @Test
    fun `MenuScreen WHEN content is stale SHOULD show the stale banner above the list`() {
        val state = MenuPreviewData.content.copy(isStale = true)

        composeRule.setContent { MenuScreen(state = state, onAction = actions::add) }

        composeRule.onNodeWithTag(MenuTestTags.STALE_BANNER).assertIsDisplayed()
        composeRule.onNodeWithTag(MenuTestTags.row(MenuPreviewData.cappuccino.id)).assertIsDisplayed()
    }

    @Test
    fun `MenuScreen WHEN content is fresh SHOULD not show the stale banner`() {
        val state = MenuPreviewData.content.copy(isStale = false)

        composeRule.setContent { MenuScreen(state = state, onAction = actions::add) }

        composeRule.onNodeWithTag(MenuTestTags.STALE_BANNER).assertDoesNotExist()
    }
}
