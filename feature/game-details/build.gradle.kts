plugins {
    id("feature.convention.plugin")
}

android {
    namespace = "io.github.onreg.feature.game.details.impl"
}

dependencies {
    implementation(projects.core.ui)
    implementation(projects.data.details.api)
    implementation(projects.data.game.api)
    implementation(projects.data.screenshots.api)
    implementation(projects.data.movies.api)
    implementation(projects.data.series.api)
    implementation(projects.presentation.details)
    implementation(projects.presentation.game)
    implementation(projects.presentation.platform)
    implementation(projects.core.utilAndroid)
}
