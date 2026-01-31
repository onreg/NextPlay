import dev.detekt.gradle.Detekt
import dev.detekt.gradle.extensions.DetektExtension
import dev.detekt.gradle.extensions.FailOnSeverity
import org.gradle.api.artifacts.VersionCatalogsExtension

/*
 * Detekt Gradle convention.
 *
 * Plugin tasks:
 * - Applying the Detekt Gradle plugin registers tasks like `detekt` (hooked into Gradle `check`)
 *   and additional source set / variant tasks such as `detektMain`, `detektTest`, and
 *   `detekt<Variant>`.
 * - This build-logic also provides `detektCheck` to scan the whole repository (including `.kts`
 *   sources outside conventional Gradle source sets).
 *
 * Configuration files:
 * - `detekt.yml` (root project): Repository-wide Detekt rules and settings.
 *
 * Options:
 * - `toolVersion`: taken from `libs.versions.toml` (`versions.detekt`).
 * - `source`: points to the repository-wide `detektSource` file tree (`*.kt` + `*.kts`, excluding
 *   build and generated output).
 * - `parallel = true`: enables parallel rule execution.
 * - `buildUponDefaultConfig = true`: starts from Detekt defaults and applies `detekt.yml` on top.
 * - `allRules = false`: keeps optional/unstable rules disabled unless enabled explicitly.
 * - `ignoreFailures = false` + `failOnSeverity = Error`: makes violations fail the build.
 * - `config = detekt.yml`: explicit repository-wide config.
 * - `basePath`: normalizes paths in reports for stable output.
 * - `reportsDir`: writes reports to `build/reports/detekt/`.
 * - Baseline support is intentionally disabled (no `detekt-baseline.xml`).
 *
 * Reports:
 * - Detekt reports are configured per-task via `reports { ... }`.
 * - Supported built-in report types are `checkstyle`, `html`, `sarif`, and `markdown`.
 * - This project keeps only `html` and a `.txt` report (implemented by enabling `markdown` and
 *   writing it to a `.txt` file) under `build/reports/detekt/`.
 */
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

/** Root Detekt rule configuration file used by all Detekt tasks. */
private val detektConfigFiles = listOf(rootProject.file("detekt.yml"))

configure<DetektExtension> {
    toolVersion.set(libs.findVersion("detekt").get().requiredVersion)
    source.setFrom(detektSource)

    parallel.set(true)
    buildUponDefaultConfig.set(true)
    allRules.set(false)
    ignoreFailures.set(false)
    failOnSeverity.set(FailOnSeverity.Error)

    config.setFrom(files(detektConfigFiles))

    basePath.set(layout.projectDirectory)
    reportsDir.set(detektReportDir)
}

tasks.withType<Detekt>().configureEach {
    reports {
        html.required.set(true)
        markdown.required.set(true)

        checkstyle.required.set(false)
        sarif.required.set(false)

        html.outputLocation.set(
            detektReportDir.map { it.file("detekt-$name.html") },
        )
        markdown.outputLocation.set(
            detektReportDir.map { it.file("detekt-$name.txt") },
        )
    }
}

tasks.register<Detekt>("detektCheck") {
    group = "verification"
    description = "Runs detekt across the entire repository."

    source = detektSource
    basePath.set(rootDir.absolutePath)

    parallel.set(true)
    buildUponDefaultConfig.set(true)
    allRules.set(false)
    ignoreFailures.set(false)
    failOnSeverity.set(FailOnSeverity.Error)

    config.setFrom(files(detektConfigFiles))

    reports {
        html.outputLocation.set(detektReportDir.map { it.file("detekt.html") })
        markdown.outputLocation.set(detektReportDir.map { it.file("detekt.txt") })
    }
}
