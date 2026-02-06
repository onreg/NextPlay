plugins {
    id("feature.convention.plugin")
}

android {
    namespace = "io.github.onreg.ui.details.presentation"
}

dependencies {
    implementation(projects.core.ui)
    implementation(projects.data.details.api)
    implementation(projects.presentation.platform)
    implementation(projects.presentation.game)
}
