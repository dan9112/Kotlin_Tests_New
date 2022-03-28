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
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.lifecycle.Observer
import com.mapbox.android.gestures.MoveGestureDetector
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.geojson.Point
import com.mapbox.maps.*
import com.mapbox.maps.Style.Companion.MAPBOX_STREETS
import com.mapbox.maps.extension.style.expressions.dsl.generated.interpolate
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotation
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.gestures.*
import com.mapbox.maps.plugin.locationcomponent.*
import com.mapbox.navigation.base.route.RouterOrigin
import com.mapbox.navigation.core.MapboxNavigationProvider
import com.mapbox.navigation.core.directions.session.RoutesObserver
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineApi
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineView
import com.mapbox.navigation.ui.maps.route.line.model.RouteLine
import ru.kamaz.mapboxmap.MapUtils.getDefaultNavigationOptions
import ru.kamaz.mapboxmap.MapUtils.getDefaultRouteLineOptions
import ru.kamaz.mapboxmap.MapUtils.setStateEnable
import ru.kamaz.mapboxmap.databinding.ActivityMapBinding
import ru.kamaz.mapboxmap.default_objects.DefaultLocationConsumer
import ru.kamaz.mapboxmap.default_objects.DefaultOnMoveListener
import ru.kamaz.mapboxmap.default_objects.DefaultRouterCallback

@MapboxExperimental
class MapActivity : AppCompatActivity() {
    private val viewModel by viewModels<MapViewModel>()

    private val defaultLocationConsumer = DefaultLocationConsumer()

