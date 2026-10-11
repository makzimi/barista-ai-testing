---
name: write-unit-tests
description: 'Write Kotlin unit tests for Barista (use cases, ViewModels, mappers, Money/HappyHour rules) with Create, FakeMenuRepository, FixedClock, MainDispatcherRule and Turbine. Trigger whenever the user wants to write, add, create, update or fix unit tests in any phrasing — English "write tests", "write a unit test", "add a test for", "cover X with tests", "test this class", "unit tests", "fix the test", "test coverage"; Russian "напиши тесты", "напиши юнит-тест", "добавь тест", "покрой тестами", "тест на X", "почини тест", "поправь тесты". Also trigger when creating or modifying any *Test.kt under src/test/, and whenever production logic in domain/ or presentation/ changes (a rule change needs a test in the same change).'
allowed-tools:
  - Bash(./gradlew *)
  - Bash(grep *)
  - Bash(find *)
---

Read and follow `docs/reference/write-unit-tests.md`.
