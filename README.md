# Barista

A small multi-module Android app (coffee menu + drink configurator) that exists only so that every kind of
test can be written against it: unit, integration, Compose UI, screenshot, architecture (Konsist) and
Gradle guards. The production code is finished; tests are added on top of it.

Spec: [`../docs/SAMPLE_APP_SPEC.md`](../docs/SAMPLE_APP_SPEC.md).

## Modules

| Module | Kind | Contents |
|---|---|---|
| `:app` | Android app | `MainActivity`, `BaristaNavHost`, `AppGraph` (manual DI), `FakeBackendInterceptor` + `assets/fake-backend/menu.json`, debug overflow menu ("Fail next request") |
| `:core:common` | Kotlin/JVM | `Clock`/`SystemClock`, `DispatcherProvider`, `Money`, `runSuspendCatching` |
| `:core:mvi` | Kotlin/JVM | `MviState`/`MviAction`/`MviEvent` markers, `MviViewModel` base |
| `:core:designsystem` | Android + Compose | `BaristaTheme`, `@ThemePreviews`, `PriceText`, `BaristaBadge`, `FavoriteIcon`, `Loading/Empty/ErrorContent`, icons |
| `:core:network` | Kotlin/JVM | `NetworkFactory` (OkHttp, Retrofit, kotlinx.serialization `Json`) |
| `:core:testing` | Android lib (code in `src/main`) | `MainDispatcherRule`, `FixedClock`, `TestDispatcherProvider`, `Create` (domain + DTO builders), data fakes `FakeMenuRepository`/`FakeFavoritesRepository`, `FakeUseCases.kt` (one fake per use case), screenshot helpers; use via `testImplementation` |
| `:data:api` | Kotlin/JVM | `@Serializable` DTOs (`MenuDto`, `DrinkDto`, …), `MenuSnapshotData`, `DataError`, `MenuRepository` / `FavoritesRepository` interfaces (DTO-based, `String` ids) |
| `:data:impl` | Android lib | `internal`: Retrofit `MenuApi`, `InMemoryMenuCache`, DataStore favorites, `MenuRepositoryImpl`, `FavoritesRepositoryImpl`; public: `DataGraph(retrofit, dataStore, dispatchers)` |
| `:domain:api` | Kotlin/JVM | domain models (`Drink`, `DrinkId`, `HappyHour`, `MenuSnapshot`, `MenuError`, `Size`, `PriceBreakdown`, …) + use case interfaces |
| `:domain:impl` | Android lib (plain Kotlin code) | `*UseCaseImpl`, `DrinkMapper` (DTO → domain, `DataError` → `MenuError`, favorites merge), `DomainGraph` |
| `:feature:menu` | Android + Compose | `presentation/`, `ui/`, `navigation/` (`MENU_ROUTE`), `di/` (`MenuDependencies` = use cases) |
| `:feature:drink-details` | Android + Compose | `presentation/`, `ui/`, `navigation/` (`drinkDetailsRoute()`), `di/` |
| `:konsist-test` | Kotlin/JVM | Architecture tests (Konsist): layering, use cases, domain purity, MVI, Compose and test conventions |
| `build-logic` | included build | Convention plugins `barista.android.application`, `barista.android.library`, `barista.android.library.compose`, `barista.kotlin.library` |

Dependency rules (enforced by `./gradlew assertModuleGraph`, one sentence each in `docs/rules/architecture.md` §1):
`feature → domain:api | core`; `domain:impl → domain:api | data:api | core`; `data:impl → data:api | core`;
`*:api → core:common`; data ↛ domain/feature, feature ↛ data / domain:impl / other features, `api ↛ impl`,
`core ↛ data/domain/feature` (`:core:testing` may see both `api`s); nothing depends on `:app`.

## Run

```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"   # JDK 25 (JBR)
./gradlew :app:installDebug                 # install on a running emulator
./gradlew :app:assembleDebug assertModuleGraph detekt
./gradlew moats                             # all fast guards in one task (see below)
```

The app never touches the network: `FakeBackendInterceptor` answers `GET /menu` and `GET /drinks/{id}`
from `app/src/main/assets/fake-backend/menu.json` with a 300 ms delay. To see the stale-cache path:
overflow menu → **Fail next request**, then pull to refresh.

## Business rules

1. **Happy hour** — a drink's `happyHour.percent` applies only while the local time from the injected `Clock` is in `[from, to)` (windows may wrap midnight).
2. **Sold out** drinks are always last in the list and cannot be added to cart.
3. **Sort** by name (default, case-insensitive) or by displayed price; stable. The favorites filter only filters.
4. **Search** is case-insensitive over name and description, debounced 300 ms; a blank query matches everything.
5. **Price** = `base × sizeMultiplier (S 1.0 / M 1.25 / L 1.5) + Σ(addOn.price × qty)`, then `× (1 − discount)`, rounded half-up to kopecks once at the end, never below 0.
6. **Add-on limits** — `qty ∈ [0, 3]`; adding beyond 3 is ignored and the row has `limitReached = true`; add-ons not in `drink.allowedAddOns` are not shown and cannot be added.
7. **Stale cache** — remote failure with a cached menu → `MenuSnapshot(isStale = true)` (banner); with no cache → `MenuError.Network` (error state).
8. **Favorites** persist in DataStore and are merged into every read; the list and details screens share them.

