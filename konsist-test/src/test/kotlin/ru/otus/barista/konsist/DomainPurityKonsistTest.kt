package ru.otus.barista.konsist

import com.lemonappdev.konsist.api.declaration.KoFileDeclaration
import com.lemonappdev.konsist.api.verify.assertFalse
import org.junit.Test

/** Business logic and data contracts stay plain Kotlin: no Android, no UI toolkit, no network library in the domain. */
class DomainPurityKonsistTest {

    private fun KoFileDeclaration.importsAny(prefixes: List<String>, allowed: Set<String> = emptySet()): Boolean =
        imports.map { it.name }
            .filterNot { it in allowed }
            .any { import -> prefixes.any { import.startsWith(it) } }

    @Test
    fun `domain and presentation import no Android, Compose UI, Retrofit, OkHttp or serialization`() {
        productionScope().files
            .filter { file ->
                file.moduleName.startsWith("domain/") || file.packagee?.name.hasPackageSegment("presentation")
            }
            .assertFalse(strict = true, additionalMessage = "see $ARCHITECTURE_DOC §5") {
                it.importsAny(LOGIC_FORBIDDEN_PREFIXES, allowed = COMPOSE_STABILITY_ANNOTATIONS)
            }
    }

    @Test
    fun `domain, data api and core common or mvi import nothing from Android`() {
        productionScope().files
            .filter { file -> file.moduleName.startsWith("domain/") || file.moduleName in JVM_ONLY_MODULES }
            .assertFalse(strict = true, additionalMessage = "see $ARCHITECTURE_DOC §5") {
                it.importsAny(ANDROID_PREFIXES, allowed = setOf(VIEW_MODEL_BASE))
            }
    }

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

        /** `domain/impl` is an Android library only so its tests can use `:core:testing`; its code must not care. */
        val JVM_ONLY_MODULES = setOf("data/api", "core/common", "core/mvi")
    }
}
