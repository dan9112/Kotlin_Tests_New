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
include (":app")
include (":file_scanner")
include (":tree_recyclerview")
include (":diff_util")
include (":foreground_service")
include(":rxjava_3")
include(":matrix")
