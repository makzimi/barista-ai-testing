plugins {
    alias(libs.plugins.barista.android.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "ru.otus.barista.feature.menu"
}

dependencies {
    api(project(":feature:menu:api"))
    implementation(project(":core:common"))
    implementation(project(":core:mvi"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:network"))

    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.kotlinx.collections.immutable)
    implementation(libs.kotlinx.serialization.json)

    testImplementation(project(":core:testing"))
    testImplementation(libs.okhttp.mockwebserver)
}
