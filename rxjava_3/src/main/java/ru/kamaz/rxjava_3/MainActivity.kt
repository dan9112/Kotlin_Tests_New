package ru.kamaz.rxjava_3

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil.setContentView
import com.jakewharton.rxbinding.view.RxView.clicks
import io.reactivex.rxjava3.core.Observable.*
import ru.kamaz.rxjava_3.databinding.ActivityMainBinding

private const val logTag = "MainActivity"

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        with(receiver = setContentView<ActivityMainBinding>(this, R.layout.activity_main)) {
            // Start the stream when the button is clicked
            clicks(button).subscribe { startRStream() }
        }
    }

    private fun startRStream() {
        val numbers = range(1, 5)
        val strings = just("One", "Two", "Three", "Four", "Five")
        val zipper = zip(strings, numbers) { s, n -> "$s $n" }
        zipper.subscribe { Log.d(logTag, it) }
    }
}
