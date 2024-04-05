plugins {
    alias(notation = libs.plugins.org.jetbrains.kotlin.jvm)
}

kotlin {
    jvmToolchain(jdkVersion = 21)
}

tasks.test {
    useJUnitPlatform()
}

dependencies {
//    implementation(libs.kotlinx.coroutines.core)
}