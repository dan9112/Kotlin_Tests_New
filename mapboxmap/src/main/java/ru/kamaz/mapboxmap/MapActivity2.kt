package ru.kamaz.mapboxmap

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.lifecycle.Observer
import com.mapbox.android.gestures.MoveGestureDetector
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.geojson.Point
import com.mapbox.maps.*
import com.mapbox.maps.extension.style.expressions.dsl.generated.interpolate
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotation
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.gestures.OnMapClickListener
import com.mapbox.maps.plugin.gestures.addOnMapClickListener
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.gestures.removeOnMapClickListener
import com.mapbox.maps.plugin.locationcomponent.*
import com.mapbox.navigation.base.route.RouterOrigin
import com.mapbox.navigation.core.MapboxNavigationProvider
import com.mapbox.navigation.core.directions.session.RoutesObserver
import com.mapbox.navigation.ui.maps.location.NavigationLocationProvider
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineApi
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineView
import com.mapbox.navigation.ui.maps.route.line.model.RouteLine
import ru.kamaz.mapboxmap.MapUtils.setStateEnable
import ru.kamaz.mapboxmap.MapViewModel.ModelStates
import ru.kamaz.mapboxmap.databinding.ActivityMapBinding
import ru.kamaz.mapboxmap.default_objects.DefaultLocationConsumer
import ru.kamaz.mapboxmap.default_objects.DefaultOnMoveListener
import ru.kamaz.mapboxmap.default_objects.DefaultRouterCallback

@MapboxExperimental
class MapActivity2 : AppCompatActivity() {
    private val viewModel by viewModels<MapViewModel>()
    private var _binding: ActivityMapBinding? = null
    private val binding: ActivityMapBinding
        get() = _binding!!
    private val defaultOnMapClickListener = OnMapClickListener { point ->
        updateAnnotationOnMap(point)
        viewModel.routeState.run {
            if (value == true) postValue(false)
        }
        false
    }

    private lateinit var defaultLocationProvider: LocationProvider

    private val defaultLocationConsumer = DefaultLocationConsumer()

    private val navigationLocationProvider = NavigationLocationProvider()

    private fun defaultProvider() = defaultLocationProvider.apply {
        registerLocationConsumer(defaultLocationConsumer)
    }

    private val routeOptions
        get() = MapUtils.getDefaultRouteOptions(this)
            // в качестве начальной точки маршрута используем точку местонахождения
            // пользователя, а конечной - устройства (в данном примере его координаты статичны)
            .coordinatesList(
                listOf(
                    defaultLocationConsumer.currentLocation.value,
                    deviceAnnotation.point
                )
            )
            .build()

    private val onIndicatorPositionChangedListener = OnIndicatorPositionChangedListener {
        binding.mapView.run {
            getMapboxMap().run {
                setCamera(CameraOptions.Builder().center(it).build())
                gestures.focalPoint = pixelForCoordinate(it)
            }
        }
    }

    private val mapboxNavigation by lazy {
        MapboxNavigationProvider.run {
            if (isCreated()) retrieve() else create(MapUtils.getDefaultNavigationOptions(this@MapActivity2))
        }
    }

    private val defaultOnMoveListener = object : DefaultOnMoveListener() {
        override fun onMove(detector: MoveGestureDetector) = run {
            viewModel.cameraState_VM.postValue(null)
            false
        }
    }

    private val defaultConsumerCurrentLocationObserver = Observer<Point?> { point ->
        if (point != null && viewModel.modelState.value == ModelStates.Base) viewModel.modelState.value =
            ModelStates.UserIsTracked
    }

    private val onIndicatorBearingChangedListener = OnIndicatorBearingChangedListener {
        binding.mapView.getMapboxMap().setCamera(CameraOptions.Builder().bearing(it).build())
    }

    private val defaultRouteStateObserver = Observer<Boolean?> { state ->
        if (state == true) getRoutes() else if (state == null) resetRoutes()
    }

