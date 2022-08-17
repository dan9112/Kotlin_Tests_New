package ru.kamaz.compose_catalog

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level.DEBUG
import ru.kamaz.compose_catalog.di.AppModules.appModule

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger(level = DEBUG)
            androidContext(androidContext = this@MainApplication)
            modules(modules = appModule)
        }
    }
}
