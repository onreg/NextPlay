plugins {
    id("library.convention.plugin")
}

android {
    namespace = "io.github.onreg.data.game.api"
}

dependencies {
    implementation(libs.paging.common)
}