    private fun getRoutes() = mapboxNavigation.run {
        if (defaultLocationConsumer.currentLocation.value != null) requestRoutes(
            routeOptions,
            object : DefaultRouterCallback() {
                override fun onRoutesReady(
                    routes: List<DirectionsRoute>,
                    routerOrigin: RouterOrigin
                ) {
                    registerRoutesObserver(routesObserver)
                    setRoutes(routes)
                }
            }) else {// При нормальном поведении икогда не должен срабатывать!
            Log.e(
                "MapActivity",
                "Не удалось определить местоположения. Убедитесь, что GPS-модуль активен!"
            )
        }
    }

    private val defaultCameraStateObserver = Observer<Boolean?> { state ->
        binding.run {
            mapView.run {
                location.run {
                    when (state) {
                        null -> {
                            removeOnIndicatorPositionChangedListener(
                                onIndicatorPositionChangedListener
                            )
                            removeOnIndicatorBearingChangedListener(
                                onIndicatorBearingChangedListener
                            )
                        }
                        false -> {
                            defaultLocationConsumer.currentLocation.value?.let {
                                getMapboxMap().setCamera(
                                    CameraOptions.Builder().center(it).build()
                                )
                            }
                            removeOnIndicatorBearingChangedListener(
                                onIndicatorBearingChangedListener
                            )
                            addOnIndicatorPositionChangedListener(
                                onIndicatorPositionChangedListener
                            )
                        }
                        true -> {
                            defaultLocationConsumer.currentBearing.value?.let {
                                getMapboxMap().setCamera(
                                    CameraOptions.Builder().bearing(it).build()
                                )
                            }
                            addOnIndicatorBearingChangedListener(
                                onIndicatorBearingChangedListener
                            )
                        }
                    }
                }
            }
            getPosition.setImageResource(
                when (state) {
                    null -> R.drawable.track_user
                    true -> R.drawable.track_user_1
                    false -> R.drawable.track_user_2
                }
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMapBinding.inflate(layoutInflater).apply {
            setContentView(root)
            defaultLocationProvider = DefaultLocationProvider(this@MapActivity2)

            registerReceiver(defaultGpsReceiver, IntentFilter().apply {
                addAction(LocationManager.PROVIDERS_CHANGED_ACTION)
            })
            onMapReady()
            initNavResources()
            buttonsSetup()

            viewModel.run {
                modelState.run {
                    mapView.location.setLocationProvider(
                        if (value == ModelStates.RouteProgressIsTracked) navigationLocationProvider
                        else defaultProvider()
                    )
                    gpsState.run {
                        value =
                            (getSystemService(LOCATION_SERVICE) as LocationManager).isLocationEnabled
                        observe(this@MapActivity2) { state ->
                            mapView.location.updateSettings { enabled = state }
                            when (viewModel.modelState.value) {
                                ModelStates.UserIsTracked -> if (!state) viewModel.modelState.postValue(
                                    ModelStates.Base
                                )
                                ModelStates.RouteBuilt -> if (!state) viewModel.modelState.postValue(
                                    ModelStates.RouteBuiltUserNotTracked
                                )
                                ModelStates.RouteBuiltUserNotTracked -> if (state) viewModel.modelState.postValue(
                                    ModelStates.RouteBuilt
                                )
                                else -> Log.e(
                                    "MapActivity2",
                                    "Недопустимый статус модели во время изменения статуса GPS-модуля"
                                )
                            }
                        }
                    }
                    observe(this@MapActivity2) { state ->
                        state.let {
                            mapView.getMapboxMap().run {
                                if (it == ModelStates.RouteProgressIsTracked || it == null) {
                                    removeOnMapClickListener(defaultOnMapClickListener)
                                    defaultLocationConsumer.currentLocation.removeObserver(
                                        defaultConsumerCurrentLocationObserver
                                    )
                                    cameraState_VM.removeObserver(defaultCameraStateObserver)
                                    routeState.removeObserver(defaultRouteStateObserver)
                                } else {
                                    addOnMapClickListener(defaultOnMapClickListener)
                                    defaultLocationConsumer.currentLocation.observe(
                                        this@MapActivity2,
                                        defaultConsumerCurrentLocationObserver
                                    )
                                    cameraState_VM.observe(
                                        this@MapActivity2,
                                        defaultCameraStateObserver
                                    )
                                    routeState.observe(this@MapActivity2, defaultRouteStateObserver)
                                }
                            }
                        }
                        when (state) {
                            null -> {
                                mapView.gestures.removeOnMoveListener(defaultOnMoveListener)
                                listOf(
                                    getPosition,
                                    getRoute,
                                    mapView,
                                    tripProgressView,
                                    startTrip
                                ).setStateEnable(false)
                                routeState.postValue(null)
                                cameraState_VM.postValue(null)
                            }
                            ModelStates.Base -> {
                                mapView.gestures.removeOnMoveListener(defaultOnMoveListener)
                                listOf(mapView).setStateEnable(true)
                                listOf(
                                    getPosition,
                                    getRoute,
                                    tripProgressView,
                                    startTrip
                                ).setStateEnable(false)
                                routeState.postValue(null)
                            }
                            ModelStates.UserIsTracked -> {
                                mapView.gestures.addOnMoveListener(defaultOnMoveListener)
                                listOf(getPosition, getRoute, mapView).setStateEnable(true)
                                listOf(tripProgressView, startTrip).setStateEnable(false)
                                routeState.postValue(null)
                            }
                            ModelStates.RouteBuilt -> {
                                mapView.gestures.addOnMoveListener(defaultOnMoveListener)
                                listOf(getPosition, getRoute, mapView, startTrip).setStateEnable(
                                    true
                                )
                                listOf(tripProgressView).setStateEnable(false)
                                routeState.postValue(true)
                            }
                            ModelStates.RouteBuiltUserNotTracked -> {
                                mapView.gestures.removeOnMoveListener(defaultOnMoveListener)
                                listOf(getRoute, mapView).setStateEnable(true)
                                listOf(getPosition, tripProgressView, startTrip).setStateEnable(
                                    false
                                )
                                routeState.postValue(false)
                            }
                            ModelStates.RouteProgressIsTracked -> {
                                mapView.gestures.removeOnMoveListener(defaultOnMoveListener)
                                listOf(startTrip, mapView).setStateEnable(true)
                                listOf(
                                    getPosition,
                                    tripProgressView,
                                    getRoute
                                ).setStateEnable(false)
                            }
                        }
                    }
                    value = ModelStates.Base
                }
            }
        }
    }

    private fun ActivityMapBinding.buttonsSetup() {
        getPosition.setOnClickListener {
            viewModel.cameraState_VM.run {
                value = when (value) {
                    null -> false
                    true -> false
                    false -> true
                }
            }
        }
        getRoute.run {
            viewModel.run {
                modelState.run {
                    setOnClickListener {
                        when (value) {
                            ModelStates.RouteBuiltUserNotTracked -> value =
                                ModelStates.Base
                            ModelStates.UserIsTracked -> value = ModelStates.RouteBuilt
                            ModelStates.RouteBuilt -> if (routeState.value == false) routeState.value =
                                true
                            else if (routeState.value == true) value =
                                ModelStates.UserIsTracked
                            else -> Log.e(
                                "MapActivity2",
                                "Ошибка в доступности кнопки \"getRoute\""
                            )
                        }
                    }
                    setOnLongClickListener {
                        value = when (value) {
                            ModelStates.RouteBuilt -> ModelStates.UserIsTracked
                            ModelStates.RouteBuiltUserNotTracked -> ModelStates.Base
                            else -> return@setOnLongClickListener false
                        }
                        true
                    }
                }
            }
        }
    }

    private lateinit var routeLineApi: MapboxRouteLineApi

    private lateinit var routeLineView: MapboxRouteLineView

    private fun initNavResources() = MapUtils.getDefaultRouteLineOptions(this).run {
        routeLineApi = MapboxRouteLineApi(this)
        routeLineView = MapboxRouteLineView(this)
    }

    private val routesObserver = RoutesObserver { routeUpdateResult ->
        if (routeUpdateResult.routes.isNotEmpty()) {
            val routeLines = routeUpdateResult.routes.map { RouteLine(it, null) }

            routeLineApi.setRoutes(routeLines) { value ->
                binding.mapView.getMapboxMap().getStyle()?.run {
                    routeLineView.renderRouteDrawData(this, value)
                }
            }
        } else {
            val style = binding.mapView.getMapboxMap().getStyle()
            if (style != null) {
                routeLineApi.clearRouteLine { value ->
                    routeLineView.renderClearRouteLineValue(style, value)
                }
            }
        }
    }

    private fun resetRoutes() = mapboxNavigation.run {
        unregisterRoutesObserver(routesObserver)
        routeLineApi.clearRouteLine { value ->
            binding.mapView.getMapboxMap().getStyle()?.let { style ->
                routeLineView.renderClearRouteLineValue(style, value)
            }
        }
        setRoutes(emptyList())
    }

    private lateinit var pointAnnotationManager: PointAnnotationManager

    private lateinit var deviceAnnotation: PointAnnotation

    private fun ActivityMapBinding.onMapReady() {
        mapView.run {
            pointAnnotationManager = annotations.createPointAnnotationManager()
            getMapboxMap().run {
                setRenderCacheOptions(RenderCacheOptions.Builder().setDisabled().build())
                loadStyleUri(Style.MAPBOX_STREETS) {
                    location.run {
                        updateSettings {
                            locationPuck = LocationPuck2D(
                                bearingImage = AppCompatResources.getDrawable(
                                    this@MapActivity2,
                                    R.drawable.mapbox_user_puck_icon,
                                ),
                                shadowImage = AppCompatResources.getDrawable(
                                    this@MapActivity2,
                                    R.drawable.mapbox_user_icon_shadow,
                                ),
                                scaleExpression = interpolate {
                                    linear()
                                    zoom()
                                    stop {
                                        literal(0.0)
                                        literal(0.6)
                                    }
                                    stop {
                                        literal(20.0)
                                        literal(1.0)
                                    }
                                }.toJson()
                            )
                        }
                    }
                    if (viewModel.firstStart) {
                        setCamera(
                            CameraOptions.Builder()
                                .zoom(14.0)
                                .build()
                        )
                        addAnnotationToMap(Point.fromLngLat(52.43554891161082, 55.726641811420166))
                    }
                }
            }
        }
    }

    private fun addAnnotationToMap(point: Point) {
        bitmapFromDrawableRes(this, R.drawable.marker_blood_red)?.let {
            val currentAnnotationOptions = PointAnnotationOptions()
                .withPoint(point)
                .withDraggable(true)
                .withIconImage(it)
            deviceAnnotation = pointAnnotationManager.create(currentAnnotationOptions)
        }
    }

    private fun updateAnnotationOnMap(newPoint: Point) =
        pointAnnotationManager.update(deviceAnnotation.apply { point = newPoint })

    private fun bitmapFromDrawableRes(context: Context, @DrawableRes resourceId: Int) =
        convertDrawableToBitmap(AppCompatResources.getDrawable(context, resourceId))

    private fun convertDrawableToBitmap(sourceDrawable: Drawable?): Bitmap? {
        if (sourceDrawable == null) return null
        else {
            if (sourceDrawable is BitmapDrawable) return sourceDrawable.bitmap
            else {
                val constantState = sourceDrawable.constantState ?: return null
                constantState.newDrawable().mutate().run {
                    return Bitmap.createBitmap(
                        intrinsicWidth,
                        intrinsicHeight,
                        Bitmap.Config.ARGB_8888
                    ).apply {
                        Canvas(this).run {
                            setBounds(0, 0, width, height)
                            draw(this)
                        }
                    }
                }
            }
        }
    }

    private fun MapView.onCameraTrackingDismissed() {
        Toast.makeText(this@MapActivity2, "onCameraTrackingDismissed", Toast.LENGTH_SHORT).show()
        location.run {
            removeOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
            removeOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener)
        }
        gestures.removeOnMoveListener(defaultOnMoveListener)
    }

    private val defaultGpsReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            viewModel.gpsState.postValue((getSystemService(LOCATION_SERVICE) as LocationManager).isLocationEnabled)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.mapView.run {
            location.run {
                getLocationProvider()?.let {
                    if (it is DefaultLocationProvider)
                        it.unRegisterLocationConsumer(defaultLocationConsumer)
                }
                updateSettings { enabled = false }
            }
            onCameraTrackingDismissed()
            gestures.run {
                removeOnMoveListener(defaultOnMoveListener)
                removeOnMapClickListener(defaultOnMapClickListener)
            }
        }
        viewModel.firstStart = false// for fragment's onDestroyView
        unregisterReceiver(defaultGpsReceiver)
        mapboxNavigation.unregisterRoutesObserver(routesObserver)
    }
}
