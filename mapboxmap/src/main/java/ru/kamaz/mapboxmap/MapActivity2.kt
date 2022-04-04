package ru.kamaz.mapboxmap

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityCompat.checkSelfPermission
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.mapbox.android.gestures.MoveGestureDetector
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.geojson.Point
import com.mapbox.maps.*
import com.mapbox.maps.extension.style.expressions.dsl.generated.interpolate
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.animation.camera
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
import com.mapbox.navigation.base.formatter.DistanceFormatterOptions
import com.mapbox.navigation.base.route.RouterOrigin
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.MapboxNavigationProvider
import com.mapbox.navigation.core.directions.session.RoutesObserver
import com.mapbox.navigation.core.trip.session.LocationMatcherResult
import com.mapbox.navigation.core.trip.session.RouteProgressObserver
import com.mapbox.navigation.ui.maps.camera.NavigationCamera
import com.mapbox.navigation.ui.maps.camera.data.MapboxNavigationViewportDataSource
import com.mapbox.navigation.ui.maps.camera.lifecycle.NavigationBasicGesturesHandler
import com.mapbox.navigation.ui.maps.location.NavigationLocationProvider
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineApi
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineView
import com.mapbox.navigation.ui.maps.route.line.model.RouteLine
import com.mapbox.navigation.ui.tripprogress.api.MapboxTripProgressApi
import com.mapbox.navigation.ui.tripprogress.model.DistanceRemainingFormatter
import com.mapbox.navigation.ui.tripprogress.model.EstimatedTimeToArrivalFormatter
import com.mapbox.navigation.ui.tripprogress.model.TimeRemainingFormatter
import com.mapbox.navigation.ui.tripprogress.model.TripProgressUpdateFormatter
import ru.kamaz.mapboxmap.MapUtils.setStateEnable
import ru.kamaz.mapboxmap.MapViewModel.ModelStates
import ru.kamaz.mapboxmap.databinding.ActivityMapBinding
import ru.kamaz.mapboxmap.default_objects.DefaultLocationConsumer
import ru.kamaz.mapboxmap.default_objects.DefaultLocationObserver
import ru.kamaz.mapboxmap.default_objects.DefaultOnMoveListener
import ru.kamaz.mapboxmap.default_objects.DefaultRouterCallback

