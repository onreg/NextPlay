plugins {
    id("non-ui.convention.plugin")
}

android {
    namespace = "io.github.onreg.core.network"
    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        val rawgApiKey = providers
            .environmentVariable("RAWG_API_KEY")
            .orElse(providers.gradleProperty("RAWG_API_KEY"))
            .orElse("")
            .get()

        check(rawgApiKey.isNotBlank()) {
            "RAWG_API_KEY is missing. Set it via environment variable or Gradle property."
        }

        buildConfigField("String", "RAWG_API_KEY", "\"$rawgApiKey\"")
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-XXLanguage:+PropertyParamAnnotationDefaultTargetMode")
    }
}

dependencies {
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.moshi)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.moshi)
    implementation(libs.moshi.kotlin)
}
