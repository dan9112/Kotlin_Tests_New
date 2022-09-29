@file:Suppress("UnstableApiUsage")

val javaVersion = JavaVersion.VERSION_1_8

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    compileSdk = 33

    defaultConfig {
        applicationId = "ru.kamaz.rxjava_3"
        minSdk = 23
        targetSdk = 33
        versionCode = 5
        versionName = "2.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
    }
    kotlinOptions {
        jvmTarget = javaVersion.toString()
    }

    buildFeatures {
        dataBinding = true
    }
}

dependencies {

    implementation(dependencyNotation = "androidx.core:core-ktx:1.9.0")
    implementation(dependencyNotation = "androidx.appcompat:appcompat:1.5.1")
    implementation(dependencyNotation = "com.google.android.material:material:1.6.1")
    implementation(dependencyNotation = "androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation(dependencyNotation = "junit:junit:4.13.2")
    androidTestImplementation(dependencyNotation = "androidx.test.ext:junit:1.1.3")
    androidTestImplementation(dependencyNotation = "androidx.test.espresso:espresso-core:3.4.0")

    implementation(dependencyNotation = "io.reactivex.rxjava3:rxkotlin:3.0.1")
    implementation(dependencyNotation = "com.jakewharton.rxbinding:rxbinding:0.4.0")

    implementation("com.jakewharton.timber:timber:5.0.1")

}
