package ru.kamaz.jetpack_compose

import android.app.Application
import ru.kamaz.jetpack_compose.BuildConfig.DEBUG
import timber.log.Timber.DebugTree
import timber.log.Timber.Forest.plant

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        if (DEBUG) plant(DebugTree())
    }
}
