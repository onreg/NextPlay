pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "NextPlay"
include(":app")
include(":core:ui")
include(":core:network")
include(":core:db")
include(":core:util-android")
include(":data:game:api")
include(":data:game:impl")
include(":data:details:api")
include(":data:details:impl")
include(":data:screenshots:api")
include(":data:screenshots:impl")
include(":data:movies:api")
include(":data:movies:impl")
include(":data:series:api")
include(":data:series:impl")
include(":feature:game")
include(":feature:game-details")
include(":presentation:game")
include(":presentation:platform")
include(":presentation:details")
include(":testing:unit")
