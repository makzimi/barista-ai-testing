package ru.otus.barista.konsist

import com.lemonappdev.konsist.api.declaration.KoFunctionDeclaration
import com.lemonappdev.konsist.api.verify.assertFalse
import com.lemonappdev.konsist.api.verify.assertTrue
import org.junit.Test

/** Screens are previewable (and therefore screenshot-tested), test tags are shared constants. */
class ComposeKonsistTest {

    private val composables: List<KoFunctionDeclaration>
        get() = productionScope().functions().filter { it.hasAnnotationWithName("Composable") }

    @Test
    fun `every public Screen composable has a ThemePreviews preview in the same file`() {
        composables
            .filter { it.hasPublicOrDefaultModifier && it.name.endsWith("Screen") }
            .assertTrue(
                strict = true,
                additionalMessage = "add a @ThemePreviews *Preview: see $COMPOSE_DOC §4"
            ) { screen ->
                screen.containingFile.functions().any { sibling ->
                    sibling.hasAnnotationWithName("ThemePreviews") && sibling.name.endsWith("Preview")
                }
            }
    }

    @Test
    fun `testTag is called with constants from a TestTags object, never a string literal`() {
        productionScope().files.assertFalse(additionalMessage = "declare the tag in *TestTags: see $COMPOSE_DOC §5") {
            STRING_LITERAL_TEST_TAG.containsMatchIn(it.text)
        }
    }

    @Test
    fun `TestTags objects of feature modules reside in the ui package`() {
        productionScope().objects()
            .filter { it.hasNameEndingWith("TestTags") && it.moduleName.startsWith("feature/") }
            .assertTrue(strict = true, additionalMessage = "see $COMPOSE_DOC §5") {
                it.resideInPackage("..ui..")
            }
    }

    @Test
    fun `modifier is the first optional parameter and defaults to Modifier`() {
        composables
            .filter { function -> function.parameters.any { it.name == "modifier" } }
            .assertTrue(additionalMessage = "see $COMPOSE_DOC §6") { function ->
                val modifierIndex = function.parameters.indexOfFirst { it.name == "modifier" }
                val modifier = function.parameters[modifierIndex]
                modifier.defaultValue == "Modifier" &&
                    function.parameters.take(modifierIndex).none { it.defaultValue != null }
            }
    }

    private companion object {
        val STRING_LITERAL_TEST_TAG = Regex("""testTag\(\s*"""")
    }
}