## Where is the logic to test

| Class (module) | Rule(s) / responsibility |
|---|---|
| `HappyHour.isActiveAt` (`:domain:api`) | 1 |
| `ApplyHappyHourUseCaseImpl` (`:domain:impl`) | 1 (list price with discount, `Clock`-driven) |
| `FilterAndSortMenuUseCaseImpl` (`:domain:impl`) | 2, 3, 4 |
| `GetMenuUseCaseImpl`, `GetDrinkDetailsUseCaseImpl` (`:domain:impl`) | 8 (favorites merged on read), 1 (discount at load time), errors → `MenuError` |
| `DrinkMapper` (`:domain:impl`) | DTO → domain (unknown category skipped, `HH:mm` happy hours, kopecks), `DataError` → `MenuError` |
| `CalculatePriceUseCaseImpl` (`:domain:impl`) | 5 |
| `ChangeAddOnQuantityUseCaseImpl` (`:domain:impl`) | 6 |
| `MenuRepositoryImpl` (`:data:impl`) | 7; DTO contract via `MenuApi` |
| `DataStoreFavoritesLocalDataSource`, `InMemoryMenuCache` (`:data:impl`) | 8, 7 (storage) |
| `MenuStateMapper` (`:feature:menu` `presentation`) | formatting, empty reason (no favorites / nothing found / menu empty), error kind |
| `MenuViewModel` (`:feature:menu` `presentation`) | Loading → Content/Empty/Error, retry, refresh, 300 ms search debounce, favorites sync, `NavigateToDrink` event |
| `DrinkDetailsStateMapper` (`:feature:drink-details` `presentation`) | `limitReached`, `canAddToCart` (2), price formatting |
| `DrinkDetailsViewModel` (`:feature:drink-details` `presentation`) | size/add-on changes recalculate price, `AddedToCart` / `NavigateBack` events, retry |
| `MenuScreen`, `DrinkDetailsScreen` (`ui`) | stateless; test tags in `MenuTestTags` / `DrinkDetailsTestTags`; `@ThemePreviews` for every state |
| `Money` (`:core:common`) | half-up rounding to kopecks, `"350.50 ₽"` formatting |

## Quality gates (the moats)

A moat is an automated check that cannot be talked around: one command, red breaks the push and the merge.
Every rule in `docs/rules/*.md` names the check that enforces it; every check's failure message points back to its rule.

| Command | What it runs | When |
|---|---|---|
| `./gradlew moats` | `detekt` → `:konsist-test:test` → `assertModuleGraph` → `fastUnitTests` (every module's JVM unit, integration and Compose UI tests; screenshot tests excluded) | before saying "done"; pre-push hook; CI |
| `./gradlew screenshots` | `verifyRoborazziDebug` in every module with screenshot tests (only screenshot tests run) | after UI changes; CI |
| `./gradlew recordScreenshots` | `recordRoborazziDebug`: re-record goldens in `src/test/screenshots/` | after an intended visual change |
| `./gradlew :konsist-test:test --rerun-tasks` | architecture tests only | while fixing an architecture violation |
| `./gradlew assertModuleGraph` | module dependency rules | after touching `dependencies {}` |
| `./gradlew installGitHooks` | installs `scripts/pre-push` (runs `moats`) into `.git/hooks` | automatic on every app build |

- `-PexcludeScreenshotTests` / `-PonlyScreenshotTests` filter test classes by hand; `moats` and `screenshots` imply them.
- For agents and scripts: add `-q --console=plain` — empty output and exit code 0 mean green.
- CI: `.github/workflows/check.yml` runs `moats`, then `screenshots`, and uploads reports and Roborazzi diffs on failure.

## AI harness

The same rules, made visible to coding agents (Claude Code, Cursor) and re-surfaced at the moment they matter:

```
CLAUDE.md                          → @AGENTS.md
AGENTS.md                          → @docs/rules/*.md + "Read on demand" (task → skill) + "done = ./gradlew moats exits 0"
docs/rules/                        always loaded: architecture.md · testing.md · compose.md · build-and-run.md
docs/reference/                    skill bodies: write-unit-tests · write-integration-tests · write-ui-tests ·
                                   screenshot-tests · architecture-tests · tdd · verify-before-done · skill-creation
.claude/skills/<name>/SKILL.md     thin stubs → docs/reference/<name>.md (bilingual EN/RU trigger descriptions)
.cursor/skills/<name>/SKILL.md     the same stubs for Cursor; .cursor/rules/barista-rules.mdc (alwaysApply)
.claude/hooks/test-guard.py        PostToolUse: edited src/main/*.kt without <Class>Test.kt → reminder
.claude/settings.json              registers the hook
scripts/pre-push                   blocks a push unless ./gradlew moats is green
.github/workflows/check.yml        moats + screenshots on every PR and on main
```
