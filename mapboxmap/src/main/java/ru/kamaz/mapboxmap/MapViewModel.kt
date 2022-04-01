package ru.kamaz.mapboxmap

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class MapViewModel(application: Application) : AndroidViewModel(application) {
    var dialogIsShown = false
    private var _connectionLiveData: ConnectionLiveData? = ConnectionLiveData(application)
    val connectionLiveData: LiveData<Boolean>
        get() = _connectionLiveData!!

    private val _gpsState = MutableLiveData(false)
    val gpsState: LiveData<Boolean>
        get() = _gpsState

    fun setGpsStateValue(value: Boolean, postValue: Boolean = false) = _gpsState.run {
        if (postValue) postValue(value)
        else setValue(value)
    }

    private val _cameraState = MutableLiveData<Boolean?>(null)
    val cameraStateVM: LiveData<Boolean?>
        get() = _cameraState

    fun setCameraStateValue(value: Boolean?, postValue: Boolean = false) = _cameraState.run {
        if (postValue) postValue(value)
        else setValue(value)
    }

    private val _routeState = MutableLiveData<Boolean?>(null)
    val routeState: LiveData<Boolean?>
        get() = _routeState

    fun setRouteStateValue(value: Boolean?, postValue: Boolean = false) = _routeState.run {
        if (postValue) postValue(value)
        else setValue(value)
    }

    private val _modelState =
        MutableLiveData<ModelStates?>(null)// Модель находится в состоянии, когда работа с картой не разрешена
    val modelState: LiveData<ModelStates?>
        get() = _modelState

    fun setModelStateValue(value: ModelStates?, postValue: Boolean = false) = _modelState.run {
        if (postValue) postValue(value)
        else setValue(value)
    }

    enum class ModelStates {
        /**
         * Модель находится в состоянии, когда на карте нет ничего, кроме маркера
         */
        Base,

        /**
         * Модель находится в состоянии, когда меттоположение пользователя отслеживается
         */
        UserIsTracked,

        /**
         * Модель находится в состоянии, когда на карте построен маршрут
         */
        RouteBuilt,

        /**
         * Модель находится в состоянии, когда на карте построен маршрут, но нет данных о местоположении пользователя
         */
        RouteBuiltUserNotTracked,

        /**
         * Модель находится в состоянии, когда ведётся отслеживание прогресса движения по пути
         */
        RouteProgressIsTracked
    }

    // override fun onCleared() {
    //     super.onCleared()
    //     _connectionLiveData = null
    // }
}