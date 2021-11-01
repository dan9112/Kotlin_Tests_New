package com.example.alarm_manager

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.app.TimePickerDialog.OnTimeSetListener
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.alarm_manager.databinding.ActivityMainBinding
import java.util.*
import java.util.Calendar.*


class MainActivity : AppCompatActivity() {

    private val rqsTime = 1
    private lateinit var timeTextView: TextView

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
            setAlarm(calSet)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main).apply {
            timeTextView = textViewAlarmPrompt

            butttonShowDialog.setOnClickListener {
                timeTextView.text = ""
                openTimePickerDialog(true)
            }

            // Кнопка отмены сигнализации
            buttonCancel.setOnClickListener {
                cancelAlarm()
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

    @SuppressLint("SetTextI18n", "UnspecifiedImmutableFlag")
    private fun setAlarm(targetCal: Calendar) {
        timeTextView.text = "Сигнализация установлена на ${targetCal.time}"
        (getSystemService(ALARM_SERVICE) as AlarmManager)[AlarmManager.RTC_WAKEUP, targetCal.timeInMillis] =
            PendingIntent.getBroadcast(
                applicationContext,
                rqsTime,
                Intent(applicationContext, AlarmReceiver::class.java),
                0
            )
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    private fun cancelAlarm() {
        timeTextView.text = "Сигнализация отменена!"
        (getSystemService(ALARM_SERVICE) as AlarmManager).cancel(
            PendingIntent.getBroadcast(
                applicationContext,
                rqsTime,
                Intent(applicationContext, AlarmReceiver::class.java),
                0
            )
        )
    }
}