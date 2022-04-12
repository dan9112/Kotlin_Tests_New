package ru.kamaz.foreground_service

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainActivityViewModel: ViewModel() {
    val actualText = MutableLiveData("Unconnected")
}