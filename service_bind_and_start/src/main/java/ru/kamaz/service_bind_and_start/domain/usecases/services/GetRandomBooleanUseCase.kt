package ru.kamaz.service_bind_and_start.domain.usecases.services

import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.*
import timber.log.Timber
import kotlin.random.Random

class GetRandomBooleanUseCase {
    val isInProcess = MutableLiveData(false)
    private var deferred: Deferred<Boolean>? = null

    /**
     * Вернёт null в случае отмены вычислений
     */
    fun getWithTimer(delay: Int): Boolean? = runBlocking {
        if (deferred == null) {
            isInProcess.postValue(true)
            deferred = CoroutineScope(Dispatchers.Default).async {
                var sec = 0
                while (sec < delay) {
                    sec++
                    delay(timeMillis = 1000)
                    Timber.d(message = "$sec сек.")
                }
                Random.nextBoolean()
            }
        }
        return@runBlocking try {
            deferred!!.await()
        } catch (exception: CancellationException) {
            null
        } finally {
            isInProcess.postValue(false)
            deferred = null
        }
    }

    fun cancel() {
        deferred?.let {
            it.cancel()
            deferred = null
        }
    }
}
