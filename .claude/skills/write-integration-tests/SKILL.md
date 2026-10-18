---
name: write-integration-tests
description: 'Write integration tests for Barista: MenuRepositoryImpl against MockWebServer with the real Retrofit/OkHttp/kotlinx.serialization stack, and sociable tests in :app through MenuGraph + DomainGraph + DataGraph with the app fake backend. Trigger when the user wants to test a repository, DTO, JSON contract, Retrofit API, cache/stale behavior or the whole feature chain — English "integration test", "test the repository", "test the API contract", "MockWebServer", "test the DTO", "test the whole feature", "sociable test"; Russian "интеграционный тест", "протестируй репозиторий", "проверь контракт API", "тест на DTO", "тест на JSON", "проверь всю цепочку", "социальный тест". Also trigger when changing anything in :data:api / :data:impl (DTOs, MenuApi, MenuRepositoryImpl, cache) or creating *IntegrationTest.kt / *RepositoryImplTest.kt.'
allowed-tools:
  - Bash(./gradlew *)
  - Bash(grep *)
  - Bash(find *)
---

Read and follow `docs/reference/write-integration-tests.md`.
