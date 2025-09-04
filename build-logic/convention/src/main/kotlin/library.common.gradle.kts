import AndroidConfig.COMPILE_SDK
import AndroidConfig.MIN_SDK

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    compileSdk = COMPILE_SDK

    defaultConfig {
        minSdk = MIN_SDK
    }
    buildFeatures {
        buildConfig = false
    }
}

java {
    sourceCompatibility = AndroidConfig.JAVA_VERSION
    targetCompatibility = AndroidConfig.JAVA_VERSION
}

kotlin {
    explicitApi()
    jvmToolchain(AndroidConfig.KOTLIN_JVM_TOOLCHAIN)
    compilerOptions {
        jvmTarget = AndroidConfig.KOTLIN_JVM_TARGET
    }
}