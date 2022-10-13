package ru.kamaz.rxkotlin_3

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.jakewharton.rxbinding.view.RxView.clicks
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Observable.just
import io.reactivex.rxjava3.core.Observable.range
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.subscribeBy
import ru.kamaz.rxkotlin_3.databinding.ActivityMainBinding.inflate
import timber.log.Timber.Forest.i
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    private val disposable = CompositeDisposable()
    private val zipper: Observable<String>
    private var num = 0u

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        with(receiver = inflate(layoutInflater)) {
            setContentView(root)
            clicks(buttonStart).subscribe {
                startRStream(++num)
            }
            clicks(buttonDispose).subscribe {
                with(disposable) {
                    if (size() != 0) {
                        clear()
                        i("All of active subscribers have been disposed")
                    } else {
                        i("There are no active subscribers to dispose")
                    }
                }
            }
        }
    }

    private fun startRStream(num: UInt) {
        disposable.add(
            zipper.zipWith(Observable.interval(1, TimeUnit.SECONDS)) { data, _ ->
                i("$data [$num]")
                data
            }.subscribeBy(
                onNext = { i("$it [$num]") },
                onComplete = { i("It has been completed [$num]") }
            )
        )
    }

    init {
        val numbers = range(1, 5)
        val strings = just("One", "Two", "Three", "Four", "Five")
        zipper = strings.zipWith(numbers) { s, n -> "$s $n" }
    }
}
