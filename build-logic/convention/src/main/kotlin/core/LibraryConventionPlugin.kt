package core

import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension
import utils.catalog

class LibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        apply(plugin = "com.android.library")
        apply(plugin = "org.jetbrains.kotlin.android")

        extensions.configure<LibraryExtension> {
            compileSdk = AndroidConfig.COMPILE_SDK

            defaultConfig {
                minSdk = AndroidConfig.MIN_SDK
            }
            buildFeatures {
                buildConfig = false
            }
            compileOptions {
                sourceCompatibility = AndroidConfig.JAVA_VERSION
                targetCompatibility = AndroidConfig.JAVA_VERSION
                isCoreLibraryDesugaringEnabled = true
            }
            androidResources {
                enable = false
            }
        }

        extensions.configure<KotlinAndroidProjectExtension> {
            explicitApi()
            jvmToolchain(AndroidConfig.KOTLIN_JVM_TOOLCHAIN)
            compilerOptions {
                jvmTarget.set(AndroidConfig.KOTLIN_JVM_TARGET)
                freeCompilerArgs.add("-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi")
                freeCompilerArgs.add("-opt-in=androidx.paging.ExperimentalPagingApi")
            }
        }

        dependencies {
            "coreLibraryDesugaring"(catalog.findLibrary("desugar-jdk-libs").get())
            "implementation"(catalog.findLibrary("kotlinx-coroutines").get())
        }
    }
}
