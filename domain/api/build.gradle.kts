plugins {
    alias(libs.plugins.barista.kotlin.library)
}

// Domain models and use case interfaces. Pure JVM; the only thing features see of the business logic.
dependencies {
    api(project(":core:common"))
}
