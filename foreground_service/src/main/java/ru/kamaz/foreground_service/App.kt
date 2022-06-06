package ru.kamaz.foreground_service

import android.app.Application

class App: Application() {
    override fun onCreate() {
        super.onCreate()
        with(receiver = getSharedPreferences(topLevelSharedPreferencesFile, MODE_PRIVATE)) {
            if (contains("needSplashScreen")) {
                edit().remove("needSplashScreen").apply()
            }
        }
    }

    companion object {
        const val topLevelSharedPreferencesFile = "tlspFile"
    }
}
