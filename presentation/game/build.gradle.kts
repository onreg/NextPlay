plugins {
    id("feature.convention.plugin")
}

android {
    namespace = "io.github.onreg.ui.game.presentation"
}

dependencies {
    implementation(projects.core.ui)
    implementation(projects.data.game.api)
    implementation(projects.presentation.platform)
}
