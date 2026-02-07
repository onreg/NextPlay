plugins {
    id("library.convention.plugin")
}

android {
    namespace = "io.github.onreg.data.series.api"
}

dependencies {
    api(projects.data.gameList.api)
    implementation(libs.paging.common)
}
