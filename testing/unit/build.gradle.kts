plugins {
    id("library.convention.plugin")
}

android {
    namespace = "io.github.onreg.testing.unit"
}

dependencies {
    api(libs.coroutines.test)
    api(libs.junit)
    api(libs.junit.kotlin)
    api(libs.paging.testing)
    api(libs.mockito.core)
    api(libs.mockito.kotlin)

    implementation(libs.paging.common)
}
