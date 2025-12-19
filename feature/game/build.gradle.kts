plugins {
    id("feature.convention.plugin")
}

android {
    namespace = "io.github.onreg.feature.game.impl"
}

dependencies {
    implementation(projects.core.ui)
    implementation(projects.data.game.api)
    implementation(libs.paging.compose)
}
