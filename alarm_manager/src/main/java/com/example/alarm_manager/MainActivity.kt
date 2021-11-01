package com.example.alarm_manager

import android.Manifest.permission.SCHEDULE_EXACT_ALARM
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.AlarmManager.RTC_WAKEUP
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.app.TimePickerDialog.OnTimeSetListener
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.alarm_manager.databinding.ActivityMainBinding
import java.util.*
import java.util.Calendar.*


class MainActivity : AppCompatActivity() {

    private val rqsTime = 1
    private lateinit var binding: ActivityMainBinding

    private lateinit var viewModel: MainViewModel

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val flag = intent.getBooleanExtra("PlayMusic", false)
            viewModel.musicIsInProcess.value = flag

            if (!flag) unregisterReceiver(this)
        }
    }

    private val broadcastIntentFilter = IntentFilter().apply {
        addAction(musicPlayerAction)
    }

    // Слушатель выбора времени
    private val onTimeSetListener =
        OnTimeSetListener { _, hourOfDay, minute ->
            val calNow = getInstance()
            val calSet = calNow.clone() as Calendar
            calSet[HOUR_OF_DAY] = hourOfDay
            calSet[MINUTE] = minute
            calSet[SECOND] = 0
            calSet[MILLISECOND] = 0
            if (calSet <= calNow) {
                // Если выбранное время на сегодня прошло,
                // то переносим на завтра
                calSet.add(DATE, 1)
            }
            trySetAlarm(calSet)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java).apply {
            binding = DataBindingUtil.setContentView<ActivityMainBinding>(
                this@MainActivity,
                R.layout.activity_main
            )
                .apply {
                    musicIsInProcess.observe(this@MainActivity, { value ->
                        when (value) {
                            true -> {
                                butttonShowDialog.apply {
                                    setOnClickListener {
                                        startService(
                                            Intent(
                                                context,
                                                MusicService::class.java
                                            ).apply {
                                                putExtra("Command", false)
                                            })
                                        cancelAlarm()
                                    }
                                    text = "Выключить сигнализацию"
                                    isEnabled = true
                                }
                            }
                            false -> {
                                butttonShowDialog.apply {
                                    setOnClickListener {
                                        textViewAlarmPrompt.text = ""
                                        openTimePickerDialog(true)
                                    }
                                    text = "Установить сигнализацию"
                                    isEnabled = true
                                }
                            }
                            null -> musicIsInProcess.value = false
                        }
                    })

                    // Кнопка отмены сигнализации
                    buttonCancel.setOnClickListener {
                        cancelAlarm()
                    }
                }
        }
    }

    // Вызываем диалоговое окно выбора времени
    private fun openTimePickerDialog(is24r: Boolean) {
        getInstance().apply {
            TimePickerDialog(
                this@MainActivity, onTimeSetListener,
                this[HOUR_OF_DAY],
                this[MINUTE], is24r
            ).apply {
                setTitle("Выберите время")
                show()
            }
        }
    }

    private fun trySetAlarm(targetCal: Calendar) {
        if (SDK_INT < VERSION_CODES.S) setAlarm(targetCal)
        else {
            val permissionAskListener = object : PermissionUtils.PermissionAskListener {
                override fun onPermissionGranted() {
                    setAlarm(targetCal)
                }

                override fun onPermissionRequest() {
                    startActivity(Intent(SCHEDULE_EXACT_ALARM))
                }

                override fun onPermissionPreviouslyDenied() {
                    setAlarm2(targetCal)
                }

                override fun onPermissionDisabled() {
                    setAlarm2(targetCal)
                }
            }
            PermissionUtils.checkPermission(
                this@MainActivity,
                SCHEDULE_EXACT_ALARM,
                permissionAskListener,
                "permissionFlag"
            )
        }
    }

    /** Установка будильника с точным временем срабатывания */
    @SuppressLint("SetTextI18n", "UnspecifiedImmutableFlag")
    private fun setAlarm(targetCal: Calendar) {
        binding.apply {
            textViewAlarmPrompt.text = "Сигнализация установлена на ${targetCal.time}"
            butttonShowDialog.isEnabled = false
        }
        registerReceiver(broadcastReceiver, broadcastIntentFilter)

        val pendingIntent = PendingIntent.getBroadcast(
            applicationContext,
            rqsTime,
            Intent(applicationContext, AlarmReceiver::class.java),
            0
        )
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        alarmManager.setExactAndAllowWhileIdle(
            RTC_WAKEUP,
            targetCal.timeInMillis,
            pendingIntent
        )
    }

    /** Установка будильника с неточным временем срабатывания */
    @SuppressLint("SetTextI18n", "UnspecifiedImmutableFlag")
    private fun setAlarm2(targetCal: Calendar) {
        binding.apply {
            textViewAlarmPrompt.text = "Сигнализация установлена на ${targetCal.time}"
            butttonShowDialog.isEnabled = false
        }
        registerReceiver(broadcastReceiver, broadcastIntentFilter)

        val pendingIntent = PendingIntent.getBroadcast(
            applicationContext,
            rqsTime,
            Intent(applicationContext, AlarmReceiver::class.java),
            0
        )
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        alarmManager.setAndAllowWhileIdle(
            RTC_WAKEUP,
            targetCal.timeInMillis,
            pendingIntent
        )
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    private fun cancelAlarm() {
        binding.textViewAlarmPrompt.text = "Сигнализация отменена!"
        (getSystemService(ALARM_SERVICE) as AlarmManager).cancel(
            PendingIntent.getBroadcast(
                applicationContext,
                rqsTime,
                Intent(applicationContext, AlarmReceiver::class.java),
                0
            )
        )
        viewModel.musicIsInProcess.value = null
    }

    companion object {
        const val musicPlayerAction = "ru.this.music_service.play"
    }
}
