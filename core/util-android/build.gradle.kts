plugins {
    id("library.convention.plugin")
    id("hilt.convention.plugin")
}

android {
    namespace = "io.github.onreg.core.util.android"
}

dependencies {
    implementation(libs.androidx.lifecylce.viewmodel.ktx)
}
