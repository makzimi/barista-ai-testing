import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import ru.otus.barista.buildlogic.configureCompose

class AndroidLibraryComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("barista.android.library")
        extensions.configure<LibraryExtension> {
            configureCompose(this)
        }
    }
}
