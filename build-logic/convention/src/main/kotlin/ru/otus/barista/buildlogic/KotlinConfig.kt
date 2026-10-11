package ru.otus.barista.buildlogic

import org.gradle.api.Project
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

internal fun Project.configureKotlinJvm() {
    tasks.withType<KotlinJvmCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
            moduleName.set(uniqueModuleName)
        }
    }
}

/** Test libraries every module gets, so test authors never touch build files for the basics. */
internal fun Project.addCommonTestDependencies() {
    dependencies.apply {
        add("testImplementation", libs.lib("junit"))
        add("testImplementation", libs.lib("kotlin-test"))
        add("testImplementation", libs.lib("kotlin-test-junit"))
        add("testImplementation", libs.lib("kotlinx-coroutines-test"))
        add("testImplementation", libs.lib("turbine"))
        add("testImplementation", libs.lib("mockk"))
    }
}
