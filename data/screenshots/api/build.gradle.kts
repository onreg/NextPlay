plugins {
    id("library.convention.plugin")
}

android {
    namespace = "io.github.onreg.data.screenshots.api"
}

dependencies {
    implementation(libs.paging.common)
}
