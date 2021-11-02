package com.example.alarm_manager

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.IBinder
import android.util.Log
import com.example.alarm_manager.main.MainActivity.Companion.musicPlayerAction

class MusicService : Service() {

    private var previousId: Int? = null

    private lateinit var player: MediaPlayer

    override fun onCreate() {
        super.onCreate()
        player = MediaPlayer.create(this, R.raw.true_survivor)
    }

    override fun onBind(intent: Intent): IBinder? = null

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        previousId?.let { stopSelf(it) }
        previousId = startId
        if (intent.getBooleanExtra("Command", false)) {
            player.start()
            sendBroadcast(Intent(musicPlayerAction).apply {
                putExtra("PlayMusic", true)
            })
        }
        else player.apply {
            stop()
            try {
                prepare()
                seekTo(0)
            } catch (t: Throwable) {
                t.message?.let { Log.e("MusicService", it) }
            } finally {
                sendBroadcast(Intent(musicPlayerAction).apply {
                    putExtra("PlayMusic", false)
                })
            }
        }
        return START_STICKY_COMPATIBILITY
    }
}