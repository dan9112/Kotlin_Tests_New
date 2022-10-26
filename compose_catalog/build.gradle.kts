@file:Suppress("UnstableApiUsage")

import org.gradle.api.JavaVersion.VERSION_11
import org.gradle.api.JavaVersion.VERSION_1_8

val composeVersion = "1.3.0"
val koinVersion = "3.2.2"
val lifecycleVersion = "2.5.1"
val mockitoVersion = "4.8.1"

val sealedEnumVersion = "0.5.0"

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    kotlin(module = "kapt")
}

android {
    compileSdk = 33
    namespace = "ru.kamaz.compose_catalog"

    defaultConfig {
        applicationId = namespace
        minSdk = 24
        targetSdk = compileSdk
        versionCode = 20
        versionName = "2.1"

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
        targetCompatibility = VERSION_11
    }
    kotlinOptions {
        jvmTarget = VERSION_11.toString()
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
    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.compose.ui:ui:$composeVersion")
    implementation("androidx.compose.material3:material3:1.0.0")
    implementation("androidx.compose.ui:ui-tooling-preview:$composeVersion")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:$lifecycleVersion")
    implementation("androidx.activity:activity-compose:1.6.1")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:$composeVersion")
    debugImplementation("androidx.compose.ui:ui-tooling:$composeVersion")
    debugImplementation("androidx.compose.ui:ui-test-manifest:$composeVersion")

    // Sealed-Enum (Beta)
    implementation("com.github.livefront.sealed-enum:runtime:$sealedEnumVersion")
    kapt("com.github.livefront.sealed-enum:processor:$sealedEnumVersion")

    // Koin for Android
    implementation("io.insert-koin:koin-android:$koinVersion")

    // Koin Test
    testImplementation("io.insert-koin:koin-test:$koinVersion")
    testImplementation("io.insert-koin:koin-test-junit4:$koinVersion")

    // ViewModel utilities for Compose
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:$lifecycleVersion")

    // Robolectric environment
    testImplementation("androidx.test:core:1.4.0")
    // Mockito framework
    testImplementation("org.mockito:mockito-core:$mockitoVersion")
    androidTestImplementation("org.mockito:mockito-android:$mockitoVersion")
    // mockito-kotlin
    testImplementation("org.mockito.kotlin:mockito-kotlin:4.0.0")
    androidTestImplementation("org.mockito.kotlin:mockito-kotlin:4.0.0")
    // Mockk framework
    testImplementation("io.mockk:mockk:1.13.2")

    // Mockk framework for instrumented tests
    androidTestImplementation("io.mockk:mockk-android:1.12.5")

    // Truth framework
    testImplementation("com.google.truth:truth:1.1.3")
    androidTestImplementation("com.google.truth:truth:1.1.3")

    // SplashScreen
    implementation("androidx.core:core-splashscreen:1.0.0")
}
