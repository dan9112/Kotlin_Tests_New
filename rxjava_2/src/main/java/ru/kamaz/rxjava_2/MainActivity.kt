package ru.kamaz.rxjava_2

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable
import ru.kamaz.rxjava_2.databinding.ActivityMainBinding

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
        // Create an Observable
        val myObservable = observable
        // Create an Observer
        val myObserver = observer

        // Subscribe myObserver to myObservable
        myObservable.subscribe(myObserver)
    }

    private val observer
        get() = object : Observer<String> {
            override fun onSubscribe(d: Disposable) {}

            // Every time onNext is called, print the value to Android Studio’s Logcat
            override fun onNext(t: String) {
                Log.d(logTag, "onNext: $t")
            }

            // Called if an exception is thrown
            override fun onError(e: Throwable) {
                Log.d(logTag, "onError: " + e.message)
            }

            // When onComplete is called, print the following to Logcat
            override fun onComplete() {
                Log.d(logTag, "onComplete")
            }
        }

    // Give myObservable some data to emit
    private val observable
        get() = Observable.just("1", "2", "3", "4", "5")
}
