# Testing Rules

Templates and the how-to live in the skills (`write-unit-tests`, `write-integration-tests`, `write-ui-tests`,
`screenshot-tests`). This file is the contract every test follows.

## §1 Which test for which change

| You changed… | Write / update | Lives in |
|---|---|---|
| A business rule (price, discount, happy hour, sort/filter, add-on limit) | **Unit** test of the use case / model | `src/test/.../domain/` |
| State formatting, empty/error reason, `limitReached` | **Unit** test of the `*StateMapper` | `src/test/.../presentation/` |
| ViewModel flow (loading → content, retry, debounce, events) | **Unit** test of the ViewModel with `FakeMenuRepository` | `src/test/.../presentation/` |
| DTO, Retrofit interface, JSON, repository cache/stale logic | **Integration** test: `MockWebServer` + real `NetworkFactory` | `src/test/.../data/` |
| Wiring across layers (ViewModel → use case → repository → mapper) | **Integration** (sociable) test through `MenuGraph` | `src/test/<feature package>/` |
| A composable's callbacks, visibility, enabled state | **Compose UI** test (Robolectric) | `src/test/.../ui/` |
| Layout, colors, dark theme, a new screen state | **Screenshot**: add a `@ThemePreviews` preview, re-record | `src/test/screenshots/` |
| Module dependency or package placement | Nothing new — `assertModuleGraph` / Konsist already guard it | — |

**Production change ⇒ test in the same change.** A fix comes with the test that would have caught the bug.
Checked by: the `test-guard.py` hook (reminder), code review, `./gradlew moats`.

## §2 Naming

`` `<action> WHEN <concrete input condition> SHOULD <observable outcome>` `` — e.g.
`` `calculate WHEN size L and two extra shots SHOULD return 1.5x base plus add-ons` ``.
Use-case tests may drop the action: `` `WHEN … SHOULD …` ``. Never `invoke`, `test1`, `works correctly`, `SHOULD handle it`.

Checked by: `TestConventionKonsistTest` (`test names contain WHEN and SHOULD`).

## §3 Arrange / Act / Assert

Three blocks separated by blank lines. **WHEN → Arrange, action → Act, SHOULD → Assert.** One Act (one call on the SUT).
No `// Arrange`, `// Act`, `// Assert`, `// Given`, `// When`, `// Then` comments — the blank lines are the structure.

Checked by: `TestConventionKonsistTest` (`tests have no Arrange Act Assert … comments`).

## §4 Every test asserts

A test passes only because an assertion said so: `assertEquals`, Turbine `awaitItem()`, Compose `assertIsDisplayed()`,
`captureRoboImage`/`captureScreenshot`. Prefer whole-object `assertEquals(expected, actual)` with `expected` built in Arrange.

Checked by: `TestConventionKonsistTest` (`every test asserts something`).

## §5 Fakes over mocks

- Data comes from fakes: `FakeMenuRepository`, `FakeMenuApi`, `FakeFavoritesLocalDataSource`, `FixedClock`, `TestDispatcherProvider`.
- Mock (MockK) only when the interaction itself is the behavior; never `verify` plumbing (`verify { repository.getMenu() }`).
- No relaxed mocks. No real time, network, disk or `Thread.sleep`.

## §6 Test data

`Create` (`:core:testing`) builds every model with realistic defaults: `Create.drink(basePrice = Money(30000))`,
`Create.CAPPUCCINO`, `Create.sampleMenu()`. Show only the fields the scenario is about.

## §7 Coroutines

`runTest` for suspend code; `MainDispatcherRule` for anything on `viewModelScope`; `TestDispatcherProvider` for
injected dispatchers; virtual time (`advanceTimeBy`, `advanceUntilIdle`) for debounce. Turbine for flows.

## §8 Location and names

- The test class is `<Sut>Test` in the **same package** as the SUT. Integration tests: `*IntegrationTest` or
  `<Repository>ImplTest`; UI tests: `<Screen>Test`/`*ScreenTest`; screenshot tests: `*ScreenshotTest`.
- Workshop copies for live coding: `<Sut>WorkshopTest` in a `workshop` package (bodies commented until the session).

Checked by: `TestConventionKonsistTest` (`classes with tests are named Test`, `tests reside in the package of the class they test`).
