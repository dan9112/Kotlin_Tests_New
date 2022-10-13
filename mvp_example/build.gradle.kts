@file:Suppress("UnstableApiUsage")

val javaVersion = JavaVersion.VERSION_11

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("de.mannodermaus.android-junit5") version "1.8.2.1"
}

android {
    compileSdk = 33
    namespace = "com.example.mvp_example"

    defaultConfig {
        applicationId = namespace
        minSdk = 21
        targetSdk = compileSdk
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        testInstrumentationRunnerArguments["runnerBuilder"] = "de.mannodermaus.junit5.AndroidJUnit5Builder"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
    }
    kotlinOptions {
        jvmTarget = javaVersion.toString()
    }
    testOptions {
        unitTests.isReturnDefaultValues = true
    }
    buildFeatures {
        viewBinding = true
    }
}

tasks.withType<Test>{
    useJUnitPlatform()
}

dependencies {
    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.5.1")
    implementation("com.google.android.material:material:1.6.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    testImplementation("org.junit.jupiter:junit-jupiter:5.9.0")
    testImplementation("com.google.truth:truth:1.1.3")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.4")

    testImplementation("org.mockito.kotlin:mockito-kotlin:4.0.0")

    androidTestImplementation("com.google.truth:truth:1.1.3")

    androidTestImplementation("androidx.test:runner:1.4.0")
    androidTestImplementation("org.junit.jupiter:junit-jupiter-api:5.9.0")

    androidTestImplementation("de.mannodermaus.junit5:android-test-core:1.3.0")
    androidTestRuntimeOnly("de.mannodermaus.junit5:android-test-runner:1.3.0")

    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
}
