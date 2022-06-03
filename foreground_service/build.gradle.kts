import org.gradle.api.JavaVersion.VERSION_11

plugins {
    id ("com.android.application")
    id ("org.jetbrains.kotlin.android")
}

android {
    compileSdk = 32

    defaultConfig {
        applicationId = "ru.kamaz.foreground_service"
        minSdk = 23
        targetSdk = 32
        versionCode = 3
        versionName = "1.2"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile(name = "proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = VERSION_11// by default JavaVersion.VERSION_1_8
        targetCompatibility = VERSION_11// by default JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = VERSION_11.toString()// by default 1.8
    }

    buildFeatures {
        dataBinding = true
    }
}

dependencies {

    implementation (dependencyNotation = "androidx.core:core-ktx:1.8.0")
    implementation (dependencyNotation = "androidx.appcompat:appcompat:1.4.2")
    implementation (dependencyNotation = "com.google.android.material:material:1.6.1")
    implementation (dependencyNotation = "androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation (dependencyNotation = "junit:junit:4.13.2")
    androidTestImplementation (dependencyNotation = "androidx.test.ext:junit:1.1.3")
    androidTestImplementation (dependencyNotation = "androidx.test.espresso:espresso-core:3.4.0")

    // Navigation component
    val navVersion = "2.4.2"
    implementation(dependencyNotation = "androidx.navigation:navigation-fragment-ktx:$navVersion")
    implementation(dependencyNotation = "androidx.navigation:navigation-ui-ktx:$navVersion")
    // Feature module Support
    implementation(dependencyNotation = "androidx.navigation:navigation-dynamic-features-fragment:$navVersion")
}