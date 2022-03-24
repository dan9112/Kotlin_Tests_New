package ru.kamaz.mapboxmap

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import com.mapbox.android.gestures.MoveGestureDetector
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.Style.Companion.MAPBOX_STREETS
import com.mapbox.maps.extension.style.expressions.dsl.generated.interpolate
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.gestures.OnMoveListener
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorBearingChangedListener
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import com.mapbox.maps.plugin.locationcomponent.location
import ru.kamaz.mapboxmap.databinding.ActivityMapBinding

class MapActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMapBinding

    private val viewModel by viewModels<MapViewModel>()

    private val ActivityMapBinding.onIndicatorBearingChangedListener
        get() = OnIndicatorBearingChangedListener {
            mapView.getMapboxMap().setCamera(CameraOptions.Builder().bearing(it).build())
        }

    private val ActivityMapBinding.onIndicatorPositionChangedListener
        get() = OnIndicatorPositionChangedListener {
            mapView.run {
                getMapboxMap().run {
                    setCamera(CameraOptions.Builder().center(it).build())
                    gestures.focalPoint = pixelForCoordinate(it)
                }
            }
        }

    private val gpsReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            viewModel.gpsState.value =
                (getSystemService(LOCATION_SERVICE) as LocationManager).isLocationEnabled
        }
    }

    private val onMoveListener = object : OnMoveListener {
        override fun onMoveBegin(detector: MoveGestureDetector) {
            binding.onCameraTrackingDismissed()
        }

        override fun onMove(detector: MoveGestureDetector): Boolean {
            return false
        }

        override fun onMoveEnd(detector: MoveGestureDetector) {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapBinding.inflate(layoutInflater).apply {
            setContentView(root)

            registerReceiver(gpsReceiver, IntentFilter().apply {
                addAction(LocationManager.PROVIDERS_CHANGED_ACTION)
            })

            workWithMap()
            onMapReady()

            viewModel.gpsState.observe(this@MapActivity) {
                if (it) {
                    initLocationComponent()
                    setupGesturesListener()
                } else resetLocationComponentAndGesturesListener()
            }
        }
    }

    private fun ActivityMapBinding.resetLocationComponentAndGesturesListener() {
        mapView.location.updateSettings { enabled = false }
        onCameraTrackingDismissed()
    }

    private fun ActivityMapBinding.workWithMap() {
        mapView.isEnabled = true
        getPosition.isEnabled = true
        mapView.visibility = View.VISIBLE
        getPosition.visibility = View.VISIBLE
    }

    private fun ActivityMapBinding.onMapReady() {
        mapView.run {
            getMapboxMap().run {
                setCamera(
                    CameraOptions.Builder()
                        .zoom(14.0)
                        .build()
                )
                loadStyleUri(MAPBOX_STREETS) {
                    location.updateSettings {
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
            }
        }
    }

    private fun ActivityMapBinding.setupGesturesListener() {
        mapView.gestures.addOnMoveListener(onMoveListener)
    }

    private fun ActivityMapBinding.initLocationComponent() {
        mapView.location.run {
            updateSettings { enabled = true }
            addOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
            addOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener)
        }
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
        binding.resetLocationComponentAndGesturesListener()
    }
}