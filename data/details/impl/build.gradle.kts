plugins {
    id("non-ui.convention.plugin")
}

android {
    namespace = "io.github.onreg.data.details.impl"
}

dependencies {
    implementation(projects.data.details.api)
    implementation(projects.core.network)
    implementation(projects.core.db)
}
