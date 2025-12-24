plugins {
    id("library.convention.plugin")
}

android {
    namespace = "io.github.onreg.testing.unit"
}

dependencies {
    implementation(libs.coroutines.test)
    implementation(libs.junit)
    implementation(libs.junit.kotlin)

    implementation(libs.paging.common)
    implementation(libs.paging.testing)
}
