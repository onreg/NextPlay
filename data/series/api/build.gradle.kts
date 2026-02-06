plugins {
    id("library.convention.plugin")
}

android {
    namespace = "io.github.onreg.data.series.api"
}

dependencies {
    implementation(libs.paging.common)
    implementation(projects.data.game.api)
}
