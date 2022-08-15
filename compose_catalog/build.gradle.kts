import org.gradle.api.JavaVersion.VERSION_11

val composeVersion = "1.2.0"

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    compileSdk = 33

    defaultConfig {
        applicationId = "ru.kamaz.compose_catalog"
        minSdk = 23
        targetSdk = 33
        versionCode = 3
        versionName = "1.2"

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
        sourceCompatibility = VERSION_11
        targetCompatibility = VERSION_11
    }
    kotlinOptions {
        jvmTarget = VERSION_11.toString()
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = composeVersion
    }
    packagingOptions {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    implementation(dependencyNotation = "androidx.core:core-ktx:1.8.0")
    implementation(dependencyNotation = "androidx.compose.ui:ui:$composeVersion")
    implementation(dependencyNotation = "androidx.compose.material3:material3:1.0.0-alpha16")
    implementation(dependencyNotation = "androidx.compose.ui:ui-tooling-preview:$composeVersion")
    implementation(dependencyNotation = "androidx.lifecycle:lifecycle-runtime-ktx:2.5.1")
    implementation(dependencyNotation = "androidx.activity:activity-compose:1.5.1")
    testImplementation(dependencyNotation = "junit:junit:4.13.2")
    androidTestImplementation(dependencyNotation = "androidx.test.ext:junit:1.1.3")
    androidTestImplementation(dependencyNotation = "androidx.test.espresso:espresso-core:3.4.0")
    androidTestImplementation(dependencyNotation = "androidx.compose.ui:ui-test-junit4:$composeVersion")
    debugImplementation(dependencyNotation = "androidx.compose.ui:ui-tooling:$composeVersion")
    debugImplementation(dependencyNotation = "androidx.compose.ui:ui-test-manifest:$composeVersion")
    implementation(kotlin("reflect"))
}