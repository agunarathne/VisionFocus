pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // Story 7.4 Note: Mapbox SDK not included
        // Reason: Maven repository requires secret token (sk.*) with DOWNLOADS:READ scope
        // Using Google Maps SDK for offline map metadata tracking instead
    }
}

rootProject.name = "VisionFocus"
include(":app")
