package ru.kamaz.mapboxmap

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MapViewModel: ViewModel() {

    val gpsState = MutableLiveData(false)

    val cameraState = MutableLiveData<Boolean?>(null)

    val routeState = MutableLiveData(false)
}