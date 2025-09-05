import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.dependencies

class HiltConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        apply(plugin = "com.google.dagger.hilt.android")
        apply(plugin = "com.google.devtools.ksp")

        dependencies {
            "implementation"(catalog.findLibrary("hilt.android").get())
            "ksp"(catalog.findLibrary("hilt.compiler").get())
        }
    }
}