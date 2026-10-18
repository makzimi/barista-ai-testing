plugins {
    alias(libs.plugins.barista.android.library)
}

android {
    namespace = "ru.otus.barista.data.impl"
}

// Everything here is `internal` except `DataGraph`: Retrofit, cache, DataStore and repository implementations.
dependencies {
    api(project(":data:api"))
    implementation(project(":core:common"))
    implementation(project(":core:network"))

    // DataGraph takes the app's Retrofit and DataStore, so both types are part of its public signature.
    api(libs.retrofit)
    api(libs.androidx.datastore.preferences)

    testImplementation(project(":core:testing"))
    testImplementation(libs.okhttp.mockwebserver)
}
