package com.example.alarm_manager.main

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
import com.example.alarm_manager.*
import com.example.alarm_manager.databinding.ActivityMainBinding
import java.util.*
import java.util.Calendar.*


class MainActivity : AppCompatActivity() {

    private val rqsTime = 1
    private lateinit var binding: ActivityMainBinding

    private lateinit var viewModel: MainViewModel

    private lateinit var targetCal: Calendar

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
            targetCal = calSet
            trySetAlarm()
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
                            true -> butttonShowDialog.run {
                                    setOnClickListener {
                                        startService(
                                            Intent(
                                                context,
                                                MusicService::class.java
                                            ).run {
                                                putExtra("Command", false)
                                            })
                                        cancelAlarm()
                                    }
                                    text = "Выключить сигнализацию"
                                    isEnabled = true
                                }
                            false -> butttonShowDialog.run {
                                    setOnClickListener {
                                        textViewAlarmPrompt.text = ""
                                        openTimePickerDialog(true)
                                    }
                                    text = "Установить сигнализацию"
                                    isEnabled = true
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
        getInstance().run {
            TimePickerDialog(
                this@MainActivity, onTimeSetListener,
                this[HOUR_OF_DAY],
                this[MINUTE], is24r
            ).run {
                setTitle("Выберите время")
                show()
            }
        }
    }

    private fun trySetAlarm() {
        if (SDK_INT < VERSION_CODES.S) setAlarm()
        else {
            val permissionAskListener = object : PermissionUtils.PermissionAskListener {
                override fun onPermissionGranted() {
                    setAlarm()
                }

                override fun onPermissionRequest() {
                    startActivity(Intent(SCHEDULE_EXACT_ALARM))
                }

                override fun onPermissionPreviouslyDenied() {
                    viewModel.run {
                        if (!dialogIsShowing) {
                            dialogIsShowing = !dialogIsShowing
                            PermissionDialogFragment().show(
                                supportFragmentManager,
                                "PermissionDialogFragment"
                            )
                        }
                    }
                }

                override fun onPermissionDisabled() {
                    setAlarm2()
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
    private fun setAlarm() {
        binding.run {
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
    private fun setAlarm2() {
        binding.run {
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

    val dialogPositiveChoice: Unit
        get() {
            viewModel.dialogIsShowing = false
            setAlarm()
        }

    val dialogNegativeChoice
        get() = run {
            viewModel.dialogIsShowing = false
            setAlarm2()
        }

    companion object {
        const val musicPlayerAction = "ru.this.music_service.play"
    }
}