plugins {
    alias(libs.plugins.barista.kotlin.library)
}

dependencies {
    api(libs.okhttp)
    api(libs.retrofit)
    api(libs.retrofit.converter.kotlinx.serialization)
    api(libs.kotlinx.serialization.json)
}
