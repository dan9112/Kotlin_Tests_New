import org.gradle.api.JavaVersion.VERSION_1_8

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    compileSdk = 31

    defaultConfig {
        applicationId = "ru.kamaz.mapboxmap"
        minSdk = 29
        targetSdk = 31
        versionCode = 9
        versionName = "5.1"

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
        targetCompatibility = VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.7.0")
    implementation("androidx.appcompat:appcompat:1.4.1")
    implementation("com.google.android.material:material:1.5.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.3")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")

    // MapBox
    implementation("com.mapbox.maps:android:10.4.0-beta.1")

    // MapBox Navigation
    implementation("com.mapbox.navigation:android:2.2.2")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")

    // Google location services
    implementation("com.google.android.gms:play-services-location:19.0.1")

    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.4.1")

    // Activity extensions
    implementation ("androidx.activity:activity-ktx:1.4.0")

    // LeakCanary
    debugImplementation ("com.squareup.leakcanary:leakcanary-android:2.8.1")
}
