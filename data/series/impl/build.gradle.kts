plugins {
    id("non-ui.convention.plugin")
}

android {
    namespace = "io.github.onreg.data.series.impl"
}

dependencies {
    implementation(projects.data.series.api)
    implementation(projects.data.game.api)
    implementation(projects.core.network)
    implementation(projects.core.db)

    implementation(libs.paging.runtime)
}
