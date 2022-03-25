package ru.kamaz.mapboxmap

import android.animation.ValueAnimator
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
import androidx.lifecycle.Observer
import com.mapbox.android.gestures.MoveGestureDetector
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapboxExperimental
import com.mapbox.maps.RenderCacheOptions
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
import com.mapbox.maps.setDisabled
import com.mapbox.navigation.base.options.NavigationOptions
import com.mapbox.navigation.core.MapboxNavigationProvider
import com.mapbox.navigation.core.trip.session.LocationMatcherResult
import com.mapbox.navigation.core.trip.session.LocationObserver
import com.mapbox.navigation.ui.maps.location.NavigationLocationProvider
import ru.kamaz.mapboxmap.databinding.ActivityMapBinding

@MapboxExperimental
class MapActivity : AppCompatActivity() {

    private lateinit var pointAnnotationManager: PointAnnotationManager

    private lateinit var deviceAnnotation: PointAnnotation

    private lateinit var binding: ActivityMapBinding

    private val viewModel by viewModels<MapViewModel>()

    private val defaultLocationConsumer = DefaultLocationConsumer()

    private val onIndicatorBearingChangedListener = OnIndicatorBearingChangedListener {
        binding.mapView.getMapboxMap().setCamera(CameraOptions.Builder().bearing(it).build())
    }

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

            registerReceiver(gpsReceiver, IntentFilter().apply {
                addAction(LocationManager.PROVIDERS_CHANGED_ACTION)
            })
            workWithMap()
            onMapReady()
            buttonsSetup()

            defaultLocationConsumer.currentLocation.observe(
                this@MapActivity,
                defaultConsumerCurrentLocationObserver
            )

            viewModel.run {
                gpsState.run {
                    value =
                        (getSystemService(LOCATION_SERVICE) as LocationManager).isLocationEnabled
                    observe(this@MapActivity) { state ->
                        if (state) {
                            setupGesturesListener()
                            initLocationComponent()
                        } else {
                            resetLocationComponentAndGesturesListener()
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

    private fun ActivityMapBinding.resetLocationComponentAndGesturesListener() {
        mapView.run {
            location.run {
                getLocationProvider()?.unRegisterLocationConsumer(defaultLocationConsumer)
                updateSettings { enabled = false }
            }
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

    private fun ActivityMapBinding.setupGesturesListener() {
        mapView.gestures.run {
            addOnMoveListener(onMoveListener)
            addOnMapClickListener(onMapClickListener)
        }
    }

    private fun ActivityMapBinding.initLocationComponent() {
        mapView.location.run {
            updateSettings { enabled = true }
            setLocationProvider(defaultLocationProvider.apply {
                registerLocationConsumer(defaultLocationConsumer)
            })
        }
    }

    private val navigationLocationProvider = NavigationLocationProvider()

    private lateinit var defaultLocationProvider: LocationProvider

    private val locationObserver = object : LocationObserver {
        override fun onNewLocationMatcherResult(locationMatcherResult: LocationMatcherResult) {
            val transitionOptions: (ValueAnimator.() -> Unit) = {
                duration = if (locationMatcherResult.isTeleport) 0 else 1000
            }
            navigationLocationProvider.changePosition(
                locationMatcherResult.enhancedLocation,
                locationMatcherResult.keyPoints,
                latLngTransitionOptions = transitionOptions,
                bearingTransitionOptions = transitionOptions
            )
        }

        override fun onNewRawLocation(rawLocation: Location) {}

    }

    private fun ActivityMapBinding.onCameraTrackingDismissed() {
        Toast.makeText(this@MapActivity, "onCameraTrackingDismissed", Toast.LENGTH_SHORT).show()
        mapView.run {
            location.run {
                removeOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
                removeOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener)
            }
            gestures.removeOnMoveListener(onMoveListener)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(gpsReceiver)
        binding.run {
            resetLocationComponentAndGesturesListener()
            mapView.getMapboxMap().removeOnMapClickListener(onMapClickListener)
        }
        mapboxNavigation.unregisterLocationObserver(locationObserver)
    }
}