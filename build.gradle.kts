// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "7.2.2" apply false
    id("com.android.library") version "7.2.2" apply false
    id("org.jetbrains.kotlin.android") version "1.7.0" apply false
    id("org.jetbrains.kotlin.jvm") version "1.6.10" apply false
    id("androidx.navigation.safeargs.kotlin") version "2.5.1" apply false
}

tasks.register<Delete>("clean") {
    delete(rootProject.buildDir)
}