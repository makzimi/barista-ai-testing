package ru.otus.barista.buildlogic

import org.gradle.api.Project
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.getByType

internal val Project.libs: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

internal fun VersionCatalog.lib(alias: String): Provider<MinimalExternalModuleDependency> =
    findLibrary(alias).orElseThrow { IllegalStateException("Library '$alias' is missing in the catalog") }

internal fun VersionCatalog.intVersion(alias: String): Int =
    findVersion(alias).orElseThrow { IllegalStateException("Version '$alias' is missing") }.requiredVersion.toInt()

/**
 * `:feature:menu:impl` -> `feature-menu-impl`.
 * Unique Kotlin module names avoid META-INF clashes between the two `impl` (and `api`) modules.
 */
internal val Project.uniqueModuleName: String
    get() = path.removePrefix(":").replace(':', '-')
