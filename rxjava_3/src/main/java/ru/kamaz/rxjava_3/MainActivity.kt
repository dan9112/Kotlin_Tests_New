package ru.kamaz.rxjava_3

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.kotlin.toObservable
import ru.kamaz.rxjava_3.databinding.ActivityMainBinding

private const val logTag = "MainActivity"

class MainActivity : AppCompatActivity() {
    private var _binding: ActivityMainBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding =
            DataBindingUtil.setContentView<ActivityMainBinding?>(this, R.layout.activity_main)
                .apply {
                    // Start the stream when the button is clicked
                    button.setOnClickListener { startRStream() }
                }
    }

    private fun startRStream() {
        listOf("1", "2", "3", "4", "5")
            // Apply the toObservable() extension function
            .toObservable()
            // Construct your Observer using the subscribeBy() extension function
            .subscribeBy(
                onNext = { Log.d(logTag, "onNext: $it") },
                onError = { Log.d(logTag, "onError: " + it.message) },
                onComplete = { Log.d(logTag, "onComplete") }
            )
    }
}
