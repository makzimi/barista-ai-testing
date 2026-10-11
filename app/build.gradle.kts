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
    implementation(project(":feature:menu:api"))
    implementation(project(":feature:menu:impl"))
    implementation(project(":feature:drink-details:api"))
    implementation(project(":feature:drink-details:impl"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)

    testImplementation(project(":core:testing"))
}
