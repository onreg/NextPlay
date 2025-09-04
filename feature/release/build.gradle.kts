plugins {
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    id("library.common.ui")
}

android {
    namespace = "io.github.onreg.data.release.impl"
}

dependencies {
    implementation(projects.data.release.api)
    implementation(libs.kotlinx.coroutines)

    implementation(libs.androidx.lifecycle.runtime.ktx)

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.compose.tooling.preview)

    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)
    ksp(libs.hilt.compiler)

    debugImplementation(libs.compose.tooling)
    debugImplementation(libs.compose.test.manifest)

    testImplementation(libs.junit)
    androidTestImplementation(libs.junit.android)
    androidTestImplementation(libs.compose.test.junit4)
}