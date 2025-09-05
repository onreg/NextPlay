plugins {
    id("library.convention.plugin")
    id("hilt.convention.plugin")
}

android {
    namespace = "io.github.onreg.data.release.impl"
}

dependencies {
    implementation(projects.data.release.api)
}