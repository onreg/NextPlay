import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class FeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        apply(plugin = "library.convention.plugin")
        apply(plugin = "hilt.convention.plugin")
        apply(plugin = "org.jetbrains.kotlin.plugin.compose")

        extensions.configure<LibraryExtension> {
            defaultConfig {
                testInstrumentationRunner = AndroidConfig.TEST_RUNNER
            }
            buildFeatures {
                compose = true
            }
            androidResources {
                enable = true
            }
        }
        dependencies {
            "implementation"(catalog.findLibrary("androidx-lifecycle-runtime-ktx").get())

            "implementation"(platform(catalog.findLibrary("compose-bom").get()))

            "implementation"(catalog.findLibrary("compose-ui").get())
            "implementation"(catalog.findLibrary("compose-material3").get())
            "implementation"(catalog.findLibrary("compose-tooling-preview").get())

            "implementation"(catalog.findLibrary("hilt-navigation-compose").get())

            "debugImplementation"(catalog.findLibrary("compose-tooling").get())
            "debugImplementation"(catalog.findLibrary("compose-test-manifest").get())
            "androidTestImplementation"(catalog.findLibrary("junit-android").get())
            "androidTestImplementation"(catalog.findLibrary("compose-test-junit4").get())
        }
    }
}