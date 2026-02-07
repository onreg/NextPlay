plugins {
    id("non-ui.convention.plugin")
}

android {
    namespace = "io.github.onreg.data.game.list.impl"
}

dependencies {
    implementation(projects.data.gameList.api)
    implementation(projects.core.network)
    implementation(projects.core.db)

    implementation(libs.paging.runtime)
}
