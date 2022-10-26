@file:Suppress("UnstableApiUsage")

import org.gradle.api.JavaVersion.VERSION_1_8

val composeVersion = "1.2.1"

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    compileSdk = 33
    namespace = "ru.kamaz.jetpack_compose"

    defaultConfig {
        applicationId = namespace
        minSdk = 23
        targetSdk = compileSdk
        versionCode = 7
        versionName = "6"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
        sourceCompatibility = VERSION_1_8
        targetCompatibility = VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = VERSION_1_8.toString()
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.3.2"
    }
    packagingOptions {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    implementation(dependencyNotation = "androidx.core:core-ktx:1.9.0")

    implementation(dependencyNotation = "androidx.compose.ui:ui:$composeVersion")
    implementation(dependencyNotation = "androidx.compose.material:material:$composeVersion")
    implementation(dependencyNotation = "androidx.compose.ui:ui-tooling-preview:$composeVersion")
    implementation(dependencyNotation = "androidx.lifecycle:lifecycle-runtime-ktx:2.5.1")
    implementation(dependencyNotation = "androidx.activity:activity-compose:1.6.1")
    testImplementation(dependencyNotation = "junit:junit:4.13.2")
    androidTestImplementation(dependencyNotation = "androidx.test.ext:junit:1.1.3")
    androidTestImplementation(dependencyNotation = "androidx.test.espresso:espresso-core:3.4.0")
    androidTestImplementation(dependencyNotation = "androidx.compose.ui:ui-test-junit4:$composeVersion")
    debugImplementation(dependencyNotation = "androidx.compose.ui:ui-tooling:$composeVersion")
    debugImplementation(dependencyNotation = "androidx.compose.ui:ui-test-manifest:$composeVersion")

    // Timber
    implementation(dependencyNotation = "com.jakewharton.timber:timber:5.0.1")

    // ConstraintLayout
    implementation(dependencyNotation = "androidx.constraintlayout:constraintlayout-compose:1.0.1")
}
