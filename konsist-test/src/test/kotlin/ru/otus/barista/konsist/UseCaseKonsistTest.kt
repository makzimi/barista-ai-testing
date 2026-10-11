package ru.otus.barista.konsist

import com.lemonappdev.konsist.api.verify.assertFalse
import com.lemonappdev.konsist.api.verify.assertTrue
import org.junit.Test

/** Use cases are single-operation classes in `domain`; ViewModels talk to the data layer only through them. */
class UseCaseKonsistTest {

    private val useCases
        get() = productionScope().classes().filter { it.hasNameEndingWith("UseCase") }

    @Test
    fun `UseCase classes reside in a domain package`() {
        useCases.assertTrue(strict = true, additionalMessage = "see $ARCHITECTURE_DOC §4") {
            it.resideInPackage("..domain..")
        }
    }

    @Test
    fun `UseCase classes expose exactly one public operator invoke`() {
        useCases.assertTrue(strict = true, additionalMessage = "see $ARCHITECTURE_DOC §4") { useCase ->
            val publicFunctions = useCase.functions(includeNested = false, includeLocal = false)
                .filter { it.hasPublicOrDefaultModifier }
            publicFunctions.size == 1 &&
                publicFunctions.single().name == "invoke" &&
                publicFunctions.single().hasOperatorModifier
        }
    }

    @Test
    fun `ViewModels depend on use cases only`() {
        productionScope().classes()
            .filter { it.hasNameEndingWith("ViewModel") }
            .flatMap { it.primaryConstructor?.parameters.orEmpty() }
            .assertFalse(additionalMessage = "inject a use case instead: see $ARCHITECTURE_DOC §3") { parameter ->
                val typeName = parameter.type.text.removeSuffix("?").substringBefore('<')
                DATA_LAYER_SUFFIXES.any { typeName.endsWith(it) }
            }
    }

    private companion object {
        val DATA_LAYER_SUFFIXES = listOf("Repository", "Api", "DataSource", "Cache")
    }
}
