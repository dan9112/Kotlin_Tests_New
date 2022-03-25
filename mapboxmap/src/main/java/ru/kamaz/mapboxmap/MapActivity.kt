package ru.kamaz.mapboxmap

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.mapbox.android.gestures.MoveGestureDetector
import com.mapbox.api.directions.v5.DirectionsCriteria
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.api.directions.v5.models.RouteOptions
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
import com.mapbox.navigation.base.extensions.applyDefaultNavigationOptions
import com.mapbox.navigation.base.extensions.applyLanguageAndVoiceUnitOptions
import com.mapbox.navigation.base.options.NavigationOptions
import com.mapbox.navigation.base.route.RouterCallback
import com.mapbox.navigation.base.route.RouterFailure
import com.mapbox.navigation.base.route.RouterOrigin
import com.mapbox.navigation.core.MapboxNavigationProvider
import com.mapbox.navigation.core.directions.session.RoutesObserver
import com.mapbox.navigation.core.trip.session.LocationMatcherResult
import com.mapbox.navigation.core.trip.session.LocationObserver
import com.mapbox.navigation.ui.maps.location.NavigationLocationProvider
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineApi
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineView
import com.mapbox.navigation.ui.maps.route.line.model.MapboxRouteLineOptions
import com.mapbox.navigation.ui.maps.route.line.model.RouteLine
import com.mapbox.navigation.ui.maps.route.line.model.RouteLineColorResources
import com.mapbox.navigation.ui.maps.route.line.model.RouteLineResources
import com.mapbox.navigation.utils.internal.toPoint
import ru.kamaz.mapboxmap.databinding.ActivityMapBinding

@MapboxExperimental
class MapActivity : AppCompatActivity() {

    private var currentPosition: Point? = null

    private var routesObserverRegistered = false
    private lateinit var pointAnnotationManager: PointAnnotationManager

    private lateinit var deviceAnnotation: PointAnnotation

    private lateinit var binding: ActivityMapBinding

    private val viewModel by viewModels<MapViewModel>()

    private val defaultLocationConsumer = DefaultLocationConsumer()

    private val onIndicatorBearingChangedListener = OnIndicatorBearingChangedListener {
        binding.mapView.getMapboxMap().setCamera(CameraOptions.Builder().bearing(it).build())
    }

    private val routeOptions: RouteOptions
        get() = RouteOptions.builder()
            // применяет параметры по умолчанию к параметрам маршрута
            .applyDefaultNavigationOptions()
            .applyLanguageAndVoiceUnitOptions(this)
            // заменяем выбранный выше профиль по умолчанию на профиль пешехода
            .profile(DirectionsCriteria.PROFILE_DRIVING)
            // в качестве начальной точки маршрута используем точку местонахождения
            // пользователя, а конечной - устройства (в данном примере его координаты статичны)
            .coordinatesList(
                listOf(
                    currentPosition,
                    deviceAnnotation.point
                )
            )
            // добавляем поиск альтернативных маршрутов к месту назначения
            .alternatives(true)
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

    private val onMoveListener = object : OnMoveListener {
        override fun onMoveBegin(detector: MoveGestureDetector) {}

        override fun onMove(detector: MoveGestureDetector) = false

        override fun onMoveEnd(detector: MoveGestureDetector) {}
    }

