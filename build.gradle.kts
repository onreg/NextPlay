plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.version.catalog.update)
    id("ktlint")
    id("detekt")
    id("lint")
}

val codeQuality by tasks.registering {
    group = "verification"
    description = "Runs detekt, ktlint, and lint across the whole repo."
    dependsOn(tasks.named("detektCheck"))
}

tasks.named("detektCheck").configure {
    finalizedBy(tasks.named("ktlintCheck"))
}

tasks.named("ktlintCheck").configure {
    finalizedBy(tasks.named("lintCheck"))
}
