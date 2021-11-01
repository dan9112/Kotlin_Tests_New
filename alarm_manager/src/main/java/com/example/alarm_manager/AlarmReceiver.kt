package com.example.alarm_manager

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast.LENGTH_LONG
import android.widget.Toast.makeText

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        makeText(
            context, "Дзинь-дзинь! Пора кормить кота",
            LENGTH_LONG
        ).show()
    }
}