    private val defaultConsumerCurrentLocationObserver = Observer<Point?> { point ->
        currentPosition = point
        binding.run {
            if (point == null) {
                getPosition.run {
                    visibility = GONE
                    isEnabled = false
                }
                getRoute.run {
                    visibility = GONE
                    isEnabled = false
                }
            } else {
                getPosition.run {
                    visibility = VISIBLE
                    isEnabled = true
                }
                getRoute.run {
                    visibility = VISIBLE
                    isEnabled = true
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapBinding.inflate(layoutInflater).apply {
            setContentView(root)
            defaultLocationProvider = DefaultLocationProvider(this@MapActivity)

            workWithMap()
            onMapReady()
            buttonsSetup()
            initNavResources()

            defaultLocationConsumer.currentLocation.observe(
                this@MapActivity,
                defaultConsumerCurrentLocationObserver
            )

            viewModel.run {
                gpsState.run {
                    value =
                        (getSystemService(LOCATION_SERVICE) as LocationManager).isLocationEnabled
                    observe(this@MapActivity) { state ->
                        if (state) mapView.run {
                            gestures.setupListeners()
                            location.initComponent()
                        } else {
                            mapView.resetLocationComponentAndGesturesListener()
                            defaultLocationConsumer.currentLocation.value = null
                            viewModel.cameraState.value = null
                        }
                    }
                }
                cameraState.observe(this@MapActivity) { state ->
                    getPosition.setImageResource(
                        when (state) {
                            null -> R.drawable.track_user
                            true -> R.drawable.track_user_1
                            false -> R.drawable.track_user_2
                        }
                    )
                }
                routeState.observe(this@MapActivity) { state ->
                    if (state == true) navigationProvider() else if (state == null) resetNavigationProvider()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(gpsReceiver, IntentFilter().apply {
            addAction(LocationManager.PROVIDERS_CHANGED_ACTION)
        })
    }

    private fun initNavResources() {
        val routeLineResources = RouteLineResources.Builder()
            /*
            Цвета, относящиеся к линии маршрута, можно настроить с помощью
            [RouteLineColorResources]. Если используются цвета по умолчанию,
            [RouteLineColorResources] не нужно устанавливать, как показано здесь, значения по
            умолчанию будут использоваться внутренним строителем

            Кастомные настройки цветов линий маршрутов:
             */
            .routeLineColorResources(
                RouteLineColorResources.Builder()
                    .routeUnknownCongestionColor(
                        ContextCompat.getColor(
                            this,
                            R.color.button_color_center_nav
                        )
                    )// !меняет цвет основного маршрута!
                    .routeCasingColor(
                        ContextCompat.getColor(
                            this,
                            R.color.button_color_start_nav
                        )
                    )// !меняет цвет обводки основного маршрута!
                    // .alternativeRouteUnknownCongestionColor(getColor(R.color.button_color_off_nav))// !меняет цвет альтернативного(ых) маршрута(ов)!
                    // .alternativeRouteCasingColor(getColor(R.color.purple_700))// !меняет цвет обводки альтернативного(ых) маршрута(ов)!
                    .build()
            )
            .build()
        val options = MapboxRouteLineOptions.Builder(this)
            .withRouteLineResources(routeLineResources)
            .withRouteLineBelowLayerId("road-label")
            .build()

        routeLineApi = MapboxRouteLineApi(options)
        routeLineView = MapboxRouteLineView(options)
    }

    private fun defaultProvider() = defaultLocationProvider.apply {
        registerLocationConsumer(defaultLocationConsumer)
    }

    private fun navigationProvider() = mapboxNavigation.run {
        requestRoutes(routeOptions, object : RouterCallback {
            override fun onCanceled(
                routeOptions: RouteOptions,
                routerOrigin: RouterOrigin
            ) {
            }

            override fun onFailure(
                reasons: List<RouterFailure>,
                routeOptions: RouteOptions
            ) {
            }

            override fun onRoutesReady(
                routes: List<DirectionsRoute>,
                routerOrigin: RouterOrigin
            ) {
                registerRoutesObserver(routesObserver)
                setRoutes(routes)
            }
        })
        navigationLocationProvider
    }

    private fun resetNavigationProvider() = mapboxNavigation.run {
        unregisterLocationObserver(locationObserver)
        unregisterRoutesObserver(routesObserver)
        routeLineApi.clearRouteLine { value ->
            binding.mapView.getMapboxMap().getStyle()?.let { style ->
                routeLineView.renderClearRouteLineValue(style, value)
            }
        }
        setRoutes(emptyList())
    }

    private lateinit var routeLineApi: MapboxRouteLineApi
    private lateinit var routeLineView: MapboxRouteLineView


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

    private fun ActivityMapBinding.buttonsSetup() {
        getPosition.setOnClickListener {
            viewModel.cameraState.run {
                when (value) {
                    null -> {
                        mapView.run {
                            defaultLocationConsumer.currentLocation.value?.let {
                                getMapboxMap().setCamera(CameraOptions.Builder().center(it).build())
                            }
                            location.addOnIndicatorPositionChangedListener(
                                onIndicatorPositionChangedListener
                            )
                        }
                        value = false
                    }
                    true -> {
                        mapView.location.removeOnIndicatorBearingChangedListener(
                            onIndicatorBearingChangedListener
                        )
                        value = false
                    }
                    false -> {
                        mapView.location.addOnIndicatorBearingChangedListener(
                            onIndicatorBearingChangedListener
                        )
                        value = true
                    }
                }
            }
        }
        getRoute.setOnClickListener {
            viewModel.routeState.run {
                value = when (value) {
                    true -> null
                    null -> true
                    false -> true
                }
            }
        }
    }

    private val mapboxNavigation by lazy {
        if (MapboxNavigationProvider.isCreated()) {
            MapboxNavigationProvider.retrieve()
        } else {
            MapboxNavigationProvider.create(
                NavigationOptions.Builder(this)
                    .accessToken(getString(R.string.mapbox_access_token))
                    .build()
            )
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

    private val onMapClickListener = OnMapClickListener { point ->
        updateAnnotationOnMap(point)
        viewModel.routeState.value = false
        false
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

    private val navigationLocationProvider = NavigationLocationProvider()

    private lateinit var defaultLocationProvider: LocationProvider

    private val locationObserver = object : LocationObserver {
        override fun onNewLocationMatcherResult(locationMatcherResult: LocationMatcherResult) {
            val location = locationMatcherResult.enhancedLocation
            currentPosition = location.toPoint()
            navigationLocationProvider.changePosition(location)
        }

        override fun onNewRawLocation(rawLocation: Location) {}

    }

    private fun MapView.onCameraTrackingDismissed() {
        Toast.makeText(this@MapActivity, "onCameraTrackingDismissed", Toast.LENGTH_SHORT).show()
        location.run {
            removeOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
            removeOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener)
        }
        gestures.removeOnMoveListener(onMoveListener)
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(gpsReceiver)
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.mapView.run {
            resetLocationComponentAndGesturesListener()
            getMapboxMap().removeOnMapClickListener(onMapClickListener)
        }
        mapboxNavigation.run {
            unregisterLocationObserver(locationObserver)
            if (routesObserverRegistered) unregisterRoutesObserver(routesObserver)
        }
    }
}


