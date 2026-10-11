plugins {
    alias(libs.plugins.barista.android.library)
}

android {
    namespace = "ru.otus.barista.core.testing"
}

// Consumed by other modules via testImplementation(project(":core:testing")); code lives in src/main on purpose.
dependencies {
    api(project(":core:common"))
    api(project(":feature:menu:api"))
    api(libs.junit)
    api(libs.kotlinx.coroutines.test)
}
