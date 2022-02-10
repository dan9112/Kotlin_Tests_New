package ru.kamaz.nier_visualizer

import android.content.*
import android.os.Bundle
import android.os.IBinder
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.content.ContextCompat.getColor
import androidx.core.content.ContextCompat.getDrawable
import androidx.databinding.DataBindingUtil.setContentView
import androidx.lifecycle.ViewModelProvider
import ru.kamaz.nier_visualizer.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainActivityViewModel

    private lateinit var musicServiceConnection: ServiceConnection

    private var musicService: MusicService? = null
    private var bound = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this, MainActivityAndroidViewModelFactory(application)).get(
            MainActivityViewModel::class.java
        )
        musicServiceConnection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                musicService = (service as MusicService.MusicServiceBinder).service
                binding.run {
                    visualizer.setPlayer(musicService!!.player.audioSessionId)
                    button.run {
                        musicService!!.run {
                            setButtonActualIcon(player.isPlaying)
                            setOnClickListener { setButtonActualIcon(startOrPause()) }
                            setOnLongClickListener {
                                stop()
                                setButtonActualIcon(false)
                                true
                            }
                        }
                    }
                }
                bound = true
            }

            private fun AppCompatImageButton.setButtonActualIcon(pause: Boolean) {
                setImageDrawable(
                    getDrawable(
                        this@MainActivity,
                        if (pause) R.drawable.apollo_holo_dark_pause else R.drawable.apollo_holo_dark_play
                    )
                )
                setColorFilter(
                    getColor(
                        this@MainActivity,
                        android.R.color.holo_red_dark
                    )
                )
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                musicService = null
                binding.button.run {
                    setOnClickListener(null)
                    setOnLongClickListener(null)
                }
                bound = false
            }
        }
        setContentView<ActivityMainBinding>(this, R.layout.activity_main).run {
            binding = this
            visualizer.setColor(getColor(this@MainActivity, android.R.color.holo_red_dark))
        }
    }

    override fun onStart() {
        super.onStart()
        bindService(
            Intent(this, MusicService::class.java),
            musicServiceConnection,
            BIND_AUTO_CREATE
        )
    }

    override fun onStop() {
        super.onStop()
        if (!bound) return
        unbindService(musicServiceConnection)
    }
}
