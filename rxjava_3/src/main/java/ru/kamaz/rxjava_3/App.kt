package ru.kamaz.rxjava_3

import android.app.Application
import ru.kamaz.rxjava_3.BuildConfig.DEBUG
import timber.log.Timber.DebugTree
import timber.log.Timber.Forest.plant

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        if (DEBUG) plant(DebugTree())
    }
}
