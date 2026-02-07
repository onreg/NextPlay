plugins {
    id("library.convention.plugin")
}

android {
    namespace = "io.github.onreg.data.details.api"
}

dependencies {
    api(projects.data.gameList.api)
}
