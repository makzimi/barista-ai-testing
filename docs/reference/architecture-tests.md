# Architecture Tests

Body of the `architecture-tests` skill. Two mechanisms guard the architecture on every `./gradlew moats`.
**The rules live in code** — the test classes are the source of truth; `docs/rules/*.md` explains them.

| Tool | Reads | Enforces |
|---|---|---|
| **Konsist** (`:konsist-test`) | Kotlin sources and imports of every module | naming, package placement, layering, domain purity, MVI/Compose/test conventions |
| **`assertModuleGraph`** (jraska module-graph-assert, root `build.gradle.kts`) | the Gradle dependency graph | module direction: `impl ↛ impl`, `api ↛ impl`, `core ↛ feature`, `↛ :app` |

## The Konsist rules

All in `konsist-test/src/test/kotlin/ru/otus/barista/konsist/`; scopes in `BaristaScope.kt` (`productionScope()` =
every `src/main`, `testScope()` = every `src/test`; `build-logic` and `build/` excluded).

| Class | Guards | Rule doc |
|---|---|---|
| `SanityKonsistTest` | the scope is not empty (≥ 40 production files, ≥ 5 test files) | — |
| `ModuleStructureKonsistTest` | `feature/<x>` = `api` + `impl`; `impl` has `domain`/`presentation`/`ui`; `api` has no `dto`/`*Impl` | architecture §1–2 |
| `LayeringKonsistTest` | `data ↛ presentation/ui`; `domain ↛ data/presentation/ui`; repository ↛ use case; use case ↛ ViewModel | architecture §2 |
| `UseCaseKonsistTest` | use cases in `domain`, one public `operator fun invoke`; **ViewModels depend on use cases only** | architecture §3–4 |
| `DomainPurityKonsistTest` | no Android/Compose UI/Retrofit/OkHttp/serialization in `domain`/`presentation`; no Android in `api`, `core:common`, `core:mvi` | architecture §5 |
| `MviConventionKonsistTest` | `@Immutable data class *State`, sealed `*Action`/`*Event`, `*ViewModel`, all in `presentation`; State without lambdas and mutable collections | architecture §6, compose §2 |
| `ComposeKonsistTest` | `@ThemePreviews` next to every `*Screen`; tags from `*TestTags`; `modifier` first optional | compose §4–6 |
| `TestConventionKonsistTest` | `WHEN … SHOULD …` names, no AAA comments, an assertion in every test, `*Test` classes next to their SUT | testing §2–4, §8 |

Every failure message ends with `see docs/rules/<file>.md §N` and lists the offending declarations with file:line.

## Run

```bash
./gradlew :konsist-test:test --rerun-tasks -q --console=plain
./gradlew assertModuleGraph -q --console=plain
```
Report: `konsist-test/build/reports/tests/test/index.html`.

## When a rule fails

**Fix the code, not the rule.** The rule encodes a decision the team already made; the failure message names it.
- ViewModel takes a repository → add a use case. Domain imports Retrofit → move the code to `data`.
  A test named `test1` → rename to `WHEN … SHOULD …`.
- Only if the rule is genuinely wrong for a new, legitimate case: change the rule **and** its `docs/rules` section in the
  same change, with the reason in the PR. There are no baselines in this project; don't add a silent exclusion list.

## Adding a rule

1. Write it as a test in the matching `*KonsistTest` (or a new `<Topic>KonsistTest`), scope through `productionScope()`/`testScope()`.
2. Use `assertTrue/assertFalse(strict = true, additionalMessage = "see docs/rules/<file>.md §N")` — `strict` fails on an
   empty selection, so a typo in a filter can't make the rule pass vacuously.
3. Prove it bites: break the code on purpose (e.g. add `MenuRepository` to `MenuViewModel`), see it red, revert.
4. Add/extend the section in `docs/rules/*.md` with "Checked by: `<Class>`".

Konsist pitfalls: parent names keep type arguments (`MviViewModel<…>`) — compare `name.substringBefore('<')`;
layering by imports can't see same-package references.

## Module graph

Rules live in `moduleGraphAssert { allowed / restricted }` in the root `build.gradle.kts`. A new edge that breaks it
(e.g. `implementation(project(":feature:menu:impl"))` in drink-details) is solved by moving the shared type to an `api`
module, not by widening the regex.
