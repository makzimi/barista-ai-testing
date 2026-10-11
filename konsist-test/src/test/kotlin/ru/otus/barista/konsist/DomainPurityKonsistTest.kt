package ru.otus.barista.konsist

import com.lemonappdev.konsist.api.declaration.KoFileDeclaration
import com.lemonappdev.konsist.api.verify.assertFalse
import org.junit.Test

/** Business logic stays plain Kotlin: no Android, no UI toolkit, no network or serialization library. */
class DomainPurityKonsistTest {

    private fun KoFileDeclaration.importsAny(prefixes: List<String>, allowed: Set<String> = emptySet()): Boolean =
        imports.map { it.name }
            .filterNot { it in allowed }
            .any { import -> prefixes.any { import.startsWith(it) } }

    @Test
    fun `domain and presentation import no Android, Compose UI, Retrofit, OkHttp or serialization`() {
        productionScope().files
            .filter { file ->
                val pkg = file.packagee?.name
                pkg.hasPackageSegment("domain") || pkg.hasPackageSegment("presentation")
            }
            .assertFalse(strict = true, additionalMessage = "see $ARCHITECTURE_DOC §5") {
                it.importsAny(LOGIC_FORBIDDEN_PREFIXES, allowed = COMPOSE_STABILITY_ANNOTATIONS)
            }
    }

    @Test
    fun `feature api modules and core common or mvi import nothing from Android`() {
        productionScope().files
            .filter { file -> file.moduleName.isFeatureApi() || file.moduleName in PURE_CORE_MODULES }
            .assertFalse(strict = true, additionalMessage = "see $ARCHITECTURE_DOC §5") {
                it.importsAny(ANDROID_PREFIXES, allowed = setOf(VIEW_MODEL_BASE))
            }
    }

    private fun String.isFeatureApi(): Boolean = startsWith("feature/") && endsWith("/api")

    private companion object {
        val LOGIC_FORBIDDEN_PREFIXES = listOf(
            "android.",
            "androidx.compose.",
            "retrofit2.",
            "okhttp3.",
            "kotlinx.serialization.",
        )
        val ANDROID_PREFIXES = listOf("android.", "androidx.")
        val COMPOSE_STABILITY_ANNOTATIONS = setOf(
            "androidx.compose.runtime.Immutable",
            "androidx.compose.runtime.Stable",
        )

        /** `MviViewModel` extends the Jetpack ViewModel so that screens get `viewModelScope` and survive rotation. */
        const val VIEW_MODEL_BASE = "androidx.lifecycle.ViewModel"
        val PURE_CORE_MODULES = setOf("core/common", "core/mvi")
    }
}
