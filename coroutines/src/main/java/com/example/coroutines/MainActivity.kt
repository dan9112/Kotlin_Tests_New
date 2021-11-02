package com.example.coroutines

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.example.coroutines.databinding.ActivityMainBinding
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.runBlocking
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
                            async { loadCatImage(1, 500) },
                            async { loadCatImage(2, 300) }
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