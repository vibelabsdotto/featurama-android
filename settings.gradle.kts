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
    }
}

rootProject.name = "FeaturamaTester"
include(":app")

includeBuild("../../sdks/android") {
    dependencySubstitution {
        substitute(module("io.featurama:featurama")).using(project(":featurama"))
    }
}
