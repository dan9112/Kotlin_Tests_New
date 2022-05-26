package ru.kamaz.service_bind_and_start.presentation.ui.fragments.blank

import android.content.Context
import android.content.Intent
import androidx.databinding.ObservableField
import ru.kamaz.service_bind_and_start.R
import ru.kamaz.service_bind_and_start.presentation.services.my_service.MyService
import ru.kamaz.service_bind_and_start.presentation.services.my_service.MyService.Companion.Command.Start
import ru.kamaz.service_bind_and_start.presentation.services.my_service.MyService.Companion.Command.Stop
import ru.kamaz.service_bind_and_start.presentation.services.my_service.MyService.Companion.commandKey
import timber.log.Timber

/**
 * Адаптер привязки данных для BlankFragment
 */
class BlankFragmentBindingAdapter(
    /**
     * Актуальный контекст для использования функций ОС Android
     */
    private val context: Context,
    /**
     * viewModel фрагмента
     */
    private val viewModel: BlankFragmentAndroidViewModel
) {
    var service: MyService? = null

    /**
     * Запуск сервиса
     */
    fun startService() {
        with(receiver = context) {
            startService(Intent(this, MyService::class.java).apply {
                putExtra(commandKey, Start)
            })
            Timber.d(message = "Tries to start service with context: $this")
        }
    }

    /**
     * Остановка сервиса
     */
    val stopService = {
        with(receiver = context) {
            startService(Intent(this, MyService::class.java).apply {
                putExtra(commandKey, Stop)
            })
            Timber.d(message = "Tries to stop service with context: $this")
        }
    }

    fun deleteResult() {
        viewModel.deleteResult()
        updateTvAndDrButton.invoke(null)
    }

    fun createNew() {
        service?.doAnythingAndGetResult()
    }

    val updateTvAndDrButton: (result: Boolean?) -> Unit
        get() = { result ->
            if (result == null) {
                isDeleteResultButtonEnable.set(false)
                text.set(context.getString(R.string.error_get_result_string))
            } else {
                isDeleteResultButtonEnable.set(true)
                text.set(result.toString())
            }
        }

    /**
     * Текст в текстовом окне
     */
    val text = ObservableField<String>()

    /**
     * Флаг доступности для пользователя кнопки создания нового результата
     */
    val isCreateNewButtonEnable = ObservableField(false)

    /**
     * Флаг достуупности для пользователя кнопки удаления предыдущего сохранённого результата
     */
    val isDeleteResultButtonEnable = ObservableField(false)
}
