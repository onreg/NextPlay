plugins {
    id("non-ui.convention.plugin")
}

android {
    namespace = "io.github.onreg.ui.platform"
    androidResources {
        enable = true
    }
}

dependencies {
    implementation(projects.core.ui)
    implementation(projects.core.utilAndroid)
    implementation(projects.data.game.api)
}
