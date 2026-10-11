plugins {
    alias(libs.plugins.barista.android.compose)
}

android {
    namespace = "ru.otus.barista.feature.drinkdetails"
}

dependencies {
    api(project(":feature:drink-details:api"))
    implementation(project(":feature:menu:api"))
    implementation(project(":core:common"))
    implementation(project(":core:mvi"))
    implementation(project(":core:designsystem"))

    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.collections.immutable)

    testImplementation(project(":core:testing"))
}
