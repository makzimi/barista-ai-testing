package ru.otus.barista.konsist.workshop

import com.lemonappdev.konsist.api.verify.assertFalse
import org.junit.Test
import ru.otus.barista.konsist.productionScope

/**
 * Live-coding copy of the "ViewModels depend on use cases only" rule (LIVE_CODING_SCRIPT Part 5).
 * Uncomment the region, run it, then add `MenuRepository` to the `MenuViewModel` constructor to see it fail.
 */
class LayeringKonsistWorkshopTest {

    private val dataLayerSuffixes = listOf("Repository", "Api", "DataSource", "Cache")

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
