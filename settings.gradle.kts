@file:Suppress("UnstableApiUsage")
pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
rootProject.name = "Kotlin Tests"
include(":mvp_example")
include(":app", ":diff_util", ":file_scanner", ":foreground_service", ":rxkotlin_3", ":tree_recyclerview")
