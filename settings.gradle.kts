pluginManagement {
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

rootProject.name = "CalView"
include(":app")
include(":core-ai")
include(":core-data")
include(":core-ml")
include(":core-ui")
include(":feature-onboarding")
include(":feature-dashboard")
include(":feature-scanner")
include(":feature-trends")
include(":feature-subscription")

