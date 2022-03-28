package ru.kamaz.mapboxmap.default_objects

import android.location.Location
import com.mapbox.navigation.core.trip.session.LocationMatcherResult
import com.mapbox.navigation.core.trip.session.LocationObserver

open class DefaultLocationObserver : LocationObserver {
    override fun onNewLocationMatcherResult(locationMatcherResult: LocationMatcherResult) {}

    override fun onNewRawLocation(rawLocation: Location) {}
}
