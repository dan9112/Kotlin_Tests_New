// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(notation = libs.plugins.com.android.library) apply false
}

tasks.register(name = "clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}
