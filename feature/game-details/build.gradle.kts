plugins {
    id("feature.convention.plugin")
}

android {
    namespace = "io.github.onreg.feature.game.details.impl"
}

dependencies {
    implementation(projects.core.ui)
    implementation(projects.core.utilAndroid)

    implementation(projects.presentation.gameList)
    implementation(projects.presentation.platform)
    implementation(projects.data.details.api)
    implementation(projects.data.screenshots.api)
    implementation(projects.data.movies.api)
    implementation(projects.data.game.api)
}
