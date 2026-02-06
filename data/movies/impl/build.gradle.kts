plugins {
    id("non-ui.convention.plugin")
}

android {
    namespace = "io.github.onreg.data.movies.impl"
}

dependencies {
    implementation(projects.data.movies.api)
    implementation(projects.core.network)
    implementation(projects.core.db)

    implementation(libs.paging.runtime)
}
