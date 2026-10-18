package ru.otus.barista.konsist.workshop

import com.lemonappdev.konsist.api.verify.assertFalse
import org.junit.Test
import ru.otus.barista.konsist.productionScope

/**
 * Live-coding copy of the "ViewModels depend on use cases only" rule (LIVE_CODING_SCRIPT Part 5).
 * Uncomment the region and run it. A feature cannot even import `MenuRepository` any more (the module graph forbids
 * `:feature -> :data`), so break it from inside the feature: declare `class RecentDrinksCache` in
 * `feature/menu/.../presentation` and add it to the `MenuViewModel` constructor — only Konsist catches that.
 */
class LayeringKonsistWorkshopTest {

    private val dataLayerSuffixes = listOf("Repository", "Api", "DataSource", "Cache", "UseCaseImpl")

    // region Step 1 — ViewModels depend on use cases only
    // @Test
    // fun `ViewModels depend on use cases only`() {
    //     productionScope().classes()
    //         .filter { it.hasNameEndingWith("ViewModel") }
    //         .flatMap { it.primaryConstructor?.parameters.orEmpty() }
    //         .assertFalse(additionalMessage = "inject a use case instead: see docs/rules/architecture.md §3") {
    //             dataLayerSuffixes.any { suffix -> it.type.name.endsWith(suffix) }
    //         }
    // }
    // endregion
}
