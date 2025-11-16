import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

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
            }
        }

        dependencies {
            "implementation"(catalog.findLibrary("kotlinx-coroutines").get())
            "testImplementation"(catalog.findLibrary("junit").get())
            "testImplementation"(catalog.findLibrary("junit-kotlin").get())
            "testImplementation"(catalog.findLibrary("mockito-core").get())
            "testImplementation"(catalog.findLibrary("mockito-kotlin").get())
        }
    }
}