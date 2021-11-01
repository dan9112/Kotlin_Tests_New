package com.example.alarm_manager

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.i("AlarmReceiver", "Data received")
        context.startService(Intent(context, MusicService::class.java).apply {
            putExtra("Command", true)
        })
    }
}