    private val onIndicatorBearingChangedListener = OnIndicatorBearingChangedListener {
        binding.mapView.getMapboxMap().setCamera(CameraOptions.Builder().bearing(it).build())
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

    private val gpsReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            viewModel.gpsState.postValue((getSystemService(LOCATION_SERVICE) as LocationManager).isLocationEnabled)
        }
    }

    private val onMoveListener = object : DefaultOnMoveListener() {
        override fun onMoveBegin(detector: MoveGestureDetector) {
            viewModel.cameraState.value = null
        }
    }

    private val defaultConsumerCurrentLocationObserver = Observer<Point?> { point ->
        viewModel.routeState.value = false
        binding.run {
            listOf(getPosition, getRoute).setStateEnable(point != null)
        }
    }

    private val mapboxNavigation by lazy {
        MapboxNavigationProvider.run {
            if (isCreated()) retrieve() else create(getDefaultNavigationOptions(this@MapActivity))
        }
    }

    private val onMapClickListener = OnMapClickListener { point ->
        updateAnnotationOnMap(point)
        viewModel.routeState.value = false
        false
    }

    private val routesObserver = RoutesObserver { routeUpdateResult ->
        if (routeUpdateResult.routes.isNotEmpty()) {
            // RouteLine: оберните объекты Direction Route и передайте их в Mapbox Routing Api, чтобы
            // сгенерировать данные, необходимые для рисования маршрута(ов) на карте
            val routeLines = routeUpdateResult.routes.map { RouteLine(it, null) }

            routeLineApi.setRoutes(routeLines) { value ->
                // RouteLine: MapboxRouteLineView ожидает ненулевой ссылки на стиль карты. Данные,
                // сгенерированные вызовом MapboxRouteLineApi выше, должны быть отображены
                // MapboxRouteLineView, чтобы визуализировать изменения на карте.
                binding.mapView.getMapboxMap().getStyle()?.apply {
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

    private val GesturesPlugin.setupListeners: () -> Unit
        get() = {
            addOnMoveListener(onMoveListener)
            addOnMapClickListener(onMapClickListener)
        }

    private val LocationComponentPlugin.initComponent: () -> Unit
        get() = {
            updateSettings { enabled = true }
            setLocationProvider(defaultProvider())
        }

    private var routesObserverRegistered = false

    private lateinit var pointAnnotationManager: PointAnnotationManager

    private lateinit var deviceAnnotation: PointAnnotation

    private lateinit var binding: ActivityMapBinding

    private lateinit var routeLineApi: MapboxRouteLineApi

    private lateinit var routeLineView: MapboxRouteLineView

    private lateinit var defaultLocationProvider: LocationProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapBinding.inflate(layoutInflater).apply {
            setContentView(root)
            defaultLocationProvider = DefaultLocationProvider(this@MapActivity)

            workWithMap()
            onMapReady()
            buttonsSetup()
            initNavResources()

            registerReceiver(gpsReceiver, IntentFilter().apply {
                addAction(LocationManager.PROVIDERS_CHANGED_ACTION)
            })
            defaultLocationConsumer.currentLocation.observe(
                this@MapActivity,
                defaultConsumerCurrentLocationObserver
            )
            viewModel.run {
                gpsState.run {
                    value =
                        (getSystemService(LOCATION_SERVICE) as LocationManager).isLocationEnabled
                    observe(this@MapActivity) { state ->
                        mapView.run {
                            if (state) {
                                gestures.setupListeners()
                                location.initComponent()
                            } else {
                                resetLocationComponentAndGesturesListener()
                                defaultLocationConsumer.run {
                                    currentLocation.value = null
                                    currentBearing.value = null
                                }
                                viewModel.cameraState.value = null
                            }
                        }
                    }
                }
                cameraState.observe(this@MapActivity) { state ->
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
                routeState.observe(this@MapActivity) { state ->
                    if (state == true) getRoutes() else if (state == null) resetRoutes()
                }
            }
        }
    }

    private fun initNavResources() {
        val options = getDefaultRouteLineOptions(this)

        routeLineApi = MapboxRouteLineApi(options)
        routeLineView = MapboxRouteLineView(options)
    }

    private fun defaultProvider() = defaultLocationProvider.apply {
        registerLocationConsumer(defaultLocationConsumer)
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

    private fun resetRoutes() = mapboxNavigation.run {
        unregisterRoutesObserver(routesObserver)
        routeLineApi.clearRouteLine { value ->
            binding.mapView.getMapboxMap().getStyle()?.let { style ->
                routeLineView.renderClearRouteLineValue(style, value)
            }
        }
        setRoutes(emptyList())
    }

    private fun ActivityMapBinding.buttonsSetup() {
        getPosition.setOnClickListener {
            viewModel.cameraState.run {
                value = when (value) {
                    null -> false
                    true -> false
                    false -> true
                }
            }
        }
        getRoute.run {
            viewModel.routeState.run {
                setOnClickListener {
                    value = when (value) {
                        true -> null
                        null -> true
                        false -> true
                    }
                }
                setOnLongClickListener {
                    if (value == null) false
                    else {
                        resetRoutes()
                        true
                    }
                }
            }
        }
    }

    private fun MapView.resetLocationComponentAndGesturesListener() {
        location.run {
            getLocationProvider()?.unRegisterLocationConsumer(defaultLocationConsumer)
            updateSettings { enabled = false }
        }
        onCameraTrackingDismissed()
    }

    private fun ActivityMapBinding.workWithMap() {
        mapView.isEnabled = true
        getPosition.isEnabled = true
        mapView.visibility = VISIBLE
        getPosition.visibility = VISIBLE
    }

    private fun ActivityMapBinding.onMapReady() {
        mapView.run {
            pointAnnotationManager = annotations.createPointAnnotationManager()
            getMapboxMap().run {
                setCamera(
                    CameraOptions.Builder()
                        .zoom(14.0)
                        .build()
                )
                setRenderCacheOptions(RenderCacheOptions.Builder().setDisabled().build())
                addOnMapClickListener(onMapClickListener)
                loadStyleUri(MAPBOX_STREETS) {
                    location.run {
                        updateSettings {
                            locationPuck = LocationPuck2D(
                                bearingImage = getDrawable(
                                    this@MapActivity,
                                    R.drawable.mapbox_user_puck_icon,
                                ),
                                shadowImage = getDrawable(
                                    this@MapActivity,
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
                    addAnnotationToMap(Point.fromLngLat(52.43554891161082, 55.726641811420166))
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
        convertDrawableToBitmap(getDrawable(context, resourceId))

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
        Toast.makeText(this@MapActivity, "onCameraTrackingDismissed", Toast.LENGTH_SHORT).show()
        location.run {
            removeOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
            removeOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener)
        }
        gestures.removeOnMoveListener(onMoveListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.mapView.run {
            resetLocationComponentAndGesturesListener()
            getMapboxMap().removeOnMapClickListener(onMapClickListener)
        }
        mapboxNavigation.run {
            if (routesObserverRegistered) unregisterRoutesObserver(routesObserver)
        }
        unregisterReceiver(gpsReceiver)
    }
}
