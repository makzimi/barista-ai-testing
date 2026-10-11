# Screenshot Tests

Body of the `screenshot-tests` skill. Roborazzi + Robolectric render composables on the JVM and compare them with
committed PNG goldens in `<module>/src/test/screenshots/`.

## Where screenshots come from

1. **Previews (automatic).** Every `@ThemePreviews` preview in `core:designsystem` and the feature impls becomes
   two goldens (light, dark) through ComposablePreviewScanner (`<fqcn>.<PreviewFn>.<light|dark>.png`).
   Want a new screen state covered? Add a `private` `@ThemePreviews` preview with a sample State — that's it.
2. **Hand-written `*ScreenshotTest`** for what previews can't express (font scale, a specific device):
   ```kotlin
   @RunWith(RobolectricTestRunner::class)
   @Config(sdk = [34], qualifiers = RobolectricDeviceQualifiers.Pixel5)
   @GraphicsMode(GraphicsMode.Mode.NATIVE)
   class MenuScreenScreenshotTest {
       @get:Rule val composeRule = createComposeRule()

       @Test
       fun `MenuScreen WHEN content SHOULD match the golden`() {
           composeRule.captureScreenshot("MenuScreenScreenshotTest.content") {
               MenuScreen(state = MenuPreviewData.content, onAction = {})
           }
       }
   }
   ```
   `captureScreenshot` (`:core:testing`) wraps `BaristaTheme`, freezes the clock and uses `BaristaRoborazziOptions`
   (`changeThreshold = 0.01`, `resizeScale = 0.5`).
3. **Excluding a preview**: annotate it `@DoNotTestPreview` and say why (non-deterministic content only).

## Commands

| Goal | Command |
|---|---|
| Verify against goldens (CI does this) | `./gradlew screenshots` |
| Re-record goldens after an intended visual change | `./gradlew recordScreenshots` |
| Produce diff images without failing | `./gradlew compareRoborazziDebug` |

`moats` does **not** run screenshot tests (they are slower); `screenshots` runs only them.

## Reviewing a diff

1. `./gradlew screenshots` fails → open `<module>/build/outputs/roborazzi/` — `*_compare.png` shows
   golden | diff | new side by side.
2. Decide: **intended** (you changed the UI on purpose) → `./gradlew recordScreenshots`, look at every changed PNG in
   `git diff --stat src/test/screenshots`, commit them with the code. **Unintended** → fix the code, never re-record
   to make CI green.
3. A golden changed that your change shouldn't touch (e.g. dark theme of another screen) = a regression you just found.

CI uploads `**/build/outputs/roborazzi/**` as an artifact when verification fails.
