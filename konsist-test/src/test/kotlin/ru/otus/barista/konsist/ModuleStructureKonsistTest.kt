package ru.otus.barista.konsist

import com.lemonappdev.konsist.api.verify.assertFalse
import com.lemonappdev.konsist.api.verify.assertTrue
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * `data/` and `domain/` = exactly `api` + `impl`; a feature is presentation + ui (logic lives in `domain`);
 * every module keeps to its package root; `:data:impl` hides everything behind `DataGraph`.
 */
class ModuleStructureKonsistTest {

    /** Module paths that contain production Kotlin, e.g. `data/impl`, `feature/menu`. */
    private val modules: Set<String>
        get() = productionScope().files.map { it.moduleName }.toSet()

    @Test
    fun `data and domain consist of exactly an api and an impl module`() {
        val offenders = listOf("data", "domain")
            .associateWith { layer -> modules.filter { it.startsWith("$layer/") }.toSet() }
            .filter { (layer, found) -> found != setOf("$layer/api", "$layer/impl") }

        assertTrue(
            "These layers must contain exactly api and impl modules: $offenders. See $ARCHITECTURE_DOC §1",
            offenders.isEmpty(),
        )
    }

    @Test
    fun `every feature has presentation and ui packages and no data or domain packages`() {
        val offenders = modules
            .filter { it.startsWith("feature/") && it.count { char -> char == '/' } == 1 }
            .associateWith { module ->
                val packages = packageSegmentsOf(module)
                (REQUIRED_FEATURE_PACKAGES - packages) + (FORBIDDEN_FEATURE_PACKAGES intersect packages)
            }
            .filterValues { it.isNotEmpty() }

        assertTrue(
            "These features miss presentation/ui or contain data/domain packages (move logic to :domain or to a " +
                ":feature:<name>:domain sub-module): $offenders. See $ARCHITECTURE_DOC §2",
            offenders.isEmpty(),
        )
    }

    @Test
    fun `every module keeps to its package root`() {
        productionScope().files
            .filter { file -> MODULE_PACKAGES.keys.any { file.moduleName == it } }
            .assertTrue(strict = true, additionalMessage = "see $ARCHITECTURE_DOC §1") { file ->
                val pkg = file.packagee?.name.orEmpty()
                MODULE_PACKAGES.getValue(file.moduleName).any { pkg == it || pkg.startsWith("$it.") }
            }
    }

    @Test
    fun `api modules contain no Impl classes`() {
        productionScope().files
            .filter { it.moduleName.endsWith("/api") }
            .assertFalse(
                strict = true,
                additionalMessage = "api holds contracts only: see $ARCHITECTURE_DOC §1"
            ) { file ->
                file.classes().any { it.hasNameEndingWith("Impl") }
            }
    }

    @Test
    fun `data impl declarations are internal except DataGraph`() {
        val dataImpl = productionScope().slice { it.moduleName == "data/impl" }
        val declarations = dataImpl.classesAndInterfacesAndObjects(includeNested = false) +
            dataImpl.functions(includeNested = false, includeLocal = false).filter { it.isTopLevel }

        declarations
            .filterNot { it.name == PUBLIC_DATA_ENTRY_POINT }
            .assertTrue(strict = true, additionalMessage = "only DataGraph is public: see $ARCHITECTURE_DOC §1") {
                it.hasInternalModifier || it.hasPrivateModifier
            }
    }

    private fun packageSegmentsOf(module: String): Set<String> = productionScope().files
        .filter { it.moduleName == module }
        .flatMap { it.packagee?.name.orEmpty().split('.') }
        .toSet()

    private companion object {
        val REQUIRED_FEATURE_PACKAGES = setOf("presentation", "ui")
        val FORBIDDEN_FEATURE_PACKAGES = setOf("data", "domain")
        const val PUBLIC_DATA_ENTRY_POINT = "DataGraph"
        val MODULE_PACKAGES = mapOf(
            "data/api" to listOf("ru.otus.barista.data.model", "ru.otus.barista.data.repository"),
            "data/impl" to listOf("ru.otus.barista.data.impl"),
            "domain/api" to listOf("ru.otus.barista.domain.model", "ru.otus.barista.domain.usecase"),
            "domain/impl" to listOf("ru.otus.barista.domain.impl"),
            "feature/menu" to listOf("ru.otus.barista.feature.menu"),
            "feature/drink-details" to listOf("ru.otus.barista.feature.drinkdetails"),
        )
    }
}
