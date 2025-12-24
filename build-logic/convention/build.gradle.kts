plugins {
    `kotlin-dsl`
}

gradlePlugin {
    plugins {
        register("LibraryConventionPlugin") {
            id = "library.convention.plugin"
            implementationClass = "LibraryConventionPlugin"
        }
        register("HiltConventionPlugin") {
            id = "hilt.convention.plugin"
            implementationClass = "HiltConventionPlugin"
        }
        register("FeatureConventionPlugin") {
            id = "feature.convention.plugin"
            implementationClass = "FeatureConventionPlugin"
        }
        register("ApplicationConventionPlugin") {
            id = "application.convention.plugin"
            implementationClass = "ApplicationConventionPlugin"
        }
        register("UiConventionPlugin") {
            id = "ui.convention.plugin"
            implementationClass = "UiConventionPlugin"
        }
        register("AndroidTestConventionPlugin") {
            id = "android-test.convention.plugin"
            implementationClass = "AndroidTestConventionPlugin"
        }
    }
}

dependencies {
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.android.gradlePlugin)
}
