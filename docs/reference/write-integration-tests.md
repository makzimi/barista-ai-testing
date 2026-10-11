# Writing Integration Tests

Body of the `write-integration-tests` skill. Integration tests run on the JVM like unit tests, but several real
classes collaborate and only the outer boundary is replaced. Contract: [`docs/rules/testing.md`](../rules/testing.md).

## Which kind

| Risk | Test | Boundary replaced |
|---|---|---|
| DTO / JSON contract, Retrofit annotations, HTTP errors → `MenuError` | **MockWebServer** test of `MenuRepositoryImpl` | the HTTP server |
| Stale cache, favorites merge, retries across layers | **Sociable** test through `MenuGraph` | `MenuApi`, favorites storage, clock, dispatchers |

Names: `MenuRepositoryImplTest` (same package as the SUT, `data`) and `<Feature>FeatureIntegrationTest` (feature root package).

## Template A — MockWebServer (real Retrofit + OkHttp + kotlinx.serialization)

```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
class MenuRepositoryImplTest {

    @get:Rule
    val server = MockWebServer()

    private val dispatcher = UnconfinedTestDispatcher()
    private val favorites = FakeFavoritesLocalDataSource()
    private val sut by lazy {
        MenuRepositoryImpl(
            api = NetworkFactory.retrofit(server.url("/").toString()).create(MenuApi::class.java),
            cache = InMemoryMenuCache(),
            favorites = favorites,
            dispatchers = TestDispatcherProvider(dispatcher),
        )
    }

    @Test
    fun `getMenu WHEN force refresh fails with 500 and cache is warm SHOULD return cached menu marked stale`() =
        runTest(dispatcher) {
            server.enqueue(MockResponse().setBody(Fixtures.read("menu.json")))
            server.enqueue(MockResponse().setResponseCode(500))
            sut.getMenu(forceRefresh = false)

            val result = sut.getMenu(forceRefresh = true)

            assertEquals(true, result.getOrThrow().isStale)
        }
}
```
- Build Retrofit with the production `NetworkFactory` — the point is to test the real converter and annotations.
- JSON bodies live in `src/test/resources/fixtures/` and are read by a tiny `Fixtures.read(name)` helper; copy the shape of
  `app/src/main/assets/fake-backend/menu.json`.
- Assert the request too when the contract matters: `assertEquals("/menu", server.takeRequest().path)`.
- A renamed JSON field must make a test red (`MenuError.Network` caused by a serialization exception) — keep one such test.

## Template B — sociable feature test through `MenuGraph`

```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
class MenuFeatureIntegrationTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(StandardTestDispatcher())

    private val api = FakeMenuApi()
    private val favorites = FakeFavoritesLocalDataSource()
    private val clock = FixedClock(time = LocalTime.of(17, 0))
    private val graph = MenuGraph(
        TestMenuDependencies(api, favorites, clock, TestDispatcherProvider(mainDispatcherRule.testDispatcher)),
    )

    @Test
    fun `Refresh WHEN backend fails after a successful load SHOULD keep content and show the stale banner`() = runTest {
        val viewModel = graph.createViewModel().also { advanceUntilIdle() }
        api.failNext = IOException("offline")

        viewModel.onAction(MenuAction.Refresh)
        advanceUntilIdle()

        assertEquals(true, viewModel.state.value.isStale)
    }
}
```
- `TestMenuDependencies` wires the real `MenuRepositoryImpl` + `InMemoryMenuCache`; only `MenuApi`, favorites,
  clock and dispatchers are fakes. Everything between them is production code.
- `FakeMenuApi` fails loudly for endpoints the scenario did not arrange (`unarranged(endpoint)`), so a test never
  passes on accidental defaults.
- Assert what the user would see (State, Events), not which internal method ran.

## Run

```bash
./gradlew :feature:menu:impl:testDebugUnitTest --tests "*IntegrationTest" --tests "*RepositoryImplTest" -q --console=plain
./gradlew moats -q --console=plain
```
