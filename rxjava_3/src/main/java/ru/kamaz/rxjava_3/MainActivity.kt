package ru.kamaz.rxjava_3

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil.setContentView
import com.jakewharton.rxbinding.view.RxView.clicks
import io.reactivex.rxjava3.core.Observable.just
import io.reactivex.rxjava3.core.Observable.range
import io.reactivex.rxjava3.kotlin.subscribeBy
import ru.kamaz.rxjava_3.databinding.ActivityMainBinding
import timber.log.Timber

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
        val zipper = strings.zipWith(numbers) { s, n -> "$s $n" }
        zipper.subscribeBy(
            onNext = { Timber.i(it) },
            onComplete = { Timber.i("It has been completed") })
    }
}
