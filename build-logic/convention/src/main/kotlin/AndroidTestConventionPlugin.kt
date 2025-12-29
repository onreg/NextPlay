import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class AndroidTestConventionPlugin : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        dependencies {
            "testImplementation"(catalog.findLibrary("junit-android").get())
            "testImplementation"(catalog.findLibrary("androidx-test-core").get())
            "testImplementation"(catalog.findLibrary("robolectric").get())
            "androidTestImplementation"(catalog.findLibrary("junit-android").get())
            "androidTestImplementation"(catalog.findLibrary("androidx-test-core").get())
        }

        pluginManager.withPlugin("org.jetbrains.kotlin.plugin.compose") {
            dependencies {
                "testImplementation"(platform(catalog.findLibrary("compose-bom").get()))
                "testImplementation"(catalog.findLibrary("compose-test-junit4").get())
                "androidTestImplementation"(platform(catalog.findLibrary("compose-bom").get()))
                "androidTestImplementation"(catalog.findLibrary("compose-test-junit4").get())
                "debugImplementation"(catalog.findLibrary("compose-test-manifest").get())
            }
        }
    }
}
