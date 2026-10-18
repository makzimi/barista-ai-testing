# Writing Unit Tests

Body of the `write-unit-tests` skill. The contract (naming, AAA, fakes, location) is in
[`docs/rules/testing.md`](../rules/testing.md); this doc is the concrete how for this project.

## 1. Pick the template by SUT

| SUT | Module | Template | Doubles |
|---|---|---|---|
| Pure use case (`CalculatePriceUseCaseImpl`, `FilterAndSortMenuUseCaseImpl`, `ChangeAddOnQuantityUseCaseImpl`) | `:domain:impl` | §3 | none |
| Use case with `Clock` (`ApplyHappyHourUseCaseImpl`, `GetDrinkDetailsUseCaseImpl`) | `:domain:impl` | §3 + `FixedClock` | `FixedClock` |
| Use case over repositories (`GetMenuUseCaseImpl`, `ToggleFavoriteUseCaseImpl`, `Observe*UseCaseImpl`) | `:domain:impl` | §3 + `runTest` | `FakeMenuRepository`, `FakeFavoritesRepository` (DTO-based) |
| `DrinkMapper` (DTO → domain, `DataError` → `MenuError`) | `:domain:impl` | §4 | none |
| `*StateMapper` | feature | §5 | none |
| `*ViewModel` | feature | §6 | `Fake*UseCase` — the feature sees only use case interfaces |
| Model rule (`HappyHour.isActiveAt`, `Money`) | where it lives | §3 | none |

Everything comes from `:core:testing` (`testImplementation` of every module that needs it): `Create` (domain + DTO
builders), `FakeMenuRepository`, `FakeFavoritesRepository`, `FakeUseCases.kt` (`FakeGetMenuUseCase`, …),
`FixedClock`, `MainDispatcherRule`, `TestDispatcherProvider`, `RecordingActions`.
JUnit4, `kotlin.test` assertions, Turbine, coroutines-test and MockK are on every module's test classpath.

## 2. The name ↔ body contract

```
`<action> WHEN <input condition> SHOULD <observable outcome>`
         └── Arrange ──────┘       └── Assert ────────┘
 └ Act ┘
```
Anything in WHEN is set up in Arrange; anything asserted is in SHOULD. One Act. Blank lines between phases, no
phase comments. Build `expected` in Arrange, assert the whole object.

## 3. Use case (`:domain:impl`)

```kotlin
class CalculatePriceUseCaseImplTest {

    private val sut = CalculatePriceUseCaseImpl()

    @Test
    fun `WHEN size L and one extra shot SHOULD return 1_5x base plus the add-on`() {
        val details = DrinkDetails(drink = Create.drink(basePrice = Money(20000)), discountPercent = null)
        val configuration = DrinkConfiguration(size = Size.L, addOns = mapOf(Create.EXTRA_SHOT.id to 1))

        val price = sut(details, configuration)

        assertEquals(Money(36000), price.total)
    }
}
```
- Time-dependent: `private val clock = FixedClock(time = LocalTime.of(17, 0))`, pass it to the constructor, move it
  with `clock.time = …` or `clock.advanceBy(Duration.ofHours(1))`. Never `LocalTime.now()`.
- Repository-backed: the **data-layer** fakes, real `DrinkMapper` — DTOs in, domain models out:
  ```kotlin
  private val menuRepository = FakeMenuRepository()            // default: Create.sampleMenuDto() (= menu.json)
  private val favoritesRepository = FakeFavoritesRepository()  // favorites.value = setOf("matcha_latte")
  private val sut = GetMenuUseCaseImpl(menuRepository, favoritesRepository, DrinkMapper())
  ```
  Arrange with `menuRepository.setDrinks(listOf(Create.ESPRESSO_DTO))`, `menuRepository.failNextGetMenu = DataError.Network()`,
  `staleOnFailure = true`; assert domain results (`Create.ESPRESSO`, `MenuError.Network`). Call in `runTest { }`.

## 4. `DrinkMapper` (`:domain:impl`)

Pure, no doubles; one rule per test:
```kotlin
@Test
fun `mapDrink WHEN the DTO has a happy-hour window SHOULD parse HH mm into local times`() {
    val dto = Create.drinkDto(happyHour = Create.happyHourDto(from = "22:00", to = "02:30", percent = 10))
    val expected = HappyHour(from = LocalTime.of(22, 0), to = LocalTime.of(2, 30), percent = 10)

    val drink = sut.mapDrink(dto, favoriteIds = emptySet())

    assertEquals(expected, drink?.happyHour)
}
```
Keep `mapSnapshot(sampleMenuDto) == sampleMenu()`: it keeps the two halves of `Create` in sync with `menu.json`.

