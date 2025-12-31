plugins {
    id("non-ui.convention.plugin")
}

android {
    namespace = "io.github.onreg.core.util.android"
}

dependencies {
    implementation(libs.androidx.lifecylce.viewmodel.ktx)
}
