package ru.kamaz.service_bind_and_start.presentation.app

import android.app.Application
import ru.kamaz.service_bind_and_start.BuildConfig.DEBUG
import timber.log.Timber.DebugTree
import timber.log.Timber.Forest.plant

@Suppress("unused")// используется только в манифесте
class App : Application() {
    override fun onCreate() {
        super.onCreate()

        if (DEBUG) plant(DebugTree())
    }
}
