// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(notation = libs.plugins.androidApplication) apply false
    alias(notation = libs.plugins.org.jetbrains.kotlin.android) apply false
    alias(notation = libs.plugins.compose.compiler) apply false
    alias(notation = libs.plugins.sqldelight) apply false
    alias(notation = libs.plugins.kotlin.parcelize) apply false
}
