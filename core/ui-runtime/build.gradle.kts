plugins {
    id("ui.convention.plugin")
}

android {
    namespace = "io.github.onreg.core.ui.runtime"
}

dependencies {
    implementation(libs.androidx.lifecycle.runtime.ktx)
}
