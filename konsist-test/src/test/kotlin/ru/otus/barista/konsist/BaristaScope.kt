package ru.otus.barista.konsist

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.container.KoScope

/**
 * Every Kotlin file of the app, minus the Gradle convention plugins and build outputs.
 * Parsed once per test JVM: every rule class scopes through these helpers.
 */
private val projectScope: KoScope by lazy {
    Konsist.scopeFromProject().slice { file ->
        !file.path.contains("/build-logic/") && !file.path.contains("/build/")
    }
}

private val mainScope: KoScope by lazy { projectScope.slice { it.sourceSetName == "main" } }

private val unitTestScope: KoScope by lazy { projectScope.slice { it.sourceSetName == "test" } }

internal fun baristaScope(): KoScope = projectScope

/** Production code: every module's `src/main`. */
internal fun productionScope(): KoScope = mainScope

/** JVM tests: every module's `src/test` (including this module's own Konsist tests). */
internal fun testScope(): KoScope = unitTestScope

/** `ru.otus.barista.feature.menu.domain` -> true for "domain". Packages are compared by whole segments. */
internal fun String?.hasPackageSegment(segment: String): Boolean = this?.split('.')?.contains(segment) == true

internal const val ARCHITECTURE_DOC = "docs/rules/architecture.md"
internal const val COMPOSE_DOC = "docs/rules/compose.md"
internal const val TESTING_DOC = "docs/rules/testing.md"
