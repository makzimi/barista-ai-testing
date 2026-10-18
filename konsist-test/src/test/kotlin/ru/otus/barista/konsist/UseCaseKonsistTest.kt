package ru.otus.barista.konsist

import com.lemonappdev.konsist.api.declaration.KoFunctionDeclaration
import com.lemonappdev.konsist.api.verify.assertEmpty
import com.lemonappdev.konsist.api.verify.assertFalse
import com.lemonappdev.konsist.api.verify.assertTrue
import org.junit.Test

/**
 * Use cases are interfaces in `:domain:api` (the seam features and their tests see) with one `*UseCaseImpl` each
 * in `:domain:impl`. `:core:testing` is test support: its `Fake*UseCase` classes are not checked here.
 */
class UseCaseKonsistTest {

    private val domainScope
        get() = productionScope().slice { it.moduleName != "core/testing" }

    private val useCaseInterfaces
        get() = domainScope.interfaces().filter { it.hasNameEndingWith("UseCase") }

    private val useCaseImpls
        get() = domainScope.classes().filter { it.hasNameEndingWith("UseCaseImpl") }

    @Test
    fun `UseCase types are interfaces, never classes`() {
        domainScope.classes()
            .filter { it.hasNameEndingWith("UseCase") }
            .assertEmpty(
                additionalMessage = "make it an interface in :domain:api + a *UseCaseImpl: see $ARCHITECTURE_DOC §4"
            )
    }

    @Test
    fun `UseCase interfaces reside in the domain usecase package`() {
        useCaseInterfaces.assertTrue(strict = true, additionalMessage = "see $ARCHITECTURE_DOC §4") {
            it.resideInPackage(USE_CASE_PACKAGE)
        }
    }

    @Test
    fun `UseCaseImpl classes reside in the domain impl usecase package and implement their UseCase`() {
        useCaseImpls.assertTrue(strict = true, additionalMessage = "see $ARCHITECTURE_DOC §4") { impl ->
            impl.resideInPackage(USE_CASE_IMPL_PACKAGE) &&
                impl.parents().map { it.name }.contains(impl.name.removeSuffix("Impl"))
        }
    }

    @Test
    fun `UseCase classes expose exactly one public operator invoke`() {
        useCaseInterfaces.assertTrue(strict = true, additionalMessage = "see $ARCHITECTURE_DOC §4") {
            it.functions(includeNested = false).isSingleOperatorInvoke()
        }
        useCaseImpls.assertTrue(strict = true, additionalMessage = "see $ARCHITECTURE_DOC §4") { useCase ->
            useCase.functions(includeNested = false, includeLocal = false)
                .filter { it.hasPublicOrDefaultModifier }
                .isSingleOperatorInvoke()
        }
    }

    @Test
    fun `ViewModels depend on use cases only`() {
        productionScope().classes()
            .filter { it.hasNameEndingWith("ViewModel") }
            .flatMap { it.primaryConstructor?.parameters.orEmpty() }
            .assertFalse(
                additionalMessage = "inject a use case interface instead: see $ARCHITECTURE_DOC §3"
            ) { parameter ->
                val typeName = parameter.type.text.removeSuffix("?").substringBefore('<')
                FORBIDDEN_VIEW_MODEL_DEPENDENCY_SUFFIXES.any { typeName.endsWith(it) }
            }
    }

    private fun List<KoFunctionDeclaration>.isSingleOperatorInvoke(): Boolean =
        size == 1 && single().name == "invoke" && single().hasOperatorModifier

    private companion object {
        const val USE_CASE_PACKAGE = "ru.otus.barista.domain.usecase"
        const val USE_CASE_IMPL_PACKAGE = "ru.otus.barista.domain.impl.usecase"
        val FORBIDDEN_VIEW_MODEL_DEPENDENCY_SUFFIXES = listOf("Repository", "Api", "DataSource", "Cache", "UseCaseImpl")
    }
}
