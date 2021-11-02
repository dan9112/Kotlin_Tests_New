package com.example.coroutines

import android.app.Application
import com.example.coroutines.BuildConfig.DEBUG
import timber.log.Timber

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        if (DEBUG) Timber.plant(Timber.DebugTree())
    }
}
