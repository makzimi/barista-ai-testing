# Architecture Rules

Every rule below is enforced by an executable check. A red check means: **fix the code, not the rule**.
How the checks work and how to add one: [`docs/reference/architecture-tests.md`](../reference/architecture-tests.md).

## §1 Modules = layers

Strict layers, not inversion: every arrow points **down** (feature → domain → data → core).

| Module | Kind | Holds | May depend on |
|---|---|---|---|
| `:app` | Android app | `AppGraph` (= `DataGraph` + `DomainGraph`), nav host, fake backend, debug menu | everything |
| `:feature:menu`, `:feature:drink-details` | Android + Compose | `presentation`, `ui`, `navigation`, `di` | `:domain:api`, `:core:*` |
| `:domain:api` | Kotlin/JVM | domain models + use case **interfaces** | `:core:common` |
| `:domain:impl` | Android lib¹ | `*UseCaseImpl`, `DrinkMapper` (DTO → domain), `DomainGraph` | `:domain:api`, `:data:api`, `:core:*` |
| `:data:api` | Kotlin/JVM | `@Serializable` DTOs, `DataError`, `MenuRepository` / `FavoritesRepository` interfaces | `:core:common` |
| `:data:impl` | Android lib | **internal** Retrofit `MenuApi`, cache, DataStore, repository impls; public `DataGraph` only | `:data:api`, `:core:*` |
| `:core:*` | JVM / Android | `common`, `mvi`, `network`, `designsystem`; `testing` (`testImplementation` only) | `:core:*` (`testing` also `:data:api`, `:domain:api`) |

¹ Android only so its tests can use `:core:testing` (an Android library); its code stays plain Kotlin (§5).

The arrows as sentences — each one is a line in `moduleGraphAssert`:
- Nobody depends on the app.
- Data knows nothing about domain or features. Domain knows nothing about UI.
- Domain contracts (`:domain:api`) don't leak data models.
- UI never touches repositories (`:feature ↛ :data`) and sees use case interfaces, not implementations (`:feature ↛ :domain:impl`).
- Features don't know each other: the app glues them (routes live in each feature's `navigation` package).
- Contracts never depend on implementations (`api ↛ impl`). Core is the bottom layer.
- Package roots: `ru.otus.barista.data.{model,repository}`, `data.impl.*`, `domain.{model,usecase}`, `domain.impl.*`,
  `feature.<name>.*`. A feature may later add `:feature:<name>:domain|data` sub-modules for logic no one else shares.

Checked by: `./gradlew assertModuleGraph` (dependency direction) and `ModuleStructureKonsistTest`
(`data`/`domain` = `api` + `impl`, package roots, only `DataGraph` public in `:data:impl`).

## §2 Layers are modules

```
feature (ui → presentation) → domain:api ← domain:impl (use cases, DrinkMapper) → data:api ← data:impl (Retrofit, cache, DataStore)
```

- `data/*` imports nothing from `ru.otus.barista.domain` or `ru.otus.barista.feature`.
- `domain/api` imports nothing from `ru.otus.barista.data`; `domain/*` imports nothing from `feature`.
- `feature/*` imports nothing from `ru.otus.barista.data` or `ru.otus.barista.domain.impl`.
- The data → domain mapping (DTO → `Drink`, `DataError` → `MenuError`, favorites merge) lives in `:domain:impl/mapper`.
- Repositories never import use cases; use cases never import ViewModels.
- A feature has `presentation` and `ui` packages and no `data`/`domain` packages.

Checked by: `LayeringKonsistTest`, `ModuleStructureKonsistTest` (plus the module graph: the import would not compile).

## §3 ViewModels depend on use case interfaces only

A ViewModel's constructor takes use case **interfaces** from `:domain:api`, mappers and `DispatcherProvider` — never a
`*Repository`, `*Api`, `*DataSource`, `*Cache` or `*UseCaseImpl`. A feature's `di/*Dependencies` lists use cases only.
That interface is the seam ViewModel tests replace with `Fake*UseCase`.

```kotlin
// ❌
class MenuViewModel(private val repository: MenuRepository, …)       // does not even compile: :feature ↛ :data
class MenuViewModel(private val getMenu: GetMenuUseCaseImpl, …)       // :feature ↛ :domain:impl
// ✅
class MenuViewModel(private val getMenu: GetMenuUseCase, …)
```

Checked by: `UseCaseKonsistTest` (`ViewModels depend on use cases only`) and the module graph.

## §4 Use cases

- Interface `<Verb><Noun>UseCase` in `ru.otus.barista.domain.usecase` (`:domain:api`); one implementation
  `<Verb><Noun>UseCaseImpl : <Verb><Noun>UseCase` in `ru.otus.barista.domain.impl.usecase`. No `*UseCase` classes.
- Exactly one public function: `operator fun invoke(…)` (`suspend` when it does I/O). Helpers are `private`.
- I/O use cases return `Result<T>` or `Flow<T>` with **domain** types and `MenuError`; pure ones return the value.
- Time comes from the injected `Clock` (`ApplyHappyHourUseCaseImpl(clock)`), never `LocalTime.now()`.

Checked by: `UseCaseKonsistTest` (interfaces vs impls, packages, the single `operator fun invoke`); return types and
time injection by review and unit tests.

## §5 Purity

- `:domain:*` and `presentation` import nothing from `android.*`, `androidx.compose.*` (except
  `androidx.compose.runtime.Immutable` / `Stable`), `retrofit2.*`, `okhttp3.*`, `kotlinx.serialization.*`.
- `:domain:*`, `:data:api`, `:core:common` and `:core:mvi` import nothing from `android.*` / `androidx.*`
  (the only exception: `androidx.lifecycle.ViewModel`, the base of `MviViewModel`). `:data:api` may use
  `kotlinx.serialization` (DTOs are `@Serializable`).

Why: business rules stay testable with plain JUnit — no Robolectric, no network.

Checked by: `DomainPurityKonsistTest` (plus `:domain:api`, `:data:api`, `:core:common`, `:core:mvi` are JVM modules
that physically cannot see Android).

## §6 MVI contract

- Screen state: `@Immutable data class <Screen>State : MviState`.
- User intents: `sealed interface <Screen>Action : MviAction`; one-shot effects: `sealed interface <Screen>Event : MviEvent`.
  Anything the screen keeps showing is State, not an Event.
- `<Screen>ViewModel : MviViewModel<State, Action, Event>`; one entry point `onAction(action)`.
- State, Action, Event and ViewModel live in the `presentation` package; formatting lives in a `*StateMapper`.

Checked by: `MviConventionKonsistTest`.
