package ru.kamaz.mapboxmap.default_objects

import android.animation.ValueAnimator
import androidx.lifecycle.MutableLiveData
import com.mapbox.geojson.Point
import com.mapbox.maps.plugin.locationcomponent.LocationConsumer

class DefaultLocationConsumer : LocationConsumer {

    val currentLocation = MutableLiveData<Point?>(null)
    val currentBearing = MutableLiveData<Double?>(null)
    override fun onBearingUpdated(vararg bearing: Double, options: (ValueAnimator.() -> Unit)?) {
        currentBearing.value = bearing.last()
    }

    override fun onLocationUpdated(vararg location: Point, options: (ValueAnimator.() -> Unit)?) {
        currentLocation.value = location.last()
    }

    override fun onPuckBearingAnimatorDefaultOptionsUpdated(options: ValueAnimator.() -> Unit) {}

    override fun onPuckLocationAnimatorDefaultOptionsUpdated(options: ValueAnimator.() -> Unit) {}
}
