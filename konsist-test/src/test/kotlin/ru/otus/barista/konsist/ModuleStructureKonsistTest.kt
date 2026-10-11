package ru.otus.barista.konsist

import com.lemonappdev.konsist.api.verify.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** `feature/<name>/` = exactly `api` + `impl`; `impl` is split into layers; `api` holds contracts only. */
class ModuleStructureKonsistTest {

    /** Module paths that contain production Kotlin, e.g. `feature/menu/impl`. */
    private val featureModules: Set<String>
        get() = productionScope().files.map { it.moduleName }.filter { it.startsWith("feature/") }.toSet()

    @Test
    fun `every feature consists of exactly an api and an impl module`() {
        val offenders = featureModules
            .groupBy({ it.split('/')[1] }, { it.removePrefix("feature/").substringAfter('/') })
            .filterValues { it.toSet() != setOf("api", "impl") }

        assertTrue(
            "These features must contain exactly api and impl modules: $offenders. See $ARCHITECTURE_DOC §1",
            offenders.isEmpty(),
        )
    }

    @Test
    fun `every feature impl has domain presentation and ui packages`() {
        val offenders = featureModules
            .filter { it.endsWith("/impl") }
            .associateWith { module -> REQUIRED_IMPL_LAYERS - layersOf(module) }
            .filterValues { it.isNotEmpty() }

        assertTrue(
            "These impl modules miss layer packages: $offenders. See $ARCHITECTURE_DOC §2",
            offenders.isEmpty(),
        )
    }

    @Test
    fun `feature api modules contain no dto packages and no Impl classes`() {
        productionScope().files
            .filter { it.moduleName.startsWith("feature/") && it.moduleName.endsWith("/api") }
            .assertFalse(
                strict = true,
                additionalMessage = "api holds contracts only: see $ARCHITECTURE_DOC §1"
            ) { file ->
                file.packagee?.name.hasPackageSegment("dto") ||
                    file.classes().any { it.hasNameEndingWith("Impl") }
            }
    }

    private fun layersOf(module: String): Set<String> = productionScope().files
        .filter { it.moduleName == module }
        .flatMap { it.packagee?.name.orEmpty().split('.') }
        .toSet()

    private companion object {
        val REQUIRED_IMPL_LAYERS = setOf("domain", "presentation", "ui")
    }
}
