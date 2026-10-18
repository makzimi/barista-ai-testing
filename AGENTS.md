# Agent Instructions

Barista — a small multi-module Android app (coffee menu + drink configurator, Kotlin, Compose, MVI) that exists
to be tested: unit, integration, Compose UI, screenshot and architecture tests, all on the JVM.
Modules are strict layers: `:feature:*` → `:domain:api` ← `:domain:impl` → `:data:api` ← `:data:impl`; only `:app`
sees every `impl` ([architecture §1](docs/rules/architecture.md)).

## Documentation

All project documentation lives under `docs/`:

- **`docs/rules/`** — always-loaded conventions, `@`-included below. Short. Every rule names the check that enforces it.
- **`docs/reference/`** — on-demand guides and skill bodies. Not loaded automatically; open one when the task calls for it.
- **`docs/wip/`** — temporary working docs for a change in flight, `YYYY-MM-DD-<name>.md`. Deleted when it ships.

Every new doc goes into one of those three folders. Don't scatter `.md` files across the source tree.

Always read all rules:

@docs/rules/architecture.md
@docs/rules/testing.md
@docs/rules/compose.md
@docs/rules/build-and-run.md

## Read on demand

When the task matches, invoke the skill (or open the doc) **before** you start.

- **Unit test** for a use case, ViewModel, mapper, `Money`/`HappyHour` rule — `write-unit-tests` skill →
  [`docs/reference/write-unit-tests.md`](docs/reference/write-unit-tests.md).
  Test names: `` `<action> WHEN <input condition> SHOULD <observable outcome>` ``.
- **Integration test** (repository + Retrofit + JSON, or the whole feature graph) — `write-integration-tests` skill →
  [`docs/reference/write-integration-tests.md`](docs/reference/write-integration-tests.md).
- **Compose UI test** (clicks, callbacks, what is displayed) — `write-ui-tests` skill →
  [`docs/reference/write-ui-tests.md`](docs/reference/write-ui-tests.md).
- **Screenshot test / golden update / visual change** — `screenshot-tests` skill →
  [`docs/reference/screenshot-tests.md`](docs/reference/screenshot-tests.md).
- **Architecture rule (Konsist / module graph) fails, or a new rule is needed** — `architecture-tests` skill →
  [`docs/reference/architecture-tests.md`](docs/reference/architecture-tests.md).
- **Implementing a feature or fixing a bug** — `tdd` skill → [`docs/reference/tdd.md`](docs/reference/tdd.md).
- **About to say "done", "fixed", "passes"** — `verify-before-done` skill →
  [`docs/reference/verify-before-done.md`](docs/reference/verify-before-done.md).
- **Creating or changing a skill** — `create-skill` skill → [`docs/reference/skill-creation.md`](docs/reference/skill-creation.md).

**Nothing is done until `./gradlew moats` exits 0.** Run it yourself and quote the result; the pre-push hook and CI
run it again.
