package com.example.alarm_manager

import android.annotation.SuppressLint
import android.app.AlarmManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.example.alarm_manager.databinding.ActivityMainBinding
import android.app.PendingIntent
import android.widget.Toast

import android.content.Intent
import java.util.*


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private lateinit var mPendingIntent: PendingIntent

    @SuppressLint("UnspecifiedImmutableFlag")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)
            .apply {
                buttonStartAlarm.setOnClickListener {
                    val myIntent = Intent(
                        this@MainActivity,
                        MyAlarmService::class.java
                    )
                    mPendingIntent = PendingIntent.getService(
                        this@MainActivity, 0,
                        myIntent, 0
                    )
                    val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
                    val calendar = Calendar.getInstance().apply {
                        timeInMillis = System.currentTimeMillis()
                        add(Calendar.SECOND, 15)
                    }
                    alarmManager[AlarmManager.RTC_WAKEUP, calendar.timeInMillis] =
                        mPendingIntent
                    Toast.makeText(
                        this@MainActivity, "Устанавливаем сигнализацию",
                        Toast.LENGTH_LONG
                    ).show()
                }
                buttonCancelAlarm.setOnClickListener {
                    val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
                    alarmManager.cancel(mPendingIntent)
                    Toast.makeText(
                        this@MainActivity, "Отмена!",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }
}