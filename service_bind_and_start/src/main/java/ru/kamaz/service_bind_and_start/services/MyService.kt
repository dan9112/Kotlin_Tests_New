package ru.kamaz.service_bind_and_start.services

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder

class MyService : Service() {
    private var binder: MyBinder = MyBinder()
    private var previousStartId: Int? = null

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        previousStartId?.let { stopSelf(it) }
        previousStartId = startId
        return START_REDELIVER_INTENT
    }

    override fun onUnbind(intent: Intent?) = run {

        false
    }

    inner class MyBinder : Binder() {
        val service: MyService
            get() = this@MyService
    }
}
