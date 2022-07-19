import org.gradle.api.JavaVersion.VERSION_11

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
}

android {
    compileSdk = 32

    defaultConfig {
        applicationId = "ru.kamaz.bluetooth_connection"
        minSdk = 23
        targetSdk = 32
        versionCode = 2
        versionName = "1.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = VERSION_11
        targetCompatibility = VERSION_11
    }
    kotlinOptions {
        jvmTarget = VERSION_11.toString()
    }
    buildFeatures {
        dataBinding = true
    }
}

dependencies {
    implementation(dependencyNotation = "androidx.core:core-ktx:1.8.0")
    implementation(dependencyNotation = "androidx.appcompat:appcompat:1.4.2")
    implementation(dependencyNotation = "com.google.android.material:material:1.6.1")
    implementation(dependencyNotation = "androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation(dependencyNotation = "junit:junit:4.13.2")
    androidTestImplementation(dependencyNotation = "androidx.test.ext:junit:1.1.3")
    androidTestImplementation(dependencyNotation = "androidx.test.espresso:espresso-core:3.4.0")

    implementation(dependencyNotation = "androidx.activity:activity-ktx:1.5.0")

    implementation(dependencyNotation = "androidx.legacy:legacy-support-v4:1.0.0")
    implementation(dependencyNotation = "androidx.lifecycle:lifecycle-livedata-ktx:2.5.0")
    implementation(dependencyNotation = "androidx.lifecycle:lifecycle-viewmodel-ktx:2.5.0")
}
