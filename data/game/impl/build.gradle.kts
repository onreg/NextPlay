plugins {
    id("library.convention.plugin")
    id("hilt.convention.plugin")
}

android {
    namespace = "io.github.onreg.data.game.impl"
}

dependencies {
    implementation(projects.data.game.api)
    implementation(projects.core.network)
    implementation(projects.core.db)

    implementation(libs.paging.runtime)
}
