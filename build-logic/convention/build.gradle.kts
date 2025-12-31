plugins {
    `kotlin-dsl`
}

gradlePlugin {
    plugins {
        register("LibraryConventionPlugin") {
            id = "library.convention.plugin"
            implementationClass = "core.LibraryConventionPlugin"
        }
        register("HiltConventionPlugin") {
            id = "hilt.convention.plugin"
            implementationClass = "core.HiltConventionPlugin"
        }
        register("FeatureConventionPlugin") {
            id = "feature.convention.plugin"
            implementationClass = "presets.FeatureConventionPlugin"
        }
        register("ApplicationConventionPlugin") {
            id = "application.convention.plugin"
            implementationClass = "core.ApplicationConventionPlugin"
        }
        register("UiConventionPlugin") {
            id = "ui.convention.plugin"
            implementationClass = "presets.UiConventionPlugin"
        }
        register("AndroidTestConventionPlugin") {
            id = "android-test.convention.plugin"
            implementationClass = "core.AndroidTestConventionPlugin"
        }
        register("UnitTestConventionPlugin") {
            id = "unit-test.convention.plugin"
            implementationClass = "core.UnitTestConventionPlugin"
        }
        register("NonUiConventionPlugin") {
            id = "non-ui.convention.plugin"
            implementationClass = "presets.NonUiConventionPlugin"
        }
    }
}

dependencies {
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.android.gradlePlugin)
}
