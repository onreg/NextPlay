plugins {
    id("feature.convention.plugin")
}

android {
    namespace = "io.github.onreg.data.release.impl"
}

dependencies {
    implementation(projects.data.release.api)

}