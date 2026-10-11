# Verify Before Done

Body of the `verify-before-done` skill.

```
NO COMPLETION CLAIMS WITHOUT FRESH VERIFICATION EVIDENCE
```
If you haven't run the command in this turn, you can't say it passes.

## The gate

1. **Identify** the command that proves the claim.
2. **Run** it fully and freshly.
3. **Read** the output and the exit code; count failures.
4. **Claim** only what the output shows — and quote it.

## The command for this project

```bash
./gradlew moats -q --console=plain; echo "exit=$?"
```
`moats` = detekt → Konsist → module graph → all JVM unit/integration/UI tests. Empty output + `exit=0` = green.
Touched UI? Also `./gradlew screenshots -q --console=plain` (and review any diff — `screenshot-tests` skill).
Touched only architecture rules? `./gradlew :konsist-test:test --rerun-tasks`.

| Claim | Requires | Not enough |
|---|---|---|
| "Tests pass" | `moats` exit 0 in this turn | an earlier run, "should pass" |
| "Bug fixed" | the reproducing test was red, now green | "changed the code" |
| "Lint clean" | `./gradlew detekt` exit 0, files re-read after auto-correct | "I formatted it" |
| "UI unchanged" | `./gradlew screenshots` exit 0 | "it compiled" |
| "Subagent finished" | the diff and a fresh `moats` | the subagent's report |

## Red flags

"should", "probably", "seems to", "Done!" before running anything, partial runs presented as full, a red result
explained away as "unrelated" without evidence.

## How to report

```
./gradlew moats -q --console=plain → exit=0 (detekt, Konsist, assertModuleGraph, fastUnitTests)
```
If something is red, say exactly what and why — never round a partial run up to "all green".
