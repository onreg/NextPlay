plugins {
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    id("application.common")
}

android {
    namespace = "io.github.onreg.nextplay"

    defaultConfig {
        applicationId = "io.github.onreg.nextplay"
        versionCode = 1
        versionName = "1.0"
    }
}

dependencies {
    implementation(projects.feature.release)
    implementation(projects.data.release.impl)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity.compose)
    implementation(libs.compose.material3)

    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)
    ksp(libs.hilt.compiler)

    androidTestImplementation(libs.junit.android)
}