## 5. Mapper (presentation)

Real instance, whole-object assert:
```kotlin
@Test
fun `mapContent WHEN favorites filter is on and nothing is favorite SHOULD return Empty NO_FAVORITES`() {
    val items = listOf(MenuItem(drink = Create.ESPRESSO, price = Create.ESPRESSO.basePrice, discountPercent = null))
    val expected = MenuState(status = MenuStatus.Empty(EmptyReason.NO_FAVORITES), favoritesOnly = true)

    val state = sut.mapContent(MenuState(favoritesOnly = true), allItems = items, visibleItems = emptyList(), isStale = false)

    assertEquals(expected, state)
}
```

## 6. ViewModel (feature) — fake use cases

```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
class MenuViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(StandardTestDispatcher())

    private val getMenu = FakeGetMenuUseCase(MenuSnapshot(drinks = listOf(Create.MATCHA, Create.ESPRESSO), isStale = false))
    private val observeFavorites = FakeObserveFavoritesUseCase()
    private val toggleFavorite = FakeToggleFavoriteUseCase(observeFavorites.favorites)   // one shared favorites flow
    private val applyHappyHour = FakeApplyHappyHourUseCase()
    private val filterAndSort = FakeFilterAndSortMenuUseCase()

    @Test
    fun `Retry WHEN the first load failed SHOULD show Content`() = runTest {
        getMenu.failNext = MenuError.Network()
        val viewModel = createViewModel().also { advanceUntilIdle() }

        viewModel.onAction(MenuAction.Retry)
        advanceUntilIdle()

        assertEquals(MenuStatus.Content, viewModel.state.value.status)
    }

    private fun createViewModel() = MenuViewModel(
        getMenu = getMenu,
        observeFavorites = observeFavorites,
        toggleFavorite = toggleFavorite,
        applyHappyHour = applyHappyHour,
        filterAndSort = filterAndSort,
        mapper = MenuStateMapper(),
        dispatchers = TestDispatcherProvider(mainDispatcherRule.testDispatcher),
    )
}
```
- The feature sees only use case interfaces, so its tests fake exactly those. Business rules are not re-tested here.
- Fake defaults are deliberately dumb (no discount, no filtering, no clamping). Arrange the answer the scenario needs:
  `applyHappyHour.setDiscount(id, Money(18692), 15)`, `filterAndSort.answer = { items, filter -> … }`,
  `calculatePrice.answer = { _, configuration -> … }`.
- Interaction is the behavior? Assert the fake's record: `toggleFavorite.calls`, `getMenu.calls`, `filterAndSort.filters`.
- `StandardTestDispatcher` + `advanceUntilIdle()` when you need to see intermediate states; debounce:
  `advanceTimeBy(MenuViewModel.SEARCH_DEBOUNCE_MS)` then `runCurrent()`.
- State sequence with Turbine: `viewModel.state.test { assertEquals(MenuState(), awaitItem()); … }`.
- Events: `viewModel.events.test { viewModel.onAction(MenuAction.OpenDrink(id)); assertEquals(MenuEvent.NavigateToDrink(id), awaitItem()) }`.

## 7. Pitfalls

| Symptom | Cause | Fix |
|---|---|---|
| `Module with the Main dispatcher is missing` | ViewModel uses `viewModelScope` | add `MainDispatcherRule` |
| State still `Loading` in the assert | async work not run | `advanceUntilIdle()` after the action |
| Debounce test flaky / always passes | real time or no time advanced | `advanceTimeBy` + `runCurrent`, never `Thread.sleep` |
| 30 tests break after a refactor | tests `verify` internal calls | assert state/output instead |
| `expected` differs in a field you didn't touch | a hidden behavior changed | that's the whole-object assert doing its job |

## 8. Run

```bash
./gradlew :domain:impl:testDebugUnitTest --tests "*.CalculatePriceUseCaseImplTest" -q --console=plain
./gradlew moats -q --console=plain     # before saying done
```

## 9. Workflow

1. Pick the SUT template (§1). 2. Write the name first. 3. Arrange with `Create` + fakes, build `expected`.
4. One Act. 5. Whole-object assert. 6. Run the class, see it fail for the right reason first (`tdd` skill).
7. Run `./gradlew moats`. 8. Delete any comment that only narrates the test.
