import org.gradle.api.attributes.Bundling
import org.gradle.api.artifacts.VersionCatalogsExtension

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

    inputs.files(fileTree(rootDir) {
        include("**/*.kt", "**/*.kts")
        exclude(
            "**/build/**",
            "**/.gradle/**",
            "**/.idea/**",
            "**/generated/**",
            "**/out/**",
            "**/node_modules/**",
            "**/vendor/**",
            "**/.git/**"
        )
    })
    outputs.dir(ktlintReportDir)

    doFirst {
        val reportDir = ktlintReportDir.get().asFile
        reportDir.mkdirs()

        args = listOf(
            "--relative",
            "--reporter=plain?group_by_file,output=${reportDir.resolve("ktlint.txt")}",
            "--reporter=checkstyle,output=${reportDir.resolve("ktlint-checkstyle.xml")}",
            "--reporter=json,output=${reportDir.resolve("ktlint.json")}",
            "--reporter=sarif,output=${reportDir.resolve("ktlint.sarif.json")}",
        ) + ktlintPatterns
    }
}

tasks.register<JavaExec>("ktlintFormat") {
    group = "formatting"
    description = "Runs ktlint formatting across the entire repository."
    classpath = ktlint
    mainClass.set("com.pinterest.ktlint.Main")
    workingDir = rootDir

    inputs.files(fileTree(rootDir) {
        include("**/*.kt", "**/*.kts")
        exclude(
            "**/build/**",
            "**/.gradle/**",
            "**/.idea/**",
            "**/generated/**",
            "**/out/**",
            "**/node_modules/**",
            "**/vendor/**",
            "**/.git/**"
        )
    })
    outputs.dir(ktlintReportDir)

    doFirst {
        val reportDir = ktlintReportDir.get().asFile
        reportDir.mkdirs()

        args = listOf(
            "-F",
            "--relative",
            "--reporter=plain?group_by_file,output=${reportDir.resolve("ktlint-format.txt")}",
            "--reporter=checkstyle,output=${reportDir.resolve("ktlint-format-checkstyle.xml")}",
            "--reporter=json,output=${reportDir.resolve("ktlint-format.json")}",
            "--reporter=sarif,output=${reportDir.resolve("ktlint-format.sarif.json")}",
        ) + ktlintPatterns
    }
}
