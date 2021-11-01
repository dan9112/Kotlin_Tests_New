package com.example.alarm_manager

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel: ViewModel() {
    val musicIsInProcess = MutableLiveData<Boolean>().apply { value = false }
}