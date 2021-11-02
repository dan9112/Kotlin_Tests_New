package com.example.coroutines

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil.setContentView
import com.example.coroutines.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import java.lang.Thread.currentThread
import java.lang.Thread.sleep

@DelicateCoroutinesApi
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.run {
            binding = setContentView<ActivityMainBinding>(this@MainActivity, R.layout.activity_main)
                .apply {
                    button.setOnClickListener {
                        d("Start")
                        CoroutineScope(Main).launch {
                            delay(1000L)
                            d("Kitty")
                            d("Thread from launch: ${currentThread().name}")
                        }

                        d("Hello")
                        sleep(5000L)
                        d("Stop")
                        d("Thread from onCreate: ${currentThread().name}")
                    }
                }
        }
    }
}
