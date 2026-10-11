package ru.otus.barista.konsist

import com.lemonappdev.konsist.api.declaration.KoClassDeclaration
import com.lemonappdev.konsist.api.provider.KoParentProvider
import com.lemonappdev.konsist.api.verify.assertFalse
import com.lemonappdev.konsist.api.verify.assertTrue
import org.junit.Test

/** MVI contract: `*State`, `*Action`, `*Event`, `*ViewModel`. See docs/rules/architecture.md §6 and compose.md §2. */
class MviConventionKonsistTest {

    private val screenStates
        get() = productionScope().classes().filter { it.hasParentWithName("MviState") }

    /** The screen State plus every class it is built from: `*State` classes of the presentation layer. */
    private val stateShapedClasses: List<KoClassDeclaration>
        get() = productionScope().classes()
            .filter { it.hasParentWithName("MviState") || it.isPresentationState() }

    @Test
    fun `classes implementing MviState are immutable data classes named State`() {
        screenStates.assertTrue(strict = true, additionalMessage = "see $ARCHITECTURE_DOC §6") {
            it.hasNameEndingWith("State") && it.hasDataModifier && it.hasAnnotationWithName("Immutable")
        }
    }

    @Test
    fun `interfaces implementing MviAction are sealed and named Action`() {
        productionScope().interfaces()
            .filter { it.hasParentWithName("MviAction") }
            .assertTrue(strict = true, additionalMessage = "see $ARCHITECTURE_DOC §6") {
                it.hasSealedModifier && it.hasNameEndingWith("Action")
            }
    }

    @Test
    fun `interfaces implementing MviEvent are sealed and named Event`() {
        productionScope().interfaces()
            .filter { it.hasParentWithName("MviEvent") }
            .assertTrue(strict = true, additionalMessage = "see $ARCHITECTURE_DOC §6") {
                it.hasSealedModifier && it.hasNameEndingWith("Event")
            }
    }

    @Test
    fun `State Action Event and ViewModel reside in a presentation package`() {
        val mviTypes = productionScope().classesAndInterfaces()
            .filter { it.hasParentWithName("MviState", "MviAction", "MviEvent") || it.extendsMviViewModel() }

        mviTypes.assertTrue(strict = true, additionalMessage = "see $ARCHITECTURE_DOC §6") {
            it.resideInPackage("..presentation..")
        }
    }

    @Test
    fun `subclasses of MviViewModel are named ViewModel`() {
        productionScope().classes()
            .filter { it.extendsMviViewModel() }
            .assertTrue(strict = true, additionalMessage = "see $ARCHITECTURE_DOC §6") {
                it.hasNameEndingWith("ViewModel")
            }
    }

    @Test
    fun `State classes have no function-typed properties`() {
        stateShapedClasses
            .flatMap { it.properties(includeNested = false) }
            .assertFalse(
                additionalMessage = "State carries data, callbacks stay composable parameters: see $COMPOSE_DOC §2"
            ) {
                it.type?.isFunctionType == true
            }
    }

    @Test
    fun `State collections are immutable`() {
        stateShapedClasses
            .flatMap { it.properties(includeNested = false) }
            .assertFalse(additionalMessage = "use ImmutableList / ImmutableSet / ImmutableMap: see $COMPOSE_DOC §2") {
                it.type?.text?.substringBefore('<')?.removeSuffix("?")?.trim() in MUTABLE_COLLECTION_TYPES
            }
    }

    /** Konsist keeps type arguments in parent names: `MviViewModel<MenuState, MenuAction, MenuEvent>`. */
    private fun KoParentProvider.extendsMviViewModel(): Boolean =
        parents().any { it.name.substringBefore('<') == "MviViewModel" }

    private fun KoClassDeclaration.isPresentationState(): Boolean =
        resideInPackage("..presentation..") && (hasNameEndingWith("State") || hasNameEndingWith("Status"))

    private companion object {
        val MUTABLE_COLLECTION_TYPES = setOf(
            "List",
            "MutableList",
            "ArrayList",
            "Set",
            "MutableSet",
            "HashSet",
            "Map",
            "MutableMap",
            "HashMap",
            "Collection",
            "Iterable",
            "Array",
        )
    }
}
