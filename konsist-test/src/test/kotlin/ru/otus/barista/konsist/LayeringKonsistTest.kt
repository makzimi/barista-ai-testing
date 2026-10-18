package ru.otus.barista.konsist

import com.lemonappdev.konsist.api.declaration.KoFileDeclaration
import com.lemonappdev.konsist.api.verify.assertFalse
import org.junit.Test

/**
 * Strict layers, checked by imports per module (`moduleName` = path, e.g. `data/impl`): feature → domain → data.
 * The module graph (`assertModuleGraph`) is the first line of defence; this is the second one, at source level.
 */
class LayeringKonsistTest {

    private fun filesInModules(prefix: String): List<KoFileDeclaration> =
        productionScope().files.filter { it.moduleName == prefix || it.moduleName.startsWith("$prefix/") }

    private fun KoFileDeclaration.importsAny(vararg packages: String): Boolean =
        imports.any { import -> packages.any { import.name == it || import.name.startsWith("$it.") } }

    @Test
    fun `data modules import nothing from domain or feature`() {
        filesInModules("data").assertFalse(strict = true, additionalMessage = "see $ARCHITECTURE_DOC §2") {
            it.importsAny(DOMAIN_PACKAGE, FEATURE_PACKAGE)
        }
    }

    @Test
    fun `domain api imports nothing from data`() {
        filesInModules("domain/api").assertFalse(strict = true, additionalMessage = "see $ARCHITECTURE_DOC §2") {
            it.importsAny(DATA_PACKAGE)
        }
    }

    @Test
    fun `domain modules import nothing from feature`() {
        filesInModules("domain").assertFalse(strict = true, additionalMessage = "see $ARCHITECTURE_DOC §2") {
            it.importsAny(FEATURE_PACKAGE)
        }
    }

    @Test
    fun `feature modules import nothing from data or domain impl`() {
        filesInModules("feature").assertFalse(strict = true, additionalMessage = "see $ARCHITECTURE_DOC §2") {
            it.importsAny(DATA_PACKAGE, DOMAIN_IMPL_PACKAGE)
        }
    }

    @Test
    fun `repositories do not import use cases`() {
        productionScope().files
            .filter { it.name.contains("Repository") }
            .assertFalse(strict = true, additionalMessage = "see $ARCHITECTURE_DOC §2") { file ->
                file.imports.any { it.name.endsWith("UseCase") || it.name.endsWith("UseCaseImpl") }
            }
    }

    @Test
    fun `use cases do not import ViewModels`() {
        productionScope().files
            .filter { file -> USE_CASE_SUFFIXES.any { file.name.removeSuffix(".kt").endsWith(it) } }
            .assertFalse(strict = true, additionalMessage = "see $ARCHITECTURE_DOC §2") { file ->
                file.imports.any { it.name.endsWith("ViewModel") }
            }
    }

    private companion object {
        const val DATA_PACKAGE = "ru.otus.barista.data"
        const val DOMAIN_PACKAGE = "ru.otus.barista.domain"
        const val DOMAIN_IMPL_PACKAGE = "ru.otus.barista.domain.impl"
        const val FEATURE_PACKAGE = "ru.otus.barista.feature"
        val USE_CASE_SUFFIXES = listOf("UseCase", "UseCaseImpl")
    }
}
