# Compose Rules

## §1 Stateless screens

- `<Screen>Screen(state, onAction, modifier = Modifier, …)` renders from `state` only and reports every user intent
  through `onAction`. It never sees a ViewModel, a repository or a navigation controller.
- `<Screen>Route(viewModel, …)` is the only glue: collects `state`, handles one-shot `events`, calls the Screen.
- Callbacks are forwarded unchanged; a refactor must not drop one (UI tests catch it: `MenuScreenTest`).

## §2 State shape

- `@Immutable data class`; collections are `ImmutableList` / `ImmutableSet` / `ImmutableMap` (`persistentListOf()`).
- **No function-typed properties** in the State or any `*State` class it contains (`onClick: () -> Unit`).
  The State names the item (`id`), the composable resolves the action.
- Strings are pre-formatted by the mapper (`price = "186.92 ₽"`, `discountLabel = "−15%"`).

Checked by: `MviConventionKonsistTest` (`State classes have no function-typed properties`, `State collections are immutable`),
detekt Twitter Compose rules.

## §3 No mapping in composables

Formatting money, picking an empty-state reason, filtering, deciding `canAddToCart` — all in `*StateMapper`
(unit-tested). Composables only lay out what the State says. Allowed in UI: enum → string resource lookups,
`testTag`, theme choices.

## §4 Previews are mandatory

Every public `*Screen` has `@ThemePreviews` previews in the same file (light + dark), one per meaningful state
(content, loading, empty, error, …), named `<Screen><State>Preview`, `private`, wrapped in `BaristaTheme`, with
sample states from `*PreviewData`. Each preview automatically becomes a screenshot golden.

Checked by: `ComposeKonsistTest` (`every public Screen composable has a ThemePreviews preview in the same file`).

## §5 Test tags

- Tags are constants (or functions like `MenuTestTags.row(id)`) in a `<Screen>TestTags` object in the feature's
  `ui` package (`DesignSystemTestTags` for shared components). Never `Modifier.testTag("literal")`.
- Tests find nodes by the same constants: `onNodeWithTag(MenuTestTags.SEARCH)`.

Checked by: `ComposeKonsistTest` (`testTag is called with constants …`, `TestTags objects … reside in the ui package`).

## §6 Modifier parameter

Every composable that emits UI takes `modifier: Modifier = Modifier` as its first optional parameter and applies it
to the root element only.

Checked by: `ComposeKonsistTest` (`modifier is the first optional parameter and defaults to Modifier`) and detekt
(`ModifierMissing`, `ModifierWithoutDefault`, `ComposableParamOrder`, `ModifierReused`).
