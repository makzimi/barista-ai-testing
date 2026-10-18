plugins {
    // Android library only because its tests use `:core:testing` (an Android library: screenshot helpers);
    // a pure JVM module cannot consume an AAR. Production code stays plain Kotlin: DomainPurityKonsistTest.
    alias(libs.plugins.barista.android.library)
}

android {
    namespace = "ru.otus.barista.domain.impl"
}

// Use case implementations and data -> domain mappers.
dependencies {
    api(project(":domain:api"))
    // DomainGraph takes the data-layer repositories, so their interfaces are part of its public signature.
    api(project(":data:api"))
    implementation(project(":core:common"))

    testImplementation(project(":core:testing"))
}
