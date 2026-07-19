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
        // for testing locally built android-preference-annotations
        // (./gradlew publishToMavenLocal -x signJavaPublication in that repo)
        mavenLocal {
            content {
                includeGroup("eu.jonahbauer")
            }
        }
        google()
        mavenCentral()
    }
}

rootProject.name = "Launcher"
include(":app")