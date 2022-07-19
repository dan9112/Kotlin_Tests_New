package ru.kamaz.bluetooth_connection

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainActivityViewModel : ViewModel() {
    private val _buttonIsEnable = MutableLiveData(true)
    val buttonIsEnable: LiveData<Boolean>
        get() = _buttonIsEnable

    fun connectionTryFinished() {
        _buttonIsEnable.postValue(true)
    }

    private val _textViewText = MutableLiveData("")
    val textViewText: LiveData<String>
        get() = _textViewText

    var text
        get() = _textViewText.value
        set(value) {
            _textViewText.postValue(value)
        }

    private val _connectTrigger = MutableLiveData(false)
    val connectTrigger: LiveData<Boolean>
        get() = _connectTrigger

    fun connectTriggerCaught() {
        _connectTrigger.postValue(false)
    }

    fun tryConnectAsClient() {
        _buttonIsEnable.value = false
        _connectTrigger.value = true
    }
}