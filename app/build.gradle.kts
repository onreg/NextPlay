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
    implementation(projects.core.ui)
    implementation(projects.core.db)
    implementation(projects.core.network)
    implementation(projects.feature.game)
    implementation(projects.data.game.impl)
}
