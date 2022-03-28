package ru.kamaz.mapboxmap.default_objects

import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.api.directions.v5.models.RouteOptions
import com.mapbox.navigation.base.route.RouterCallback
import com.mapbox.navigation.base.route.RouterFailure
import com.mapbox.navigation.base.route.RouterOrigin

open class DefaultRouterCallback : RouterCallback {
    override fun onCanceled(routeOptions: RouteOptions, routerOrigin: RouterOrigin) {}

    override fun onFailure(reasons: List<RouterFailure>, routeOptions: RouteOptions) {}

    override fun onRoutesReady(routes: List<DirectionsRoute>, routerOrigin: RouterOrigin) {}
}
