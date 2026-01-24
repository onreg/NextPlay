import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import com.android.build.api.dsl.Lint
import org.gradle.api.GradleException
import org.gradle.api.Project

private val lintAll = tasks.register("lintAll") {
    group = "verification"
    description = "Runs Android Lint across all Android modules."
}

val lintReportMerge = tasks.register("lintCheck") {
    group = "verification"
    description = "Merges Android Lint reports across modules and fails on issues."
    dependsOn(lintAll)
    val outputDir = layout.buildDirectory.dir("reports/lint")
    outputs.dir(outputDir)
    doLast {
        val outDirFile = outputDir.get().asFile
        outDirFile.mkdirs()
        val mergedText = outDirFile.resolve("lint.txt")
        val mergedHtml = outDirFile.resolve("lint.html")

        val textBuilder = StringBuilder()
        val htmlBodyBuilder = StringBuilder()
        var issueCount = 0

        subprojects.forEach { project ->
            val textReport = project.layout.buildDirectory
                .file("reports/lint/lint-results.txt")
                .get()
                .asFile
            val htmlReport = project.layout.buildDirectory
                .file("reports/lint/lint-results.html")
                .get()
                .asFile

            val text = if (textReport.exists()) {
                textReport.readText(Charsets.UTF_8)
            } else {
                null
            }
            val html = if (htmlReport.exists()) {
                htmlReport.readText(Charsets.UTF_8)
            } else {
                null
            }

            if (text != null) {
                textBuilder.append("=== ").append(project.path).append(" ===\n")
                textBuilder.append(text.trimEnd())
                textBuilder.append("\n\n")
            }

            issueCount += parseLintIssuesCount(text, html)

            if (html != null) {
                val bodyMatch = Regex("<body[^>]*>([\\s\\S]*?)</body>", RegexOption.IGNORE_CASE)
                    .find(html)
                val bodyContent = bodyMatch?.groupValues?.get(1)?.trim() ?: run {
                    val escaped = html
                        .replace("&", "&amp;")
                        .replace("<", "&lt;")
                        .replace(">", "&gt;")
                    "<pre>$escaped</pre>"
                }
                htmlBodyBuilder
                    .append("<section>")
                    .append("<h2>")
                    .append(project.path)
                    .append("</h2>")
                    .append(bodyContent)
                    .append("</section><hr/>")
            }
        }

        mergedText.writeText(textBuilder.toString().trimEnd() + "\n")
        mergedHtml.writeText(
            """
            <!doctype html>
            <html>
              <head>
                <meta charset="utf-8"/>
                <title>Lint Results - Merged</title>
                <style>
                  body { font-family: sans-serif; margin: 24px; }
                  h1 { margin-bottom: 16px; }
                  h2 { margin-top: 24px; }
                  section { margin-bottom: 32px; }
                </style>
              </head>
              <body>
                <h1>Lint Results - Merged</h1>
                ${htmlBodyBuilder.toString().trim()}
              </body>
            </html>
            """.trimIndent() + "\n",
        )

        if (issueCount > 0) {
            throw GradleException(
                "Android Lint found $issueCount issues. See ${mergedText.absolutePath} and " +
                    "${mergedHtml.absolutePath}.",
            )
        }
    }
}

subprojects {
    plugins.withId("com.android.application") {
        extensions.configure<ApplicationExtension> {
            lint {
                this@subprojects.configureAndroidLint(this)
            }
        }
        lintAll.configure {
            dependsOn(tasks.named("lint"))
        }
    }
    plugins.withId("com.android.library") {
        extensions.configure<LibraryExtension> {
            lint {
                this@subprojects.configureAndroidLint(this)
            }
        }
        lintAll.configure {
            dependsOn(tasks.named("lint"))
        }
    }
}

private fun Project.configureAndroidLint(lint: Lint) {
    lint.warningsAsErrors = true
    lint.abortOnError = false
    lint.textReport = true
    lint.textOutput = layout.buildDirectory
        .file("reports/lint/lint-results.txt")
        .get()
        .asFile
    lint.htmlReport = true
    lint.htmlOutput = layout.buildDirectory
        .file("reports/lint/lint-results.html")
        .get()
        .asFile
}

private fun parseLintIssuesCount(
    text: String?,
    html: String?,
): Int {
    if (text != null) {
        val summaryMatch = Regex("(\\d+)\\s+errors?,\\s+(\\d+)\\s+warnings?", RegexOption.IGNORE_CASE)
            .findAll(text)
            .lastOrNull()
        if (summaryMatch != null) {
            val errors = summaryMatch.groupValues[1].toInt()
            val warnings = summaryMatch.groupValues[2].toInt()
            return errors + warnings
        }

        val foundMatch = Regex("Issues? found:\\s*(\\d+)", RegexOption.IGNORE_CASE)
            .findAll(text)
            .lastOrNull()
        if (foundMatch != null) {
            return foundMatch.groupValues[1].toInt()
        }

        val errors = Regex("\\b(\\d+)\\s+errors?\\b", RegexOption.IGNORE_CASE)
            .findAll(text)
            .lastOrNull()
            ?.groupValues
            ?.get(1)
            ?.toInt()
            ?: 0
        val warnings = Regex("\\b(\\d+)\\s+warnings?\\b", RegexOption.IGNORE_CASE)
            .findAll(text)
            .lastOrNull()
            ?.groupValues
            ?.get(1)
            ?.toInt()
            ?: 0
        if (errors + warnings > 0) {
            return errors + warnings
        }
    }

    if (html != null) {
        val foundMatch = Regex("Issues? found:\\s*(\\d+)", RegexOption.IGNORE_CASE)
            .findAll(html)
            .lastOrNull()
        if (foundMatch != null) {
            return foundMatch.groupValues[1].toInt()
        }

        val summaryMatch = Regex("(\\d+)\\s+errors?,\\s+(\\d+)\\s+warnings?", RegexOption.IGNORE_CASE)
            .findAll(html)
            .lastOrNull()
        if (summaryMatch != null) {
            val errors = summaryMatch.groupValues[1].toInt()
            val warnings = summaryMatch.groupValues[2].toInt()
            return errors + warnings
        }

        val errors = Regex("\\b(\\d+)\\s+errors?\\b", RegexOption.IGNORE_CASE)
            .findAll(html)
            .lastOrNull()
            ?.groupValues
            ?.get(1)
            ?.toInt()
            ?: 0
        val warnings = Regex("\\b(\\d+)\\s+warnings?\\b", RegexOption.IGNORE_CASE)
            .findAll(html)
            .lastOrNull()
            ?.groupValues
            ?.get(1)
            ?.toInt()
            ?: 0
        if (errors + warnings > 0) {
            return errors + warnings
        }
    }

    return 0
}
