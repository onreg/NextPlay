import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.attributes.Bundling

/**
 * Ktlint Gradle convention (CLI-based).
 *
 * Configuration files:
 * - `.editorconfig` (root project): Formatting rules and ktlint options used by the CLI.
 *
 * Options:
 * - `ktlint-cli` dependency version: taken from `libs.versions.toml` (`versions.ktlint`).
 * - `ktlintPatterns`: includes `*.kt` and `*.kts` while excluding build output and common generated/IDE
 *   directories.
 * - `workingDir = rootDir`: makes `--relative` paths stable in reports.
 * - `ktlintCheck`:
 *   - uses `--relative` for stable paths.
 *   - writes reports as `ktlint.txt` (plain) and `ktlint.html` (html).
 * - `ktlintFormat`:
 *   - uses `-F` to apply formatting changes.
 *   - writes reports as `ktlint-format.txt` (plain) and `ktlint-format.html` (html).
 *
 * Reports:
 * - Generates only HTML and plain text reports under `build/reports/ktlint/`.
 */
private val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")
private val ktlint by configurations.creating

dependencies {
    ktlint(libs.findLibrary("ktlint-cli").get()) {
        attributes {
            attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling.EXTERNAL))
        }
    }
}

private val ktlintReportDir = layout.buildDirectory.dir("reports/ktlint")
private val ktlintPatterns = listOf(
    "**/*.kt",
    "**/*.kts",
    "!**/build/**",
    "!**/.gradle/**",
    "!**/.idea/**",
    "!**/generated/**",
    "!**/out/**",
    "!**/node_modules/**",
    "!**/vendor/**",
    "!**/.git/**",
)

tasks.register<JavaExec>("ktlintCheck") {
    group = "verification"
    description = "Runs ktlint across the entire repository."
    classpath = ktlint
    mainClass.set("com.pinterest.ktlint.Main")
    workingDir = rootDir

    inputs.files(
        fileTree(rootDir) {
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
        },
    )
    outputs.dir(ktlintReportDir)

    doFirst {
        val reportDir = ktlintReportDir.get().asFile
        reportDir.mkdirs()

        args = listOf(
            "--relative",
            "--reporter=plain?group_by_file,output=${reportDir.resolve("ktlint.txt")}",
            "--reporter=html,output=${reportDir.resolve("ktlint.html")}",
        ) + ktlintPatterns
    }
}

tasks.register<JavaExec>("ktlintFormat") {
    group = "formatting"
    description = "Runs ktlint formatting across the entire repository."
    classpath = ktlint
    mainClass.set("com.pinterest.ktlint.Main")
    workingDir = rootDir

    inputs.files(
        fileTree(rootDir) {
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
        },
    )
    outputs.dir(ktlintReportDir)

    doFirst {
        val reportDir = ktlintReportDir.get().asFile
        reportDir.mkdirs()

        args = listOf(
            "-F",
            "--relative",
            "--reporter=plain?group_by_file,output=${reportDir.resolve("ktlint-format.txt")}",
            "--reporter=html,output=${reportDir.resolve("ktlint-format.html")}",
        ) + ktlintPatterns
    }
}
