plugins {
    id("library.convention.plugin")
    id("hilt.convention.plugin")
}

android {
    namespace = "io.github.onreg.ui.game.presentation"
}

dependencies {
    implementation(projects.core.ui)
    implementation(projects.data.game.api)
    implementation(projects.presentation.platform)
    implementation(libs.paging.common)

    testImplementation(libs.coroutines.test)
    testImplementation(libs.paging.runtime)
}
