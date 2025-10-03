plugins {
    id("feature.convention.plugin")
}

android {
    namespace = "io.github.onreg.data.release.impl"
}

dependencies {
    implementation(projects.core.ui)
    implementation(projects.data.release.api)
}