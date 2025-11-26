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
    implementation(libs.room.ktx)
    implementation(libs.room.paging)

    testImplementation(libs.coroutines.test)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.paging.common)
    testImplementation(libs.room.testing)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.robolectric)
}
