# Writing Compose UI Tests

Body of the `write-ui-tests` skill. Compose UI tests run on the JVM through Robolectric — no emulator. They check
behavior of a **stateless** screen: what is displayed for a given State, and which `Action` each gesture reports.
Contract: [`docs/rules/testing.md`](../rules/testing.md), [`docs/rules/compose.md`](../rules/compose.md).

## Template

```kotlin
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class MenuScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val actions = RecordingActions<MenuAction>()

    @Test
    fun `MenuScreen WHEN tea chip clicked SHOULD send SelectCategory TEA`() {
        composeRule.setContent { MenuScreen(state = MenuPreviewData.content, onAction = actions) }

        composeRule.onNodeWithTag(MenuTestTags.chip(CategoryFilter.TEA)).performClick()

        assertEquals(listOf<MenuAction>(MenuAction.SelectCategory(CategoryFilter.TEA)), actions.items)
    }

    @Test
    fun `MenuScreen WHEN status is Error SHOULD show the retry button`() {
        composeRule.setContent { MenuScreen(state = MenuState(status = MenuStatus.Error(ErrorKind.NETWORK)), onAction = actions) }

        composeRule.onNodeWithTag(DesignSystemTestTags.RETRY).assertIsDisplayed()
    }
}
```

## Rules of thumb

- Test the `*Screen`, not the `*Route`: pass a State, record actions with `RecordingActions` (`:core:testing`).
- Find nodes **only** by tags from `<Screen>TestTags` / `DesignSystemTestTags` — never by a string literal, rarely by
  text (text is a screenshot concern). Missing tag? Add a constant to the `*TestTags` object and use it in the composable.
- Sample states come from `*PreviewData` (the same data the previews and screenshots use).
- One gesture per test; assert the recorded actions list as a whole.
- Scroll first for rows below the fold: `onNodeWithTag(MenuTestTags.LIST).performScrollToNode(hasTestTag(tag))`.
- What it renders like is not a UI-test question → `screenshot-tests` skill.

## Run

```bash
./gradlew :feature:menu:impl:testDebugUnitTest --tests "*.MenuScreenTest" -q --console=plain
./gradlew moats -q --console=plain
```
End-to-end flows on a real device live in `maestro/` and are not part of `moats`.
