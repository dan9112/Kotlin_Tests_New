// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath(dependencyNotation = "com.android.tools.build:gradle:7.2.1")
        classpath(dependencyNotation = "org.jetbrains.kotlin:kotlin-gradle-plugin:1.7.0")
        classpath(dependencyNotation = "androidx.navigation:navigation-safe-args-gradle-plugin:2.5.1")

        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
    }
}

tasks.register<Delete>("clean") {
    delete(rootProject.buildDir)
}