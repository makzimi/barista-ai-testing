plugins {
    alias(libs.plugins.barista.kotlin.library)
    alias(libs.plugins.kotlin.serialization)
}

// Data contracts: @Serializable DTOs and repository interfaces. Pure JVM, knows nothing about domain or features.
dependencies {
    api(project(":core:common"))
    api(libs.kotlinx.serialization.json)
}
