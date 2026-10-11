import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import ru.otus.barista.buildlogic.configureAndroidCommon
import ru.otus.barista.buildlogic.configureCompose
import ru.otus.barista.buildlogic.intVersion
import ru.otus.barista.buildlogic.libs

class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.android.application")
        extensions.configure<ApplicationExtension> {
            configureAndroidCommon(this)
            configureCompose(this)
            defaultConfig.targetSdk = libs.intVersion("targetSdk")
        }
    }
}