@MapboxExperimental
class MapActivity2 : AppCompatActivity() {
    private lateinit var viewModel: MapViewModel
    private var _binding: ActivityMapBinding? = null
    private val binding: ActivityMapBinding
        get() = _binding!!
    private val defaultOnMapClickListener = OnMapClickListener { point ->
        updateAnnotationOnMap(point)
        viewModel.run {
            if (routeState.value == true) setRouteStateValue(false, postValue = true)
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

    private lateinit var mapboxNavigation: MapboxNavigation

    private val defaultOnMoveListener = object : DefaultOnMoveListener() {
        override fun onMove(detector: MoveGestureDetector) = run {
            viewModel.setCameraStateValue(null, postValue = true)
            false
        }
    }

    private val defaultConsumerCurrentLocationObserver = Observer<Point?> { point ->
        if (point != null && viewModel.modelState.value == ModelStates.Base) viewModel.setModelStateValue(
            ModelStates.UserIsTracked,
            postValue = true
        )
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

    private val navigationCameraStateObserver = Observer<Boolean?> { state ->
        when (state) {
            null -> navigationCamera.requestNavigationCameraToIdle()
            false -> navigationCamera.requestNavigationCameraToOverview()
            true -> navigationCamera.requestNavigationCameraToFollowing()
        }
        binding.getPosition.setImageResource(
            when (state) {
                null -> R.drawable.track_user
                true -> R.drawable.track_user_1
                false -> R.drawable.track_user_2
            }
        )
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

    private val defaultGpsStateObserver = Observer<Boolean> { state ->
        binding.mapView.location.updateSettings { enabled = state }
        when (viewModel.modelState.value) {
            ModelStates.UserIsTracked -> if (!state) viewModel.setModelStateValue(
                ModelStates.Base, postValue = true
            )
            ModelStates.RouteBuilt -> if (!state) viewModel.setModelStateValue(
                ModelStates.RouteBuiltUserNotTracked, postValue = true
            )
            ModelStates.RouteBuiltUserNotTracked -> if (state) viewModel.setModelStateValue(
                ModelStates.RouteBuilt, postValue = true
            )
            else -> Log.d(
                "MapActivity2",
                "Недопустимый статус модели во время изменения статуса GPS-модуля"
            )
        }
    }

    private val navigationGpsStateObserver = Observer<Boolean> { state ->
        if (!state) {
            mapboxNavigation.run {
                navigationCamera.requestNavigationCameraToOverview()
                unregisterRouteProgressObserver(routeProgressObserver)
                stopTripSession()
            }
            viewModel.setModelStateValue(ModelStates.RouteBuiltUserNotTracked, postValue = true)
        }
    }

    private lateinit var navigationCameraAnimationsLifecycleListener: NavigationBasicGesturesHandler

    private val connectionObserver = Observer<Boolean> {
        if (it) {
            viewModel.setModelStateValue(ModelStates.Base, postValue = true)
            if (viewModel.dialogIsShown) {
                (supportFragmentManager.findFragmentByTag("NED") as? DialogFragment)?.dismiss()
                viewModel.dialogIsShown = false
            }
        } else if (!it) {
            viewModel.setModelStateValue(null, postValue = true)
            Log.e("NetworkStateLog", "Нет сети!")
            noNetDialogShow()
        }
    }

    @SuppressLint("ShowToast")
    private fun noNetDialogShow() {
        if (!viewModel.dialogIsShown) {
            viewModel.dialogIsShown = true
            NetworkConnectionLostDialog.newInstance {
                viewModel.dialogIsShown = false
                if (viewModel.connectionLiveData.value != true) {
                    Snackbar.make(
                        binding.root,
                        "Подключение к интернету отсутствует. Карта недоступна.",
                        Snackbar.LENGTH_LONG
                    ).setTextColor(Color.WHITE).setBackgroundTint(
                        ContextCompat.getColor(
                            this,
                            R.color.button_color_off_nav
                        )
                    ).show()
                    finish()
                }
            }.show(supportFragmentManager, "NED")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMapBinding.inflate(layoutInflater).apply {
            setContentView(root)
            viewModel = ViewModelProvider(
                this@MapActivity2,
                ViewModelFactory(application)
            )[MapViewModel::class.java]
            viewportDataSource = MapboxNavigationViewportDataSource(mapView.getMapboxMap()).apply {
                Resources.getSystem().displayMetrics.density.run {
                    overviewPadding = EdgeInsets(
                        140.0 * this,
                        40.0 * this,
                        120.0 * this,
                        40.0 * this
                    )
                    followingPadding = EdgeInsets(
                        180.0 * this,
                        40.0 * this,
                        150.0 * this,
                        40.0 * this
                    )
                }
            }
            navigationCamera = mapView.run {
                NavigationCamera(
                    getMapboxMap(),
                    camera,
                    viewportDataSource
                )
            }
            navigationCameraAnimationsLifecycleListener =
                NavigationBasicGesturesHandler(navigationCamera)
            mapboxNavigation = MapboxNavigationProvider.run {
                if (isCreated()) retrieve() else create(MapUtils.getDefaultNavigationOptions(this@MapActivity2))
            }
            tripProgressView.background =
                ContextCompat.getDrawable(this@MapActivity2, R.drawable.button_nav_off)
            tripProgressApi = run {
                val distanceFormatterOptions =
                    DistanceFormatterOptions.Builder(this@MapActivity2)
                        .build()
                val tripProgressFormatter =
                    TripProgressUpdateFormatter.Builder(this@MapActivity2)
                        .distanceRemainingFormatter(
                            DistanceRemainingFormatter(
                                distanceFormatterOptions
                            )
                        )
                        .timeRemainingFormatter(
                            TimeRemainingFormatter(this@MapActivity2)
                        )
                        .estimatedTimeToArrivalFormatter(
                            EstimatedTimeToArrivalFormatter(this@MapActivity2)
                        )
                        .build()
                MapboxTripProgressApi(tripProgressFormatter)
            }
            defaultLocationProvider = DefaultLocationProvider(this@MapActivity2)
            onMapReady()
            initNavResources()
            buttonsSetup()

            viewModel.run {
                connectionLiveData.observe(this@MapActivity2, connectionObserver)
                val connectManager =
                    (getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (connectManager.activeNetwork == null) noNetDialogShow()
                } else @Suppress("DEPRECATION") if (connectManager.activeNetworkInfo == null || !connectManager.activeNetworkInfo!!.isConnected) noNetDialogShow()
                modelState.run {
                    observe(this@MapActivity2) { state ->
                        state.let {
                            mapView.run {
                                getMapboxMap().run {
                                    if (it == null) {
                                        if (!firstTry) {// При первом включении ничего ещё нет
                                            unregisterReceiver(defaultGpsReceiver)
                                            gpsState.removeObserver(navigationGpsStateObserver)
                                            cameraStateVM.removeObserver(
                                                navigationCameraStateObserver
                                            )
                                            mapboxNavigation.unregisterLocationObserver(
                                                locationObserver
                                            )
                                            camera.removeCameraAnimationsLifecycleListener(
                                                navigationCameraAnimationsLifecycleListener
                                            )
                                        }
                                    } else {
                                        setGpsStateValue((getSystemService(LOCATION_SERVICE) as LocationManager).run {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) isLocationEnabled else isProviderEnabled(
                                                LocationManager.GPS_PROVIDER
                                            )
                                        })
                                        registerReceiver(defaultGpsReceiver, IntentFilter().apply {
                                            addAction(LocationManager.PROVIDERS_CHANGED_ACTION)
                                        })
                                    }
                                    if (it == ModelStates.RouteProgressIsTracked || it == null) {
                                        gestures.removeOnMoveListener(defaultOnMoveListener)
                                        removeOnMapClickListener(defaultOnMapClickListener)
                                        defaultLocationConsumer.currentLocation.removeObserver(
                                            defaultConsumerCurrentLocationObserver
                                        )
                                        cameraStateVM.removeObserver(defaultCameraStateObserver)
                                        location.removeOnIndicatorBearingChangedListener(
                                            onIndicatorBearingChangedListener
                                        )
                                        location.removeOnIndicatorPositionChangedListener(
                                            onIndicatorPositionChangedListener
                                        )
                                        routeState.removeObserver(defaultRouteStateObserver)
                                        gpsState.removeObserver(defaultGpsStateObserver)
                                        if (it == ModelStates.RouteProgressIsTracked) {
                                            location.setLocationProvider(navigationLocationProvider)
                                            gpsState.observe(
                                                this@MapActivity2,
                                                navigationGpsStateObserver
                                            )
                                            cameraStateVM.observe(
                                                this@MapActivity2,
                                                navigationCameraStateObserver
                                            )
                                            mapboxNavigation.registerLocationObserver(
                                                locationObserver
                                            )
                                            camera.addCameraAnimationsLifecycleListener(
                                                navigationCameraAnimationsLifecycleListener
                                            )
                                        }
                                    } else {
                                        mapboxNavigation.unregisterLocationObserver(locationObserver)
                                        camera.removeCameraAnimationsLifecycleListener(
                                            navigationCameraAnimationsLifecycleListener
                                        )
                                        location.run {
                                            if (cameraStateVM.value != null) addOnIndicatorPositionChangedListener(
                                                onIndicatorPositionChangedListener
                                            )
                                            if (cameraStateVM.value == true) addOnIndicatorBearingChangedListener(
                                                onIndicatorBearingChangedListener
                                            )
                                            setLocationProvider(defaultProvider())
                                        }
                                        gestures.addOnMoveListener(defaultOnMoveListener)
                                        gpsState.run {
                                            // value = false
                                            removeObserver(navigationGpsStateObserver)
                                            observe(this@MapActivity2, defaultGpsStateObserver)
                                        }
                                        addOnMapClickListener(defaultOnMapClickListener)
                                        defaultLocationConsumer.currentLocation.observe(
                                            this@MapActivity2,
                                            defaultConsumerCurrentLocationObserver
                                        )
                                        cameraStateVM.run {
                                            removeObserver(navigationCameraStateObserver)
                                            observe(this@MapActivity2, defaultCameraStateObserver)
                                        }
                                        routeState.observe(
                                            this@MapActivity2,
                                            defaultRouteStateObserver
                                        )
                                    }
                                }
                            }
                        }
                        when (state) {
                            null -> {
                                listOf(
                                    getPosition,
                                    getRoute,
                                    mapView,
                                    tripProgressView,
                                    startTrip
                                ).setStateEnable(false)
                                setRouteStateValue(null, postValue = true)
                                setCameraStateValue(null, postValue = true)
                            }
                            ModelStates.Base -> {
                                if (firstTry) {
                                    if (checkSelfPermission(
                                            this@MapActivity2,
                                            ACCESS_FINE_LOCATION
                                        ) != PERMISSION_GRANTED && checkSelfPermission(
                                            this@MapActivity2,
                                            ACCESS_COARSE_LOCATION
                                        ) != PERMISSION_GRANTED
                                    ) askPermissions()
                                    firstTry = false
                                }
                                listOf(mapView, getPosition).setStateEnable(true)
                                listOf(
                                    getRoute,
                                    tripProgressView,
                                    startTrip
                                ).setStateEnable(false)
                                setRouteStateValue(null, postValue = true)
                                setCameraStateValue(null, postValue = true)
                            }
                            ModelStates.UserIsTracked -> {
                                listOf(getPosition, getRoute, mapView).setStateEnable(true)
                                listOf(tripProgressView, startTrip).setStateEnable(false)
                                setRouteStateValue(null, postValue = true)
                            }
                            ModelStates.RouteBuilt -> {
                                listOf(getPosition, getRoute, mapView, startTrip).setStateEnable(
                                    true
                                )
                                listOf(tripProgressView).setStateEnable(false)
                                setRouteStateValue(true, postValue = true)
                            }
                            ModelStates.RouteBuiltUserNotTracked -> {
                                listOf(getRoute, mapView).setStateEnable(true)
                                listOf(getPosition, tripProgressView, startTrip).setStateEnable(
                                    false
                                )
                                setRouteStateValue(false, postValue = true)
                                setCameraStateValue(null, postValue = true)
                            }
                            ModelStates.RouteProgressIsTracked -> {
                                listOf(
                                    getPosition,
                                    startTrip,
                                    tripProgressView,
                                    mapView
                                ).setStateEnable(true)
                                listOf(getRoute).setStateEnable(false)
                            }
                        }
                    }
                }
            }
        }
    }

    private val locationObserver = object : DefaultLocationObserver() {
        override fun onNewLocationMatcherResult(locationMatcherResult: LocationMatcherResult) {
            // lastKnownLocation = locationMatcherResult.enhancedLocation
            // Timber.d(
            //     "Bearing: ${lastKnownLocation.bearing}\nLocation: ${
            //         lastKnownLocation.toPoint().coordinates()
            //     }"
            // )
            navigationLocationProvider.changePosition(locationMatcherResult.enhancedLocation)
            viewportDataSource.run {
                onLocationChanged(locationMatcherResult.enhancedLocation)

                evaluate()
            }
        }
    }

    @SuppressLint("ShowToast")
    private fun ActivityMapBinding.buttonsSetup() {
        viewModel.run {
            getPosition.setOnClickListener {
                if (modelState.value == ModelStates.Base) {
                    if (checkSelfPermission(
                            this@MapActivity2,
                            ACCESS_FINE_LOCATION
                        ) != PERMISSION_GRANTED && checkSelfPermission(
                            this@MapActivity2,
                            ACCESS_COARSE_LOCATION
                        ) != PERMISSION_GRANTED
                    ) askPermissions()
                    else if (gpsState.value != true) Snackbar.make(
                        binding.root,
                        "Для использования компонентов навигации, включите GPS",
                        Snackbar.LENGTH_LONG
                    ).setTextColor(Color.WHITE).setBackgroundTint(
                        ContextCompat.getColor(
                            this@MapActivity2,
                            R.color.button_color_off_nav
                        )
                    ).show()
                } else setCameraStateValue(
                    when (cameraStateVM.value) {
                        null -> false
                        true -> false
                        false -> true
                    }
                )
            }
            getRoute.run {
                setOnClickListener {
                    when (modelState.value) {
                        ModelStates.RouteBuiltUserNotTracked -> setModelStateValue(ModelStates.Base)
                        ModelStates.UserIsTracked -> setModelStateValue(ModelStates.RouteBuilt)
                        ModelStates.RouteBuilt -> if (routeState.value == false) setRouteStateValue(
                            true
                        )
                        else if (routeState.value == true) setModelStateValue(ModelStates.UserIsTracked)
                        else -> Log.e(
                            "MapActivity2",
                            "Ошибка в доступности кнопки \"getRoute\""
                        )
                    }
                }
                setOnLongClickListener {
                    setModelStateValue(
                        when (modelState.value) {
                            ModelStates.RouteBuilt -> {
                                setRouteStateValue(null)
                                ModelStates.UserIsTracked
                            }
                            ModelStates.RouteBuiltUserNotTracked -> {
                                setRouteStateValue(null)
                                ModelStates.Base
                            }
                            else -> return@setOnLongClickListener false
                        }
                    )
                    true
                }
            }
            startTrip.setOnClickListener {
                if (modelState.value == ModelStates.RouteProgressIsTracked) {
                    mapboxNavigation.run {
                        navigationCamera.requestNavigationCameraToOverview()
                        setCameraStateValue(null)
                        unregisterRouteProgressObserver(routeProgressObserver)
                        stopTripSession()
                    }
                    setModelStateValue(ModelStates.RouteBuilt)
                } else if (modelState.value == ModelStates.RouteBuilt) {
                    mapboxNavigation.run {
                        registerRouteProgressObserver(routeProgressObserver)
                        if (checkSelfPermission(
                                this@MapActivity2,
                                ACCESS_FINE_LOCATION
                            ) != PERMISSION_GRANTED && checkSelfPermission(
                                this@MapActivity2,
                                ACCESS_COARSE_LOCATION
                            ) != PERMISSION_GRANTED
                        ) {
                            setModelStateValue(null)
                            return@setOnClickListener
                        }
                        startTripSession()
                    }
                    setModelStateValue(ModelStates.RouteProgressIsTracked)
                }
            }
        }
    }

    private val routeProgressObserver = RouteProgressObserver { progress ->
        binding.tripProgressView.render(tripProgressApi.getTripProgress(progress))
        viewportDataSource.run {
            onRouteProgressChanged(progress)
            evaluate()
        }
    }

    @SuppressLint("ShowToast")
    private fun ActivityMapBinding.askPermissions() {
        val snackBar: Snackbar
        if (shouldShowRequestPermissionRationale(this@MapActivity2, ACCESS_COARSE_LOCATION)) {
            getSharedPreferences("preference_permission", MODE_PRIVATE).edit().apply {
                putBoolean("location_permissions", false)
            }.apply()
            snackBar = Snackbar.make(
                root,
                "Разрешение необходимо, чтобы использовать компоненты навигации",
                Snackbar.LENGTH_LONG
            )
            snackBar.setAction("Ok") {
                locationPermissionRequest.launch(
                    arrayOf(
                        ACCESS_COARSE_LOCATION,
                        ACCESS_FINE_LOCATION
                    )
                )
            }.setActionTextColor(
                ContextCompat.getColor(
                    this@MapActivity2,
                    R.color.button_color_center_nav
                )
            )
        } else {
            val callback = object : Snackbar.Callback() {
                override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                    super.onDismissed(transientBottomBar, event)
                    locationPermissionRequest.launch(
                        arrayOf(
                            ACCESS_COARSE_LOCATION,
                            ACCESS_FINE_LOCATION
                        )
                    )
                }
            }
            snackBar = Snackbar.make(
                root,
                "Нет доступа к компонентам навигации",
                Snackbar.LENGTH_SHORT
            ).addCallback(callback)
            snackBar.view.setOnClickListener {
                snackBar.dismiss()
            }
        }
        snackBar.setTextColor(Color.WHITE).setBackgroundTint(
            ContextCompat.getColor(
                this@MapActivity2,
                R.color.button_color_off_nav
            )
        ).show()
    }

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        var granted = false
        permissions.entries.forEach { if (it.value) granted = true }
        if (!granted) {
            val snackBar: Snackbar
            when {
                getSharedPreferences(
                    "preference_permission",
                    MODE_PRIVATE
                ).getBoolean("location_permissions", true) -> {
                    snackBar = Snackbar.make(
                        binding.root,
                        "Компоненты навигации недоступны, используется только карта",
                        Snackbar.LENGTH_SHORT
                    )
                }
                else -> snackBar = Snackbar.make(
                    binding.root,
                    "Компоненты навигации недоступны, используется только карта. Включите разрешение определения местоположения самостоятельно",
                    Snackbar.LENGTH_SHORT
                ).apply {
                    view.run {
                        findViewById<TextView>(R.id.snackbar_text).maxLines = 3
                        setOnClickListener {
                            startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", packageName, null)
                            })
                        }
                    }
                }
            }
            snackBar.setTextColor(Color.WHITE).setBackgroundTint(
                ContextCompat.getColor(
                    this,
                    R.color.button_color_off_nav
                )
            ).show()
            if (viewModel.modelState.value != null && viewModel.modelState.value != ModelStates.Base) viewModel.setModelStateValue(
                ModelStates.Base,
                postValue = true
            )
        }
    }

