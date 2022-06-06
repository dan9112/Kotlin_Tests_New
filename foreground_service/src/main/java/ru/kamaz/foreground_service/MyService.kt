package ru.kamaz.foreground_service

import android.app.*
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Binder
import androidx.core.app.NotificationCompat
import androidx.navigation.NavDeepLinkBuilder

class MyService : Service() {

    private val binder = MyBinder()

    override fun onBind(intent: Intent) = binder

    private lateinit var notificationManager: NotificationManager

    private val notificationBuilder =
        NotificationCompat.Builder(this, notificationChannelBluetoothStateId)
            .setSmallIcon(R.mipmap.ic_launcher)

    override fun onCreate() {
        super.onCreate()
        notificationManager =
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).apply {
                createNotificationChannel(
                    NotificationChannel(
                        notificationChannelBluetoothStateId,
                        notificationChannelBluetoothStateName,
                        NotificationManager.IMPORTANCE_DEFAULT
                    ).apply {
                        lockscreenVisibility = Notification.VISIBILITY_SECRET
                        setSound(
                            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
                            null
                        )
                        lightColor = R.attr.colorPrimary
                        vibrationPattern = longArrayOf(200, 200)
                        enableLights(true)
                        enableVibration(true)
                    }
                )
            }
    }

    private var previousStartId: Int? = null

    private var count = 0

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        previousStartId?.let{stopSelf(it)}
        previousStartId = startId
        when (val currentCommand = intent.getParcelableExtra<Command>(command)!!) {
            is Command.Start -> {
                val contentPendingIntent = NavDeepLinkBuilder(context = this).run {
                    setGraph(R.navigation.my_navigation_graph)
                    currentCommand.route.forEach {
                        addDestination(destId = it.destId, args = it.destArgs)
                    }
                    createPendingIntent()
                }
                if (count == 0) startForeground(
                    911, notificationBuilder
                        .setContentIntent(contentPendingIntent)
                        .setChannelId(notificationChannelBluetoothStateId)
                        .setContentTitle("Статус Bluetooth")
                        .setContentText("Unknown")
                        .addAction(
                            android.R.drawable.ic_menu_close_clear_cancel, "Завершить", PendingIntent.getService(
                                this, 666, Intent(this, this::class.java).apply {
                                    putExtra(command, Command.Stop())
                                }, FLAG_IMMUTABLE
                            )
                        )
                        .build()
                )
                else notificationManager.notify(
                    911, notificationBuilder
                        .setContentTitle("Повторный вызов статуса Bluetooth")
                        .setContentText("Unknown [$count]")
                        .build()
                )
                count++
            }
            is Command.Stop -> {
                previousStartId = null
                count = 0
                stopSelf()
            }
        }
        return START_REDELIVER_INTENT
    }

    inner class MyBinder : Binder() {
        val service
            get() = this@MyService
    }

    companion object {
        const val command = "myServiceCommand"

        const val notificationChannelBluetoothStateId = "Bluetooth_state_channel_id"
        const val notificationChannelBluetoothStateName = "Bluetooth state channel"
    }
}
