import dev.detekt.gradle.Detekt
import dev.detekt.gradle.extensions.DetektExtension
import dev.detekt.gradle.extensions.FailOnSeverity

plugins {
    id("dev.detekt")
}

private val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

private val detektSource = fileTree(rootDir) {
    include("**/*.kt", "**/*.kts")
    exclude(
        "**/build/**",
        "**/.gradle/**",
        "**/.idea/**",
        "**/generated/**",
        "**/out/**",
        "**/node_modules/**",
        "**/vendor/**",
        "**/.git/**",
    )
}

private val detektReportDir = layout.buildDirectory.dir("reports/detekt")
private val detektConfigFiles = listOf(rootProject.file("detekt.yml"))
private val detektBaselineFile = rootProject.file("detekt-baseline.xml")

configure<DetektExtension> {
    toolVersion.set(libs.findVersion("detekt").get().requiredVersion)
    source.setFrom(detektSource)

    parallel.set(true)
    buildUponDefaultConfig.set(true)
    allRules.set(false)
    ignoreFailures.set(false)
    failOnSeverity.set(FailOnSeverity.Error)

    if (detektConfigFiles.isNotEmpty()) config.setFrom(files(detektConfigFiles))
    if (detektBaselineFile.exists()) baseline.set(detektBaselineFile)

    basePath.set(layout.projectDirectory)
    reportsDir.set(detektReportDir)
}

tasks.register<Detekt>("detektCheck") {
    group = "verification"
    description = "Runs detekt across the entire repository."

    setSource(detektSource)
    basePath.set(rootDir.absolutePath)

    parallel.set(true)
    buildUponDefaultConfig.set(true)
    allRules.set(false)
    ignoreFailures.set(false)
    failOnSeverity.set(FailOnSeverity.Error)

    if (detektConfigFiles.isNotEmpty()) config.setFrom(files(detektConfigFiles))
    if (detektBaselineFile.exists()) baseline.set(detektBaselineFile)

    reports {
        checkstyle.required.set(true)
        checkstyle.outputLocation.set(detektReportDir.map { it.file("detekt.xml") })

        html.required.set(true)
        html.outputLocation.set(detektReportDir.map { it.file("detekt.html") })

        sarif.required.set(true)
        sarif.outputLocation.set(detektReportDir.map { it.file("detekt.sarif") })

        markdown.required.set(true)
        markdown.outputLocation.set(detektReportDir.map { it.file("detekt.md") })
    }
}