    private lateinit var navigationCamera: NavigationCamera

    private lateinit var tripProgressApi: MapboxTripProgressApi
    private lateinit var viewportDataSource: MapboxNavigationViewportDataSource

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
            viewportDataSource.onRouteChanged(routeUpdateResult.routes.first())
        } else {
            val style = binding.mapView.getMapboxMap().getStyle()
            if (style != null) {
                routeLineApi.clearRouteLine { value ->
                    routeLineView.renderClearRouteLineValue(style, value)
                }
            }
            viewportDataSource.clearRouteData()
        }
        viewportDataSource.evaluate()
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
        location.run {
            removeOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
            removeOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener)
        }
        gestures.removeOnMoveListener(defaultOnMoveListener)
    }

    private val defaultGpsReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            viewModel.setGpsStateValue((getSystemService(LOCATION_SERVICE) as LocationManager).run {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) isLocationEnabled else
                    isProviderEnabled(LocationManager.GPS_PROVIDER)
            }, postValue = true)
        }
    }

    override fun onDestroy() {
        viewModel.connectionLiveData.removeObserver(connectionObserver)
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
                removeOnMapClickListener(defaultOnMapClickListener)
            }
        }
        if (viewModel.modelState.value == ModelStates.RouteProgressIsTracked) mapboxNavigation.run {
            navigationCamera.requestNavigationCameraToOverview()
            unregisterRouteProgressObserver(routeProgressObserver)
            stopTripSession()
        }
        if (viewModel.modelState.value != null && viewModel.modelState.value != ModelStates.RouteProgressIsTracked) unregisterReceiver(
            defaultGpsReceiver
        )
        mapboxNavigation.run {
            unregisterRoutesObserver(routesObserver)
            onDestroy()
        }
        super.onDestroy()
    }
}
