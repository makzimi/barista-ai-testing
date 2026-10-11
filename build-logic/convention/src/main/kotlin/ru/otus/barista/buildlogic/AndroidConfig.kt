package ru.otus.barista.buildlogic

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project

internal fun Project.configureAndroidCommon(extension: CommonExtension) {
    extension.apply {
        compileSdk = libs.intVersion("compileSdk")
        defaultConfig.minSdk = libs.intVersion("minSdk")
        compileOptions.sourceCompatibility = JavaVersion.VERSION_17
        compileOptions.targetCompatibility = JavaVersion.VERSION_17
        testOptions.unitTests.isIncludeAndroidResources = true
        testOptions.unitTests.isReturnDefaultValues = true
    }
    configureKotlinJvm()
    addCommonTestDependencies()
    configureTestFilters()
}

internal fun Project.configureCompose(extension: CommonExtension) {
    pluginManager.apply("org.jetbrains.kotlin.plugin.compose")
    extension.buildFeatures.compose = true
    dependencies.apply {
        val bom = dependencies.platform(libs.lib("androidx-compose-bom"))
        add("implementation", bom)
        add("testImplementation", bom)
        add("implementation", libs.lib("androidx-compose-ui"))
        add("implementation", libs.lib("androidx-compose-foundation"))
        add("implementation", libs.lib("androidx-compose-material3"))
        add("implementation", libs.lib("androidx-compose-ui-tooling-preview"))
        add("debugImplementation", libs.lib("androidx-compose-ui-tooling"))
        add("testImplementation", libs.lib("robolectric"))
        add("testImplementation", libs.lib("androidx-test-core"))
        add("testImplementation", libs.lib("androidx-test-ext-junit"))
        add("testImplementation", libs.lib("androidx-compose-ui-test-junit4"))
        add("debugImplementation", libs.lib("androidx-compose-ui-test-manifest"))
    }
}
