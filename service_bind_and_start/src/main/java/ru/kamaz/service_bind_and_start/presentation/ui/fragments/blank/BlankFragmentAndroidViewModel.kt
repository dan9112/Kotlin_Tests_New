package ru.kamaz.service_bind_and_start.presentation.ui.fragments.blank

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import ru.kamaz.service_bind_and_start.data.repository.MyRepositoryImpl
import ru.kamaz.service_bind_and_start.domain.usecases.ui.blank.DeleteBooleanUseCase.deleteBoolean
import ru.kamaz.service_bind_and_start.domain.usecases.ui.blank.GetBooleanUseCase.getBoolean
import ru.kamaz.service_bind_and_start.domain.usecases.ui.blank.SaveBooleanUseCase.saveBoolean

class BlankFragmentAndroidViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = MyRepositoryImpl(application.applicationContext)

    fun saveResult(result: Boolean) = repository.saveBoolean(result = result)

    fun getResult() = repository.getBoolean()

    fun deleteResult() = repository.deleteBoolean()
}
