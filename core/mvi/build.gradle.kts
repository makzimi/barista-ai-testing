plugins {
    alias(libs.plugins.barista.kotlin.library)
}

dependencies {
    // The KMP lifecycle-viewmodel artifact has a JVM variant: ViewModel + viewModelScope without Android.
    api(libs.androidx.lifecycle.viewmodel)
    api(libs.kotlinx.coroutines.core)
}
