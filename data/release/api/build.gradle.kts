plugins {
    id("library.common")
}

android {
    namespace = "io.github.onreg.data.release.api"
}

dependencies {
    implementation(libs.kotlinx.coroutines)
}