# Test-Driven Development

Body of the `tdd` skill. It governs the **cycle** — when you may write which code. The craft of a good test is in
`write-unit-tests` / `write-integration-tests` / `write-ui-tests`.

## The Iron Law

```
NO PRODUCTION CODE WITHOUT A FAILING TEST FIRST
```
Wrote code before the test? Delete it and start from the test. Don't keep it "as reference".

## Red → Green → Refactor

| Step | Do | Proof |
|---|---|---|
| **RED** | one test for one behavior, named `<action> WHEN <condition> SHOULD <outcome>` | — |
| **VERIFY RED** | run it | it **fails**, for the expected reason (missing behavior / wrong value), not a typo or compile error in the test |
| **GREEN** | the minimal production code that makes it pass | — |
| **VERIFY GREEN** | run the class, then the module | all green |
| **REFACTOR** | clean names, remove duplication, keep behavior | still green |
| **REPEAT** | next behavior | — |

```bash
./gradlew :feature:drink-details:impl:testDebugUnitTest --tests "*.ChangeAddOnQuantityUseCaseTest" -q --console=plain
```

- Test passes on the first run → it tests existing behavior or nothing. Fix the test.
- Bug fix: first a test that reproduces the bug (red), then the fix (green). The test stays as the regression guard.
- Example (Barista): "max 5 identical add-ons" → red test in `ChangeAddOnQuantityUseCaseTest`
  `` `change WHEN quantity is 5 and delta is 1 SHOULD keep 5` `` → change the limit → green → update the mapper test for
  `limitReached` → `./gradlew moats`.

## When it doesn't apply (ask the human first)

Throwaway spikes, build/config files (`*.gradle.kts`, `libs.versions.toml`), pure layout tweaks (covered by
screenshot goldens instead: add/adjust a preview, re-record, review the diff).

## Red flags — stop and restart the cycle

"I'll add tests after", "it's too simple to test", "I already checked manually", a test that was never red,
mocks verifying calls instead of results, changing the assertion until it passes.

## Done

Every new behavior has a test that was seen red, everything is green, and `./gradlew moats` exits 0
(see `verify-before-done`).
