package ru.kamaz.mapboxmap.default_objects

import com.mapbox.android.gestures.MoveGestureDetector
import com.mapbox.maps.plugin.gestures.OnMoveListener

open class DefaultOnMoveListener : OnMoveListener {
    override fun onMove(detector: MoveGestureDetector) = false

    override fun onMoveBegin(detector: MoveGestureDetector) {}

    override fun onMoveEnd(detector: MoveGestureDetector) {}
}
