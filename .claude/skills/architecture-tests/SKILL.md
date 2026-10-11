---
name: architecture-tests
description: 'Understand, fix and extend Barista architecture tests: Konsist rules in :konsist-test (layering, use cases, ViewModels depend on use cases only, domain purity, MVI, Compose, test conventions) and assertModuleGraph. Trigger when a Konsist test or assertModuleGraph fails, when adding a dependency between modules, moving a class between layers, or adding an architecture rule — English "Konsist failed", "architecture test", "module graph", "assertModuleGraph failed", "add an architecture rule", "layering violation", "can I depend on"; Russian "упал Konsist", "архитектурный тест", "граф модулей", "добавь архитектурное правило", "нарушение слоёв", "можно ли зависеть от". Also trigger when editing anything under konsist-test/ or the moduleGraphAssert block.'
allowed-tools:
  - Bash(./gradlew *)
  - Bash(grep *)
  - Bash(find *)
---

Read and follow `docs/reference/architecture-tests.md`.
