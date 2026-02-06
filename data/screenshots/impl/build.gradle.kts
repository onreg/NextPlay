plugins {
    id("non-ui.convention.plugin")
}

android {
    namespace = "io.github.onreg.data.screenshots.impl"
}

dependencies {
    implementation(projects.data.screenshots.api)
    implementation(projects.core.network)
    implementation(projects.core.db)

    implementation(libs.paging.runtime)
}
