# Writing Integration Tests

Body of the `write-integration-tests` skill. Integration tests run on the JVM like unit tests, but several real
classes collaborate and only the outer boundary is replaced. Contract: [`docs/rules/testing.md`](../rules/testing.md).

## Which kind

| Risk | Test | Module | Boundary replaced |
|---|---|---|---|
| DTO / JSON contract, Retrofit annotations, HTTP errors → `DataError`, cache/stale | **MockWebServer** test of `MenuRepositoryImpl` | `:data:impl` | the HTTP server |
| Stale banner, favorites merge, retries across **all** layers | **Sociable** test: `MenuGraph` + `DomainGraph` + `DataGraph` | `:app` | the backend (fake-backend interceptor), favorites file, clock, dispatchers |

Names: `MenuRepositoryImplTest` (same package as the SUT, `data.impl.repository`) and `<Feature>FeatureIntegrationTest`
(`ru.otus.barista.app`). The sociable test lives in `:app` because only the app may see every `impl`.

## Template A — MockWebServer (`:data:impl`, real Retrofit + OkHttp + kotlinx.serialization)

```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
class MenuRepositoryImplTest {

    @get:Rule
    val server = MockWebServer()

    private val dispatcher = UnconfinedTestDispatcher()
    private val sut by lazy {
        MenuRepositoryImpl(
            api = NetworkFactory.retrofit(server.url("/").toString()).create(MenuApi::class.java),
            cache = InMemoryMenuCache(),
            dispatchers = TestDispatcherProvider(dispatcher),
        )
    }

    @Test
    fun `getMenu WHEN force refresh fails with 500 and cache is warm SHOULD return cached menu marked stale`() =
        runTest(dispatcher) {
            server.enqueue(MockResponse().setBody(Fixtures.read("menu.json")))
            server.enqueue(MockResponse().setResponseCode(500))
            sut.getMenu(forceRefresh = false)
            val expected = MenuSnapshotData(drinks = Create.sampleMenuDto(), isStale = true)

            val result = sut.getMenu(forceRefresh = true)

            assertEquals(expected, result.getOrThrow())
        }
}
```
- `MenuApi` and `MenuRepositoryImpl` are `internal`: these tests live in `:data:impl`, nowhere else.
- Build Retrofit with the production `NetworkFactory` — the point is to test the real converter and annotations.
- JSON bodies live in `data/impl/src/test/resources/fixtures/`, read by `Fixtures.read(name)`; results are DTOs —
  compare with `Create.sampleMenuDto()` / `Create.ESPRESSO_DTO`.
- A renamed JSON field must make a test red (`DataError.Network` caused by a serialization exception) — keep one.
- No HTTP needed (cache/fallback only)? `FakeMenuApi` fakes the Retrofit interface: `MenuRepositoryImplWithFakeApiTest`.

## Template B — sociable test in `:app`

```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
class MenuFeatureIntegrationTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(StandardTestDispatcher())

    private val clock = FixedClock(time = LocalTime.of(17, 0))
    private val dependencies = TestMenuDependencies(clock, mainDispatcherRule.testDispatcher)
    private val graph = MenuGraph(dependencies)

    @Test
    fun `Refresh WHEN backend fails after a successful load SHOULD keep content and show the stale banner`() = runTest {
        val viewModel = graph.createViewModel().also { advanceUntilIdle() }
        val expected = viewModel.state.value.copy(isStale = true)
        dependencies.backend.setFailNextRequest(true)

        viewModel.onAction(MenuAction.Refresh)
        advanceUntilIdle()

        assertEquals(expected, viewModel.state.value)
    }
}
```
- `TestMenuDependencies` (`app/src/test`) builds the production `DataGraph(retrofit, dataStore, dispatchers)` +
  `DomainGraph`, exactly like `AppGraph`. `:data:impl` stays sealed: the test only hands it a Retrofit whose OkHttp
  client has the app's own `FakeBackendInterceptor` (serving `src/main/assets/fake-backend/menu.json`, added to the
  test resources) and an in-memory `DataStore`.
- OkHttp's async calls run as test-dispatcher tasks (`Dispatcher(TestDispatcherExecutorService(...))`), so
  `advanceUntilIdle()` covers the whole round trip — no real threads, no flakiness.
- Arrange failures with `dependencies.backend.setFailNextRequest(true)`; requests are recorded in `dependencies.requests`;
  stored favorites: `dependencies.storedFavoriteIds()`.
- Assert what the user would see (State, Events), not which internal method ran.

## Run

```bash
./gradlew :data:impl:testDebugUnitTest --tests "*RepositoryImpl*Test" -q --console=plain
./gradlew :app:testDebugUnitTest --tests "*IntegrationTest" -q --console=plain
./gradlew moats -q --console=plain
```
