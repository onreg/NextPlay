import AndroidConfig.COMPILE_SDK
import AndroidConfig.MIN_SDK

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    compileSdk = COMPILE_SDK

    defaultConfig {
        targetSdk = AndroidConfig.TARGET_SDK
        minSdk = MIN_SDK
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    buildFeatures {
        compose = true
    }
}

java {
    sourceCompatibility = AndroidConfig.JAVA_VERSION
    targetCompatibility = AndroidConfig.JAVA_VERSION
}

kotlin {
    jvmToolchain(AndroidConfig.KOTLIN_JVM_TOOLCHAIN)
    compilerOptions {
        jvmTarget = AndroidConfig.KOTLIN_JVM_TARGET
    }
}