import io.gitlab.arturbosch.detekt.Detekt

plugins {
    id("io.gitlab.arturbosch.detekt")
}

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

private val detektConfigFile = rootProject.file("config/detekt/detekt.yml")
private val detektBaselineFile = rootProject.file("config/detekt/baseline.xml")

tasks.register<Detekt>("detektCheck") {
    group = "verification"
    description = "Runs detekt across the entire repository."

    source = detektSource
    basePath = rootDir.absolutePath

    if (detektConfigFile.exists()) config.setFrom(files(detektConfigFile))
    if (detektBaselineFile.exists()) baseline.set(detektBaselineFile)

    reports {
        sarif.required.set(false)
        xml.required.set(false)
        html.required.set(false)
        md.required.set(false)

        txt.required.set(true)
        txt.outputLocation.set(detektReportDir.map { it.file("detekt.txt") })
    }
}
