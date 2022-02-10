package ru.kamaz.nier_visualizer

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder

class MusicService : Service() {
    lateinit var player: MediaPlayer

    private var binder = MusicServiceBinder()

    override fun onCreate() {
        super.onCreate()
        initPlayer()
    }

    override fun onDestroy() {
        super.onDestroy()
        player.run {
            reset()
            release()
        }
    }

    private fun initPlayer() {
        player = MediaPlayer.create(this, R.raw.my_darkest_days__casual_sex)
    }

    override fun onBind(intent: Intent): IBinder = binder

    fun startOrPause() = if (player.isPlaying) {
        startService(Intent(this@MusicService, MusicService::class.java).setAction(actionPause))
        false
    } else {
        startService(Intent(this@MusicService, MusicService::class.java).setAction(actionStart))
        true
    }

    fun stop() {
        startService(Intent(this@MusicService, MusicService::class.java).setAction(actionStop))
    }

    inner class MusicServiceBinder : Binder() {
        val service
            get() = this@MusicService
    }

    private var previousStartId: Int? = null

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int) = player.run {
        when (intent.action) {
            actionStart -> start()
            actionPause -> pause()
            actionStop -> {// stop function works incorrect
                pause()
                seekTo(0)
            }
        }
        if (intent.action != actionDestroy) {
            previousStartId?.let { stopSelf(it) }
            previousStartId = startId
        } else stopSelf()
        START_REDELIVER_INTENT
    }

    companion object {
        const val actionStart = "my.action.start"
        const val actionPause = "my.action.pause"
        const val actionStop = "my.action.stop"
        const val actionDestroy = "my.action.destroy"
    }
}
