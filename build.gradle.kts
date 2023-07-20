// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "7.4.1" apply false
    id("com.android.library") version "7.4.1" apply false
    id("org.jetbrains.kotlin.android") version "1.8.21" apply false
    id("androidx.navigation.safeargs.kotlin") version "2.6.0" apply false
}

tasks.register<Delete>(name = "clean") {
    delete(rootProject.buildDir)
}
