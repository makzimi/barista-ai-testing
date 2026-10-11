# Architecture Rules

Every rule below is enforced by an executable check. A red check means: **fix the code, not the rule**.
How the checks work and how to add one: [`docs/reference/architecture-tests.md`](../reference/architecture-tests.md).

## §1 Modules

| Kind | Path | May depend on |
|---|---|---|
| App | `:app` | everything (wires the graph) |
| Feature API | `:feature:<name>:api` (pure Kotlin/JVM) | `:core:common`, `:core:mvi` |
| Feature impl | `:feature:<name>:impl` (Android + Compose) | any `:feature:*:api`, any `:core:*` |
| Core | `:core:common`, `:core:mvi`, `:core:network` (JVM), `:core:designsystem` (Android) | `:core:common` (`designsystem` also `:core:mvi`) |
| Test support | `:core:testing` (`testImplementation` only) | anything |

- Every `feature/<name>/` holds exactly `api` + `impl`.
- `impl → impl` is forbidden: features talk through `api` (models, `MenuRepository`, route constants).
- `api` holds contracts only: no `dto` package, no `*Impl` classes.
- Nothing depends on `:app`; `core` never depends on `feature` (except `:core:testing`).

Checked by: `./gradlew assertModuleGraph` (dependency direction) and `ModuleStructureKonsistTest` (module and package layout).

## §2 Layers inside a feature `impl`

```
ui (Compose) → presentation (ViewModel, State, Mapper) → domain (use cases, models) ← data (Retrofit, DTOs, cache, DataStore)
```

- `data` never imports `presentation` or `ui`.
- `domain` never imports `data`, `presentation` or `ui`. It sees the repository through the `api` interface.
- Repositories never import use cases; use cases never import ViewModels.
- Every `impl` has `domain`, `presentation` and `ui` packages (`data` only when the feature owns data; drink-details reuses `MenuRepository`).

Checked by: `LayeringKonsistTest`, `ModuleStructureKonsistTest`.

## §3 ViewModels depend on use cases only

A ViewModel's constructor takes use cases, mappers and `DispatcherProvider` — never a `*Repository`, `*Api`,
`*DataSource` or `*Cache`. Need data? Add a use case (even a one-line one: it is the seam tests replace).

```kotlin
// ❌
class MenuViewModel(private val repository: MenuRepository, …)
// ✅
class MenuViewModel(private val getMenu: GetMenuUseCase, …)
```

Checked by: `UseCaseKonsistTest` (`ViewModels depend on use cases only`).

## §4 Use cases

- Name `<Verb><Noun>UseCase`, package `..domain..`.
- Exactly one public function: `operator fun invoke(…)` (`suspend` when it does I/O). Helpers are `private`.
- I/O use cases return `Result<T>` or `Flow<T>`; pure ones (`CalculatePriceUseCase`) return the value.
- Time comes from the injected `Clock`, never `LocalTime.now()`; dispatchers from `DispatcherProvider`.

Checked by: `UseCaseKonsistTest` (package and the single `operator fun invoke`); return types and time/dispatcher
injection by review and unit tests.

## §5 Domain purity

- `domain` and `presentation` import nothing from `android.*`, `androidx.compose.*` (except
  `androidx.compose.runtime.Immutable` / `Stable`), `retrofit2.*`, `okhttp3.*`, `kotlinx.serialization.*`.
- `:feature:*:api`, `:core:common` and `:core:mvi` import nothing from `android.*` / `androidx.*`
  (the only exception: `androidx.lifecycle.ViewModel`, the base of `MviViewModel`).

Why: business rules stay testable with plain JUnit — no Robolectric, no network.

Checked by: `DomainPurityKonsistTest` (plus the JVM-only modules physically cannot see Android).

## §6 MVI contract

- Screen state: `@Immutable data class <Screen>State : MviState`.
- User intents: `sealed interface <Screen>Action : MviAction`; one-shot effects: `sealed interface <Screen>Event : MviEvent`.
  Anything the screen keeps showing is State, not an Event.
- `<Screen>ViewModel : MviViewModel<State, Action, Event>`; one entry point `onAction(action)`.
- State, Action, Event and ViewModel live in the `presentation` package; formatting lives in a `*StateMapper`.

Checked by: `MviConventionKonsistTest`.
