package ru.otus.barista.konsist

import com.lemonappdev.konsist.api.declaration.KoFileDeclaration
import com.lemonappdev.konsist.api.verify.assertFalse
import org.junit.Test

/**
 * Layer direction inside a feature `impl`, checked by imports: ui -> presentation -> domain <- data.
 * Same-package references emit no import, so these checks are a floor, not a proof.
 */
class LayeringKonsistTest {

    private fun filesInLayer(layer: String): List<KoFileDeclaration> =
        productionScope().files.filter { it.packagee?.name.hasPackageSegment(layer) }

    private fun KoFileDeclaration.importsLayer(vararg layers: String): Boolean = imports
        .map { it.name }
        .filter { it.startsWith(PROJECT_PACKAGE) }
        .any { import -> layers.any { layer -> import.hasPackageSegment(layer) } }

    @Test
    fun `data layer does not import presentation or ui`() {
        filesInLayer("data").assertFalse(additionalMessage = "see $ARCHITECTURE_DOC §2") {
            it.importsLayer("presentation", "ui")
        }
    }

    @Test
    fun `domain layer does not import data presentation or ui`() {
        filesInLayer("domain").assertFalse(strict = true, additionalMessage = "see $ARCHITECTURE_DOC §2") {
            it.importsLayer("data", "presentation", "ui")
        }
    }

    @Test
    fun `repositories do not import use cases`() {
        productionScope().files
            .filter { it.name.contains("Repository") }
            .assertFalse(strict = true, additionalMessage = "see $ARCHITECTURE_DOC §2") { file ->
                file.imports.any { it.name.endsWith("UseCase") }
            }
    }

    @Test
    fun `use cases do not import ViewModels`() {
        productionScope().files
            .filter { it.name.removeSuffix(".kt").endsWith("UseCase") }
            .assertFalse(strict = true, additionalMessage = "see $ARCHITECTURE_DOC §2") { file ->
                file.imports.any { it.name.endsWith("ViewModel") }
            }
    }

    private companion object {
        const val PROJECT_PACKAGE = "ru.otus.barista."
    }
}
