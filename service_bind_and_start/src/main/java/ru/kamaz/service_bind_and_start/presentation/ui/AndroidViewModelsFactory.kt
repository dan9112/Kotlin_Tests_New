package ru.kamaz.service_bind_and_start.presentation.ui

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ru.kamaz.service_bind_and_start.presentation.ui.fragments.blank.BlankFragmentAndroidViewModel

class AndroidViewModelsFactory(val app: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BlankFragmentAndroidViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BlankFragmentAndroidViewModel(app) as T
        }
        throw IllegalArgumentException("Unable to construct ViewModel")
    }
}
