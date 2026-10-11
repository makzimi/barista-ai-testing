# Writing Unit Tests

Body of the `write-unit-tests` skill. The contract (naming, AAA, fakes, location) is in
[`docs/rules/testing.md`](../rules/testing.md); this doc is the concrete how for this project.

## 1. Pick the template by SUT

| SUT | Template | Doubles |
|---|---|---|
| Pure use case (`CalculatePriceUseCase`, `FilterAndSortMenuUseCase`, `ChangeAddOnQuantityUseCase`) | §3 | none |
| Use case with `Clock` (`ApplyHappyHourUseCase`, `GetDrinkDetailsUseCase`) | §3 + `FixedClock` | `FixedClock` |
| Use case over the repository (`GetMenuUseCase`, `ToggleFavoriteUseCase`) | §3 + `runTest` | `FakeMenuRepository` |
| `*StateMapper` | §4 | none |
| `*ViewModel` | §5 | real use cases over `FakeMenuRepository` |
| Model rule (`HappyHour.isActiveAt`, `Money`) | §3 | none |

Everything comes from `:core:testing` (already a `testImplementation` of both feature impls):
`Create`, `FakeMenuRepository`, `FixedClock`, `MainDispatcherRule`, `TestDispatcherProvider`, `RecordingActions`.
JUnit4, `kotlin.test` assertions, Turbine, coroutines-test and MockK are on every module's test classpath.

## 2. The name ↔ body contract

```
`<action> WHEN <input condition> SHOULD <observable outcome>`
         └── Arrange ──────┘       └── Assert ────────┘
 └ Act ┘
```
Anything in WHEN is set up in Arrange; anything asserted is in SHOULD. One Act. Blank lines between phases, no
phase comments. Build `expected` in Arrange, assert the whole object.

## 3. Use case

```kotlin
class CalculatePriceUseCaseTest {

    private val sut = CalculatePriceUseCase()

    @Test
    fun `calculate WHEN size L and one extra shot SHOULD return 1_5x base plus the add-on`() {
        val details = DrinkDetails(drink = Create.drink(basePrice = Money(20000)), discountPercent = null)
        val configuration = DrinkConfiguration(size = Size.L, addOns = mapOf(Create.EXTRA_SHOT.id to 1))

        val price = sut(details, configuration)

        assertEquals(Money(36000), price.total)
    }
}
```
- Time-dependent: `private val clock = FixedClock(time = LocalTime.of(17, 0))`, pass it to the constructor, move it
  with `clock.time = …` or `clock.advanceBy(Duration.ofHours(1))`. Never `LocalTime.now()`.
- Repository-backed: `private val repository = FakeMenuRepository()`; arrange with `repository.setDrinks(…)`,
  `repository.failNextGetMenu = IOException()`; call in `runTest { }`.

## 4. Mapper

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

## 5. ViewModel

```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
class MenuViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(StandardTestDispatcher())

    private val repository = FakeMenuRepository(listOf(Create.ESPRESSO, Create.MATCHA))
    private val clock = FixedClock(time = LocalTime.of(12, 0))

    @Test
    fun `Retry WHEN the first load failed SHOULD show Content`() = runTest {
        repository.failNextGetMenu = MenuError.Network(IOException())
        val viewModel = createViewModel().also { advanceUntilIdle() }

        viewModel.onAction(MenuAction.Retry)
        advanceUntilIdle()

        assertEquals(MenuStatus.Content, viewModel.state.value.status)
    }

    private fun createViewModel() = MenuViewModel(
        getMenu = GetMenuUseCase(repository),
        observeFavorites = ObserveFavoritesUseCase(repository),
        toggleFavorite = ToggleFavoriteUseCase(repository),
        applyHappyHour = ApplyHappyHourUseCase(clock),
        filterAndSort = FilterAndSortMenuUseCase(),
        mapper = MenuStateMapper(),
        dispatchers = TestDispatcherProvider(mainDispatcherRule.testDispatcher),
    )
}
```
- Real use cases + `FakeMenuRepository`, not mocked use cases: the test survives refactors of the use cases.
- `StandardTestDispatcher` + `advanceUntilIdle()` when you need to see intermediate states; debounce:
  `advanceTimeBy(MenuViewModel.SEARCH_DEBOUNCE_MS)` then `runCurrent()`.
- State sequence with Turbine: `viewModel.state.test { assertEquals(MenuState(), awaitItem()); … }`.
- Events: `viewModel.events.test { viewModel.onAction(MenuAction.OpenDrink(id)); assertEquals(MenuEvent.NavigateToDrink(id), awaitItem()) }`.
- Interaction is the behavior (favorite saved)? Assert the fake's record: `repository.favoriteCalls`.

## 6. Pitfalls

| Symptom | Cause | Fix |
|---|---|---|
| `Module with the Main dispatcher is missing` | ViewModel uses `viewModelScope` | add `MainDispatcherRule` |
| State still `Loading` in the assert | async work not run | `advanceUntilIdle()` after the action |
| Debounce test flaky / always passes | real time or no time advanced | `advanceTimeBy` + `runCurrent`, never `Thread.sleep` |
| 30 tests break after a refactor | tests `verify` internal calls | assert state/output instead |
| `expected` differs in a field you didn't touch | a hidden behavior changed | that's the whole-object assert doing its job |

## 7. Run

```bash
./gradlew :feature:drink-details:impl:testDebugUnitTest --tests "*.CalculatePriceUseCaseTest" -q --console=plain
./gradlew moats -q --console=plain     # before saying done
```

## 8. Workflow

1. Pick the SUT template (§1). 2. Write the name first. 3. Arrange with `Create` + fakes, build `expected`.
4. One Act. 5. Whole-object assert. 6. Run the class, see it fail for the right reason first (`tdd` skill).
7. Run `./gradlew moats`. 8. Delete any comment that only narrates the test.
