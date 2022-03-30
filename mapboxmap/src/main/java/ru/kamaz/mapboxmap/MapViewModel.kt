package ru.kamaz.mapboxmap

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MapViewModel: ViewModel() {

    var firstStart = true

    val gpsState = MutableLiveData(false)

    val cameraState_VM = MutableLiveData<Boolean?>(null)

    val routeState = MutableLiveData<Boolean?>(null)

    val modelState = MutableLiveData<ModelStates>(null)// Модель находится в состоянии, когда работа с картой не разрешена

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
}