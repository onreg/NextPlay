import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

class ApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        apply(plugin = "com.android.application")
        apply(plugin = "org.jetbrains.kotlin.android")
        apply(plugin = "org.jetbrains.kotlin.plugin.compose")
        apply(plugin = "com.google.devtools.ksp")
        apply(plugin = "hilt.convention.plugin")
        apply(plugin = "android-test.convention.plugin")

        extensions.configure<ApplicationExtension> {
            compileSdk = AndroidConfig.COMPILE_SDK

            defaultConfig {
                minSdk = AndroidConfig.MIN_SDK
                targetSdk = AndroidConfig.TARGET_SDK
                testInstrumentationRunner = AndroidConfig.TEST_RUNNER
            }

            buildFeatures {
                compose = true
            }

            compileOptions {
                sourceCompatibility = AndroidConfig.JAVA_VERSION
                targetCompatibility = AndroidConfig.JAVA_VERSION
                isCoreLibraryDesugaringEnabled = true
            }

            buildTypes {
                release {
                    isMinifyEnabled = true
                    isShrinkResources = true
                    proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
                }
            }
        }

        extensions.configure<KotlinAndroidProjectExtension> {
            jvmToolchain(AndroidConfig.KOTLIN_JVM_TOOLCHAIN)
            compilerOptions {
                jvmTarget.set(AndroidConfig.KOTLIN_JVM_TARGET)
            }
        }

        dependencies {
            "implementation"(platform(catalog.findLibrary("compose-bom").get()))

            "implementation"(catalog.findLibrary("compose-material3").get())
            "implementation"(catalog.findLibrary("androidx-activity-compose").get())

            "implementation"(catalog.findLibrary("androidx-core-ktx").get())
            "implementation"(catalog.findLibrary("androidx-appcompat").get())

            "implementation"(catalog.findLibrary("hilt-navigation-compose").get())

            "coreLibraryDesugaring"(catalog.findLibrary("desugar-jdk-libs").get())
            "testImplementation"(project(":testing:unit"))
        }
    }
}
