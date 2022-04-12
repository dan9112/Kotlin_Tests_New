package ru.kamaz.foreground_service

import android.annotation.TargetApi
import android.app.*
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.media.app.NotificationCompat.MediaStyle

class ForegroundMusicService : Service() {
    private val binder = LocalBinder()

    var mForeground = false

    private lateinit var notificationBuilder: NotificationCompat.Builder

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        createNotification()
    }

    private var prevStartId: Int? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        prevStartId?.let { stopSelf(it) }
        prevStartId = startId
        if (intent != null) {
            Log.i("Service", intent.toString())
             updateNotification()
             if (intent.getBooleanExtra("startForeground", false)) {
                 if (!mForeground) setForeground(notificationBuilder.build())
             } else {
                 if (mForeground) {
                     stopForeground(true)
                     mForeground = false
                 }
             }
        }
        return START_NOT_STICKY
    }

    private val updateNotification: () -> Unit = {
        val notification = notificationBuilder
            .setSmallIcon(android.R.drawable.btn_star_big_on)
            .build()
        NotificationManagerCompat.from(this).notify(NOTIFICATION_ID, notification)
    }

    private val createNotification: () -> Unit = {
        notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) createNotificationChannel()
        else notificationBuilder.priority = NotificationCompat.PRIORITY_LOW

        val openPlayerIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val flags = if (VersioningHelper.isMarshmallow()) PendingIntent.FLAG_IMMUTABLE or 0 else 0
        val contentIntent = PendingIntent.getActivity(
            this,
            NOTIFICATION_INTENT_REQUEST_CODE,
            openPlayerIntent,
            flags
        )

        notificationBuilder.setContentIntent(contentIntent).setShowWhen(false).setOngoing(true)
            .addAction(
                NotificationCompat.Action.Builder(
                    android.R.drawable.ic_media_play,
                    PLAY_PAUSE_ACTION,
                    getPlayerAction(PLAY_PAUSE_ACTION)
                ).build()
            )
            .setStyle(
                MediaStyle().setShowActionsInCompactView(0)
            )
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
    }

    private fun getPlayerAction(playerAction: String): PendingIntent {
        val intent = Intent().apply {
            action = playerAction
            component =
                ComponentName(this@ForegroundMusicService, ForegroundMusicService::class.java)
        }
        val flags = if (VersioningHelper.isMarshmallow()) {
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        return PendingIntent.getService(this, NOTIFICATION_INTENT_REQUEST_CODE, intent, flags)
    }

    private val setForeground: (Notification) -> Unit = {
        if (VersioningHelper.isQ()) startForeground(
            NOTIFICATION_ID,
            it,
            ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
        ) else startForeground(NOTIFICATION_ID, it)
        mForeground = true
    }

    @TargetApi(26)
    private fun createNotificationChannel() {
        with(getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager) {
            if (getNotificationChannel(NOTIFICATION_CHANNEL_ID) == null) {
                NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    getString(R.string.app_name),
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = getString(R.string.app_name)
                    enableLights(false)
                    enableVibration(false)
                    setShowBadge(false)
                    createNotificationChannel(this)
                }
            }
        }
    }

    inner class LocalBinder : Binder() {
        // Return this instance of PlayerService so we can call public methods
        fun getService(): ForegroundMusicService = this@ForegroundMusicService
    }

    companion object {
        const val NOTIFICATION_ID = 666
        const val NOTIFICATION_CHANNEL_ID = "NCI^^^"
        const val NOTIFICATION_INTENT_REQUEST_CODE = 113

        const val PLAY_PAUSE_ACTION = "ru.kamaz.foreground_s.play_pause"
        const val STOP_ACTION = "ru.kamaz.foreground_s.stop"
        const val NOTIFY_ACTION = "ru.kamaz.foreground_s.notify"
        const val CLOSE_ACTION = "ru.kamaz.foreground_s.close"
    }
}