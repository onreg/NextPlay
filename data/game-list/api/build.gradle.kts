plugins {
    id("library.convention.plugin")
}

android {
    namespace = "io.github.onreg.data.game.list.api"
}

dependencies {
    implementation(libs.paging.common)
}
