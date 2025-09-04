plugins {
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    id("library.common")
}

android {
    namespace = "io.github.onreg.data.release.impl"
    androidResources {
        enable = false
    }
}

dependencies {
    implementation(projects.data.release.api)
    implementation(libs.kotlinx.coroutines)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    testImplementation(libs.junit)
}