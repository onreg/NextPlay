plugins {
    id("ui.convention.plugin")
}

android {
    namespace = "io.github.onreg.core.ui"
}
dependencies {
    implementation(libs.paging.compose)
}
