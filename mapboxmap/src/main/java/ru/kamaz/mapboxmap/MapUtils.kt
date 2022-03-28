package ru.kamaz.mapboxmap

import android.content.Context
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.core.content.ContextCompat.getColor
import com.mapbox.api.directions.v5.DirectionsCriteria
import com.mapbox.api.directions.v5.models.RouteOptions
import com.mapbox.navigation.base.extensions.applyDefaultNavigationOptions
import com.mapbox.navigation.base.extensions.applyLanguageAndVoiceUnitOptions
import com.mapbox.navigation.base.options.NavigationOptions
import com.mapbox.navigation.ui.maps.route.line.model.MapboxRouteLineOptions
import com.mapbox.navigation.ui.maps.route.line.model.RouteLineColorResources
import com.mapbox.navigation.ui.maps.route.line.model.RouteLineResources

object MapUtils {
    fun getDefaultRouteOptions(context: Context) = RouteOptions.builder()
        // применяет параметры по умолчанию к параметрам маршрута
        .applyDefaultNavigationOptions()
        .applyLanguageAndVoiceUnitOptions(context)
        // заменяем выбранный выше профиль по умолчанию на профиль пешехода
        .profile(DirectionsCriteria.PROFILE_DRIVING)
        // в качестве начальной точки маршрута используем точку местонахождения
        // пользователя, а конечной - устройства (в данном примере его координаты статичны)
        .alternatives(true)

    fun List<View>.setStateEnable(enable: Boolean) {
        forEach {
            it.run {
                isEnabled = enable
                visibility = if (enable) VISIBLE else GONE
            }
        }
    }

    private fun getDefaultRouteLineResources(context: Context) = RouteLineResources.Builder()
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
                    getColor(context, R.color.button_color_center_nav)
                )// !меняет цвет основного маршрута!
                .routeCasingColor(
                    getColor(context, R.color.button_color_start_nav)
                )// !меняет цвет обводки основного маршрута!
                // .alternativeRouteUnknownCongestionColor(getColor(R.color.button_color_off_nav))// !меняет цвет альтернативного(ых) маршрута(ов)!
                // .alternativeRouteCasingColor(getColor(R.color.purple_700))// !меняет цвет обводки альтернативного(ых) маршрута(ов)!
                .build()
        )
        .build()

    fun getDefaultRouteLineOptions(context: Context) = MapboxRouteLineOptions.Builder(context)
        .withRouteLineResources(getDefaultRouteLineResources(context))
        .withRouteLineBelowLayerId("road-label")
        .build()

    fun getDefaultNavigationOptions(context: Context) = NavigationOptions.Builder(context)
        .accessToken(context.getString(R.string.mapbox_access_token))
        .build()
}
