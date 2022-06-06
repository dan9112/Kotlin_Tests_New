dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
rootProject.name = "Kotlin Tests"
include (":app")
include (":file_scanner")
include (":tree_recyclerview")
include (":diff_util")
include (":rx_java")
include (":foreground_service")
