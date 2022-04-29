package ru.kamaz.service_bind_and_start

import android.app.Application
import timber.log.Timber.DebugTree
import timber.log.Timber.Forest.plant

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) plant(DebugTree())
    }
}
