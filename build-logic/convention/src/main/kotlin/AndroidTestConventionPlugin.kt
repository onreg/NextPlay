import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class AndroidTestConventionPlugin : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        dependencies {
            "androidTestImplementation"(catalog.findLibrary("junit-android").get())
            "testImplementation"(catalog.findLibrary("junit-android").get())
            "testImplementation"(catalog.findLibrary("androidx-test-core").get())
            "testImplementation"(catalog.findLibrary("robolectric").get())
        }

        pluginManager.withPlugin("org.jetbrains.kotlin.plugin.compose") {
            dependencies {
                "debugImplementation"(catalog.findLibrary("compose-test-manifest").get())
                "androidTestImplementation"(catalog.findLibrary("compose-test-junit4").get())
            }
        }
    }
}
