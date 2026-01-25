package core

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.dependencies
import utils.catalog

class HiltConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        apply(plugin = "com.google.dagger.hilt.android")
        apply(plugin = "com.google.devtools.ksp")

        dependencies {
            "implementation"(catalog.findLibrary("hilt.android").get())
            "implementation"(catalog.findLibrary("hilt-navigation-compose").get())
            "ksp"(catalog.findLibrary("hilt.compiler").get())
        }
    }
}
