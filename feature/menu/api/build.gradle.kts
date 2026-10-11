plugins {
    alias(libs.plugins.barista.kotlin.library)
}

dependencies {
    api(project(":core:common"))
    api(libs.kotlinx.coroutines.core)
}
