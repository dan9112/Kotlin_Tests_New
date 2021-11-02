package com.example.coroutines

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.example.coroutines.databinding.ActivityMainBinding
import kotlinx.coroutines.*
import timber.log.Timber

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)
            .apply {
                button.setOnClickListener {
                    runBlocking {
                        Timber.d("Начинаем погрузку котов")
                        joinAll(
                            launch { loadCatImage(1, 500) },
                            launch { loadCatImage(2, 300) }
                        )
                        Timber.d("Операция погрузки котов завершена")
                    }
                }
            }
    }

    private suspend fun loadCatImage(number: Int, delay: Long) {
        Timber.d("Загружаем котика под номером $number")
        delay(delay)
        Timber.d("Котик под номером $number загружен")
    }
}