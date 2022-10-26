plugins {
    id("com.android.application") version "7.3.1" apply false
    id("com.android.library") version "7.3.1" apply false
    id("org.jetbrains.kotlin.android") version "1.7.20" apply false
    id("org.jetbrains.kotlin.jvm") version "1.7.20" apply false
    id("androidx.navigation.safeargs.kotlin") version "2.5.1" apply false
}

tasks.register<Delete>("clean") {
    delete(rootProject.buildDir)
}