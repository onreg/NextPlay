package presets

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply

class FeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        apply(plugin = "ui.convention.plugin")
        apply(plugin = "hilt.convention.plugin")
        apply(plugin = "android-test.convention.plugin")
        apply(plugin = "unit-test.convention.plugin")
    }
}
