plugins {
    id("library.convention.plugin")
    id("hilt.convention.plugin")
    id("android-test.convention.plugin")
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
}
