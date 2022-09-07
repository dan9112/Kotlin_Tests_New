import org.gradle.api.JavaVersion.VERSION_11
import org.gradle.api.JavaVersion.VERSION_1_8

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    compileSdk = 32

    defaultConfig {
        applicationId = "ru.kamaz.matrix"
        minSdk = 23
        targetSdk = 32
        versionCode = 2
        versionName = "1.1"

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
        sourceCompatibility = VERSION_1_8
        targetCompatibility = VERSION_11
    }
    kotlinOptions {
        jvmTarget = VERSION_11.toString()
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation(dependencyNotation = "androidx.core:core-ktx:1.8.0")
    implementation(dependencyNotation = "androidx.appcompat:appcompat:1.5.0")
    implementation(dependencyNotation = "com.google.android.material:material:1.8.0-alpha01")
    implementation(dependencyNotation = "androidx.constraintlayout:constraintlayout:2.1.4")
    implementation(dependencyNotation = "androidx.navigation:navigation-fragment-ktx:2.5.1")
    implementation(dependencyNotation = "androidx.navigation:navigation-ui-ktx:2.5.1")
    testImplementation(dependencyNotation = "junit:junit:4.13.2")
    androidTestImplementation(dependencyNotation = "androidx.test.ext:junit:1.1.3")
    androidTestImplementation(dependencyNotation = "androidx.test.espresso:espresso-core:3.4.0")
}
