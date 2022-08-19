import org.gradle.api.JavaVersion.VERSION_11
import org.gradle.api.JavaVersion.VERSION_1_8

val composeVersion = "1.2.1"
val koinVersion = "3.2.0"
val lifecycleVersion = "2.5.1"

val sealedEnumVersion = "0.5.0"

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    kotlin(module = "kapt")
}

android {
    compileSdk = 33

    defaultConfig {
        applicationId = "ru.kamaz.compose_catalog"
        minSdk = 23
        targetSdk = 33
        versionCode = 14
        versionName = "1.13"

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
        kotlinCompilerExtensionVersion = "1.3.0"
    }
    packagingOptions {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildToolsVersion = "32.0.0"
}

dependencies {
    implementation(dependencyNotation = "androidx.core:core-ktx:1.8.0")
    implementation(dependencyNotation = "androidx.compose.ui:ui:$composeVersion")
    implementation(dependencyNotation = "androidx.compose.material3:material3:1.0.0-alpha16")
    implementation(dependencyNotation = "androidx.compose.ui:ui-tooling-preview:$composeVersion")
    implementation(dependencyNotation = "androidx.lifecycle:lifecycle-runtime-ktx:$lifecycleVersion")
    implementation(dependencyNotation = "androidx.activity:activity-compose:1.5.1")
    testImplementation(dependencyNotation = "junit:junit:4.13.2")
    androidTestImplementation(dependencyNotation = "androidx.test.ext:junit:1.1.3")
    androidTestImplementation(dependencyNotation = "androidx.test.espresso:espresso-core:3.4.0")
    androidTestImplementation(dependencyNotation = "androidx.compose.ui:ui-test-junit4:$composeVersion")
    debugImplementation(dependencyNotation = "androidx.compose.ui:ui-tooling:$composeVersion")
    debugImplementation(dependencyNotation = "androidx.compose.ui:ui-test-manifest:$composeVersion")

    // Sealed-Enum (Beta)
    implementation(dependencyNotation = "com.github.livefront.sealed-enum:runtime:$sealedEnumVersion")
    kapt(dependencyNotation = "com.github.livefront.sealed-enum:processor:$sealedEnumVersion")

    // Koin for Android
    implementation(dependencyNotation = "io.insert-koin:koin-android:$koinVersion")

    // Koin Test
    testImplementation(dependencyNotation = "io.insert-koin:koin-test:$koinVersion")
    testImplementation(dependencyNotation = "io.insert-koin:koin-test-junit4:$koinVersion")

    // ViewModel utilities for Compose
    implementation(dependencyNotation = "androidx.lifecycle:lifecycle-viewmodel-compose:$lifecycleVersion")

    // Robolectric environment
    testImplementation(dependencyNotation = "androidx.test:core:1.5.0-alpha01")
    // Mockito framework
    testImplementation(dependencyNotation = "org.mockito:mockito-core:4.7.0")
    androidTestImplementation(dependencyNotation = "org.mockito:mockito-android:4.7.0")
    // mockito-kotlin
    testImplementation(dependencyNotation = "org.mockito.kotlin:mockito-kotlin:4.0.0")
    androidTestImplementation(dependencyNotation = "org.mockito.kotlin:mockito-kotlin:4.0.0")
    // Mockk framework
    testImplementation(dependencyNotation = "io.mockk:mockk:1.12.5")

    // Mockk framework for instrumented tests
    androidTestImplementation(dependencyNotation = "io.mockk:mockk-android:1.12.5")

    // Truth framework
    testImplementation(dependencyNotation = "com.google.truth:truth:1.1.3")
    androidTestImplementation(dependencyNotation = "com.google.truth:truth:1.1.3")
}
