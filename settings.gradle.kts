dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        val mapboxDownloadToken = "sk.eyJ1Ijoibm9wZTEzIiwiYSI6ImNrendrenlnbTZ3OXozMG8xNzc4OGFucWwifQ.HP7IwymwxS0zLxRizEnmxQ"
        google()
        mavenCentral()
        maven { setUrl("https://plugins.gradle.org/m2") }
        maven {
            setUrl("https://api.mapbox.com/downloads/v2/releases/maven")
            authentication {
                create<BasicAuthentication>("basic")
            }
            credentials {
                username = "mapbox"
                password = mapboxDownloadToken
            }
        }
        maven { setUrl("https://jitpack.io") }
    }
}
rootProject.name = "Kotlin Tests"
include (":app")
include (":file_scanner")
include (":tree_recyclerview")
include (":android_paging")
include (":mapboxmap")
