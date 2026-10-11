---
name: verify-before-done
description: 'Verify before claiming completion: run ./gradlew moats (detekt, Konsist, module graph, all JVM tests), read the exit code, quote the evidence; screenshots for UI changes. Trigger before ANY claim that work is done, fixed, passing or ready — and when the user asks "is it done", "does it work", "are tests green", "ready to push", "check everything", "run the checks"; Russian "готово?", "работает?", "тесты зелёные?", "можно пушить", "проверь всё", "прогони проверки", "запусти moats". Also trigger before committing, pushing or reporting a finished task.'
allowed-tools:
  - Bash(./gradlew *)
  - Bash(grep *)
  - Bash(find *)
---

Read and follow `docs/reference/verify-before-done.md`.
