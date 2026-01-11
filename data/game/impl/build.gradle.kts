plugins {
    id("non-ui.convention.plugin")
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
