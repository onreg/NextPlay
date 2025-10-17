import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class UiConventionPlugin : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        apply(plugin = "library.convention.plugin")
        apply(plugin = "org.jetbrains.kotlin.plugin.compose")

        extensions.configure<LibraryExtension> {
            buildFeatures {
                compose = true
            }
            androidResources {
                enable = true
            }
        }

        dependencies {
            "implementation"(platform(catalog.findLibrary("compose-bom").get()))

            "implementation"(catalog.findLibrary("compose-ui").get())
            "implementation"(catalog.findLibrary("compose-material3").get())
            "implementation"(catalog.findLibrary("compose-tooling-preview").get())
            "implementation"(catalog.findLibrary("coil-compose").get())
            "implementation"(catalog.findLibrary("coil-network").get())

            "debugImplementation"(catalog.findLibrary("compose-tooling").get())
        }
    }
}
