package ru.kamaz.foreground_service

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import ru.kamaz.foreground_service.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var mPlayerService: ForegroundMusicService
    private var isBound = false
    private lateinit var mBindingIntent: Intent
    private var serviceStarted = false
    private lateinit var viewModel: MainActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        doBindService()
        with(ActivityMainBinding.inflate(layoutInflater)) {
            viewModel = ViewModelProviders.of(this@MainActivity)[MainActivityViewModel::class.java]
            viewModel.actualText.observe(this@MainActivity) {
                tv.text = it
            }
            tv.setOnClickListener {
                serviceStarted = true
                viewModel.actualText.value = if (mPlayerService.mForeground) {
                    startService(mBindingIntent.apply { putExtra("startForeground", false) })
                    getString(R.string.show_notification)
                } else {
                    startService(mBindingIntent.apply { putExtra("startForeground", true) })
                    getString(R.string.hide_notification)
                }
            }
            setContentView(root)
        }
    }

    private fun doBindService() {
        mBindingIntent = Intent(this, ForegroundMusicService::class.java).also {// apply?
            bindService(it, connection, Context.BIND_AUTO_CREATE)
        }
    }

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName, service: IBinder) {
            // get bound service and instantiate MediaPlayerHolder
            val binder = service as ForegroundMusicService.LocalBinder
            mPlayerService = binder.getService()
            viewModel.actualText.postValue(
                getString(
                    if (mPlayerService.mForeground) R.string.hide_notification else R.string.show_notification
                )
            )
            isBound = true
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            isBound = false
            viewModel.actualText.postValue("Disconnected")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (serviceStarted && !mPlayerService.mForeground) stopService(mBindingIntent)
        if (isBound) unbindService(connection)
    }
}