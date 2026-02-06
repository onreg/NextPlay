plugins {
    id("library.convention.plugin")
}

android {
    namespace = "io.github.onreg.data.movies.api"
}

dependencies {
    implementation(libs.paging.common)
}
