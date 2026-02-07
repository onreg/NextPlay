plugins {
    id("feature.convention.plugin")
}

android {
    namespace = "io.github.onreg.feature.game.list.impl"
}

dependencies {
    implementation(projects.core.ui)
    implementation(projects.data.gameList.api)
    implementation(projects.presentation.gameList)
    implementation(projects.core.utilAndroid)
    testImplementation(projects.presentation.platform)
}
