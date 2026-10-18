plugins {
    alias(libs.plugins.barista.android.application)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "ru.otus.barista.app"

    defaultConfig {
        applicationId = "ru.otus.barista"
        versionCode = 1
        versionName = "1.0"
    }

    // The sociable test serves the real fake-backend JSON: make `src/main/assets` readable as test resources.
    sourceSets.getByName("test").resources.srcDir("src/main/assets")

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:mvi"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:network"))
    implementation(project(":data:api"))
    implementation(project(":data:impl"))
    implementation(project(":domain:api"))
    implementation(project(":domain:impl"))
    implementation(project(":feature:menu"))
    implementation(project(":feature:drink-details"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)

    testImplementation(project(":core:testing"))
    testImplementation(libs.kotlinx.collections.immutable)
}
