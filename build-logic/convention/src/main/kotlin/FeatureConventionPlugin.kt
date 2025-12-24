import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class FeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        apply(plugin = "ui.convention.plugin")
        apply(plugin = "hilt.convention.plugin")
        apply(plugin = "android-test.convention.plugin")

        extensions.configure<LibraryExtension> {
            defaultConfig {
                testInstrumentationRunner = AndroidConfig.TEST_RUNNER
            }
        }
        dependencies {
            "implementation"(catalog.findLibrary("androidx-lifecycle-runtime-ktx").get())
            "implementation"(catalog.findLibrary("hilt-navigation-compose").get())
        }
    }
}
