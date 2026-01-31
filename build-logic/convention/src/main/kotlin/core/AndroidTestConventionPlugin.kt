package core

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import utils.catalog

class AndroidTestConventionPlugin : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        dependencies {
            "androidTestImplementation"(catalog.findLibrary("junit-android").get())
            "androidTestImplementation"(catalog.findLibrary("androidx-test-core").get())
        }

        pluginManager.withPlugin("org.jetbrains.kotlin.plugin.compose") {
            dependencies {
                "androidTestImplementation"(platform(catalog.findLibrary("compose-bom").get()))
                "androidTestImplementation"(catalog.findLibrary("compose-test-junit4").get())
                "debugImplementation"(catalog.findLibrary("compose-test-manifest").get())
            }
        }
    }
}
