plugins {
    id("library.convention.plugin")
    id("hilt.convention.plugin")
}

android {
    namespace = "io.github.onreg.core.db"
}

ksp {
    arg("room.schemaLocation", "${project.projectDir}/schemas")
    arg("room.incremental", "true")
    arg("room.expandProjection", "true")
}

dependencies {
    implementation(libs.room.ktx)
    implementation(libs.room.paging)

    ksp(libs.room.compiler)

    testImplementation(libs.coroutines.test)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.junit.android)
    testImplementation(libs.robolectric)
}
