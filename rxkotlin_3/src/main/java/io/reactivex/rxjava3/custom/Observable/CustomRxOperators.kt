package io.reactivex.rxjava3.custom

import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.observers.DisposableObserver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

/**
 * Returns an Observable that emits parent's emissions with an interval between them
 * @param interval delay in milliseconds between emissions
 */
fun <T : Any> Observable<T>.addIntervals(interval: Long) = lift { observer ->
    object : DisposableObserver<T>() {
        /** Actual job with items emission */
        private lateinit var job: Job

        /** Items that should be emitted */
        private val queue = LinkedList<T>()

        /** Next emissions necessary flag */
        private var nextIsNecessary = true

        /** Scope for coroutines */
        private val scope = CoroutineScope(IO)

        override fun onNext(item: T) {
            if (nextIsNecessary) {
                queue.add(item)
                if (!::job.isInitialized || job.isCompleted) job = scope.launch { startEmit() }
            }
        }

        /** Starts emitting items */
        private suspend fun startEmit() {
            while (queue.isNotEmpty()) {
                observer.onNext(queue.removeFirst())
                if (nextIsNecessary || queue.isNotEmpty()) delay(interval)
            }
        }

        override fun onError(throwable: Throwable) = finishStream(throwable)

        override fun onComplete() = finishStream()

        /**
         * It marks to ignore next emissions and emits after the last emitted without interval
         * onComplete() if [throwable] = null and onError([throwable]) otherwise
         */
        private fun finishStream(throwable: Throwable? = null) {
            nextIsNecessary = false
            scope.launch {
                job.join()
                with(observer) {
                    if (throwable != null) onError(throwable)
                    else onComplete()
                }
            }
        }
    }
}
