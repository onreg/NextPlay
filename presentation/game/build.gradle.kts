plugins {
    id("ui.convention.plugin")
    id("hilt.convention.plugin")
    id("android-test.convention.plugin")
}

android {
    namespace = "io.github.onreg.ui.game.presentation"
}

dependencies {
    implementation(projects.core.ui)
    implementation(projects.data.game.api)
    implementation(projects.presentation.platform)

    implementation(libs.paging.compose)
    testImplementation(libs.paging.testing)
}
