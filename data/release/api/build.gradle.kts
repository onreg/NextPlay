plugins {
    id("library.common")
}

android {
    namespace = "io.github.onreg.data.release.api"
    androidResources {
        enable = false
    }
}

dependencies {
    implementation(libs.kotlinx.coroutines)
}