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
include(
    ":app",
    ":content_provider",
    ":diff_util",
    ":file_scanner",
    ":foreground_service",
    ":rxkotlin_3",
    ":simple_app_for_content_provider_test",
    ":tree_recyclerview"
)
