package ru.kamaz.mapboxmap

import android.animation.ValueAnimator
import androidx.lifecycle.MutableLiveData
import com.mapbox.geojson.Point
import com.mapbox.maps.plugin.locationcomponent.LocationConsumer

class DefaultLocationConsumer : LocationConsumer {

    var currentLocation = MutableLiveData<Point?>(null)
    override fun onBearingUpdated(vararg bearing: Double, options: (ValueAnimator.() -> Unit)?) {}

    override fun onLocationUpdated(vararg location: Point, options: (ValueAnimator.() -> Unit)?) {
        currentLocation.value = location.last()
    }

    override fun onPuckBearingAnimatorDefaultOptionsUpdated(options: ValueAnimator.() -> Unit) {}

    override fun onPuckLocationAnimatorDefaultOptionsUpdated(options: ValueAnimator.() -> Unit) {}
}