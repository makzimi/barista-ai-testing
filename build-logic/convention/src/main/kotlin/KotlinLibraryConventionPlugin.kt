import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.kotlin.dsl.configure
import ru.otus.barista.buildlogic.addCommonTestDependencies
import ru.otus.barista.buildlogic.configureKotlinJvm
import ru.otus.barista.buildlogic.configureTestFilters

/** Pure Kotlin/JVM module: no Android on the classpath, which proves domain purity at compile time. */
class KotlinLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("org.jetbrains.kotlin.jvm")
        extensions.configure<JavaPluginExtension> {
            sourceCompatibility = JavaVersion.VERSION_17
            targetCompatibility = JavaVersion.VERSION_17
        }
        configureKotlinJvm()
        addCommonTestDependencies()
        configureTestFilters()
    }
}
