plugins {
    id("feature.convention.plugin")
}

android {
    namespace = "io.github.onreg.ui.game.list.presentation"
}

dependencies {
    implementation(projects.core.ui)
    implementation(projects.data.gameList.api)
    implementation(projects.presentation.platform)
}
