package com.example.coroutines

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.example.coroutines.databinding.ActivityMainBinding
import kotlinx.coroutines.*
import timber.log.Timber

@DelicateCoroutinesApi
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)
            .apply {
                button.setOnClickListener {
                    Timber.d("Start")
                    CoroutineScope(Dispatchers.Main).launch {
                        delay(1000L)
                        Timber.d("Kitty")
                        Timber.d("Thread from launch: ${Thread.currentThread().name}")
                    }

                    Timber.d("Hello")
                    Thread.sleep(5000L)
                    Timber.d("Stop")
                    Timber.d("Thread from onCreate: ${Thread.currentThread().name}")
                }
            }
    }
}