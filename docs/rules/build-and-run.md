# Build and Run

Always `./gradlew` (the wrapper pins Gradle 9.5). JDK 17+; locally: `export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"`.

| Task | Command |
|---|---|
| **All fast moats — the definition of done** | `./gradlew moats` |
| Unit tests of one module | `./gradlew :domain:impl:testDebugUnitTest` (JVM modules: `:core:common:test`) |
| One test class / method | add `--tests "ru.otus.barista.domain.impl.usecase.FilterAndSortMenuUseCaseImplTest"` / `--tests "*.FilterAndSortMenuUseCaseImplTest.WHEN sort*"` |
| Architecture tests only | `./gradlew :konsist-test:test --rerun-tasks` |
| Module dependency rules | `./gradlew assertModuleGraph` |
| Static analysis (auto-corrects formatting) | `./gradlew detekt` |
| Verify screenshots against goldens | `./gradlew screenshots` |
| Re-record screenshot goldens | `./gradlew recordScreenshots` |
| Build / install the app | `./gradlew :app:assembleDebug` / `./gradlew :app:installDebug` |
| Install the pre-push hook | `./gradlew installGitHooks` (also runs on every app build) |

- `moats` = `detekt` → `:konsist-test:test` → `assertModuleGraph` → `fastUnitTests` (every `testDebugUnitTest`/`test`
  with `src/test`, screenshot tests excluded). Same property by hand: `-PexcludeScreenshotTests`.
- `screenshots` runs only the screenshot tests (`-PonlyScreenshotTests`). Don't combine it with `moats` in one invocation.
- `--rerun-tasks` for Konsist forces a fresh scan: the result you quote is then evidence, not a cached verdict.
- For agents and scripts add `-q --console=plain`: on success the output is empty and the exit code is the answer.
- `detekt` rewrites formatting in place (`autoCorrect`); re-read files you edited after running it.
