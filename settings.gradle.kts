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
        maven(url = "https://jitpack.io")
    }
}
rootProject.name = "Kotlin Tests"
include (":app", ":camerax", ":compose_catalog", ":diff_util", ":file_scanner", ":foreground_service", ":jetpack_compose", ":mvp_example", ":tree_recyclerview")
