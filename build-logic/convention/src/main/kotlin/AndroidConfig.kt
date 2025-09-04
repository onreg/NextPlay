import org.gradle.api.JavaVersion
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

object AndroidConfig {
    const val TARGET_SDK = 36
    const val COMPILE_SDK = 36
    const val MIN_SDK = 24
    const val KOTLIN_JVM_TOOLCHAIN = 17
    val JAVA_VERSION = JavaVersion.VERSION_17
    val KOTLIN_JVM_TARGET = JvmTarget.JVM_17
}