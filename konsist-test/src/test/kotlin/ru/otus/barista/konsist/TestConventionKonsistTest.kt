package ru.otus.barista.konsist

import com.lemonappdev.konsist.api.container.KoScope
import com.lemonappdev.konsist.api.declaration.KoClassDeclaration
import com.lemonappdev.konsist.api.declaration.KoFunctionDeclaration
import com.lemonappdev.konsist.api.verify.assertFalse
import com.lemonappdev.konsist.api.verify.assertTrue
import org.junit.Test

/**
 * Rules for the tests themselves. Konsist's own tests are excluded: they are named as descriptive sentences.
 * Generated Roborazzi preview tests live under build/ and never reach the scope.
 */
class TestConventionKonsistTest {

    private val appTests: KoScope
        get() = testScope().slice { it.moduleName != "konsist-test" }

    private val testFunctions: List<KoFunctionDeclaration>
        get() = appTests.functions().filter { it.hasAnnotationWithName("Test") }

    private val testClasses: List<KoClassDeclaration>
        get() = appTests.classes(includeLocal = false)
            .filter { clazz -> clazz.functions(includeNested = false).any { it.hasAnnotationWithName("Test") } }

    @Test
    fun `test names contain WHEN and SHOULD`() {
        testFunctions
            .filterNot { it.packagee?.name.hasPackageSegment("workshop") }
            .assertTrue(
                additionalMessage = "name it `<action> WHEN <condition> SHOULD <outcome>`: see $TESTING_DOC §2"
            ) {
                WHEN_SHOULD.containsMatchIn(it.name)
            }
    }

    @Test
    fun `tests have no Arrange Act Assert or Given When Then comments`() {
        appTests.files.assertFalse(additionalMessage = "separate the phases with blank lines: see $TESTING_DOC §3") {
            PHASE_COMMENT.containsMatchIn(it.text)
        }
    }

    @Test
    fun `every test asserts something`() {
        testFunctions.assertTrue(additionalMessage = "a test without an assertion always passes: see $TESTING_DOC §4") {
            ASSERTION.containsMatchIn(it.text)
        }
    }

    @Test
    fun `classes with tests are named Test`() {
        testClasses.assertTrue(additionalMessage = "see $TESTING_DOC §8") {
            it.hasNameEndingWith("Test")
        }
    }

    @Test
    fun `tests reside in the package of the class they test`() {
        val productionPackages: Map<String, Set<String>> = productionDeclarationPackages()

        testClasses
            .filterNot { it.packagee?.name.hasPackageSegment("workshop") }
            .assertTrue(additionalMessage = "move the test next to its SUT: see $TESTING_DOC §8") { testClass ->
                val sutPackages = sutCandidates(testClass.name).firstNotNullOfOrNull { productionPackages[it] }
                sutPackages == null || testClass.packagee?.name in sutPackages
            }
    }

    /** `MenuScreenTest` -> [MenuScreen, Menu]: the longest name that exists in production wins. */
    private fun sutCandidates(testClassName: String): List<String> = TEST_SUFFIXES
        .filter { testClassName.endsWith(it) }
        .map { testClassName.removeSuffix(it) }
        .filter { it.isNotEmpty() }
        .sortedByDescending { it.length }

    private fun productionDeclarationPackages(): Map<String, Set<String>> {
        val scope = productionScope()
        val named = scope.classesAndInterfacesAndObjects().map { it.name to it.packagee?.name } +
            scope.functions(includeNested = false, includeLocal = false)
                .filter { it.isTopLevel }
                .map { it.name to it.packagee?.name }
        return named
            .filter { (_, pkg) -> pkg != null }
            .groupBy({ it.first }, { it.second.orEmpty() })
            .mapValues { it.value.toSet() }
    }

    private companion object {
        val WHEN_SHOULD = Regex("""(^| )WHEN .+ SHOULD .+""")
        val PHASE_COMMENT = Regex("""(?m)^\s*//\s*(Arrange|Act|Assert|Given|When|Then)\b""")
        val ASSERTION = Regex("""assert|await|verify|expect|captureRoboImage|captureScreenshot""")
        val TEST_SUFFIXES = listOf(
            "Test",
            "WorkshopTest",
            "ScreenshotTest",
            "ScreenTest",
            "IntegrationTest",
            "MockkTest",
        )
    }
}
