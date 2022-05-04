package ru.kamaz.service_bind_and_start.presentation.services.my_service

import android.app.Service
import android.content.Intent
import android.os.Binder
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.kamaz.service_bind_and_start.domain.usecases.services.GetRandomBooleanUseCase
import ru.kamaz.service_bind_and_start.presentation.services.my_service.MyService.Companion.Command
import timber.log.Timber
import java.io.Serializable

/**
 * Сервис в коде останавливать только через команду [startService] с вложенным параметром [Start][Command.Start]
 * через [putExtra][Intent.putExtra]!
 */
class MyService : Service() {
    private var binder: MyBinder = MyBinder()

    private val getRandomBooleanUseCase = GetRandomBooleanUseCase()

    val result = MutableLiveData<Boolean?>(null)

    /**
     * Если null, сервис не запущен
     */
    private var previousStartId: Int? = null

    override fun onBind(intent: Intent) = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        previousStartId =
            when (intent?.getSerializableExtra(commandKey) as? Command ?: Command.Start) {
                Command.Start -> {
                    previousStartId?.let { stopSelf(it) }
                    Timber.d(message = if (previousStartId != null) "Service already has been started" else "Service is started")
                    startId
                }
                Command.Stop -> {
                    stopSelf()
                    Timber.d(message = if (previousStartId != null) "Service is stopped" else "Service has not been started")
                    null
                }
            }
        return START_REDELIVER_INTENT
    }

    override fun onUnbind(intent: Intent?) = false

    /**
     * Флаг задачи. Возвращает true, если задача находится в процессе выполнения.
     */
    val isInProcess: LiveData<Boolean>
        get() = getRandomBooleanUseCase.isInProcess

    /**
     * Запуск задачи. Вернёт true, если задача была запущена, и false, если нет.
     */
    fun doAnythingAndGetResult() = if (getRandomBooleanUseCase.isInProcess.value == false) {
        CoroutineScope(Dispatchers.Default).launch {
            result.postValue(getRandomBooleanUseCase.getWithTimer(delay = 10))
        }
        true
    } else false

    override fun onDestroy() {
        super.onDestroy()
        getRandomBooleanUseCase.cancel()
        Timber.d(message = "Service is destroyed")
    }

    inner class MyBinder : Binder() {
        val service: MyService
            get() = this@MyService
    }

    companion object {
        /**
         * Ключ для передачи команды сервису через аргументы [Intent] для [startService]
         */
        const val commandKey = "commandKey"

        enum class Command : Serializable {
            /**
             * Команда запуска сервиса
             */
            Start,

            /**
             * Команда остановки сервиса
             */
            Stop
        }
    }
}
