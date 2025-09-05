plugins {
    id("application.convention.plugin")
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
}