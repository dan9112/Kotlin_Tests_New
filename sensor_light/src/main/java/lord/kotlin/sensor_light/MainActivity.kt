package lord.kotlin.sensor_light

import android.annotation.SuppressLint
import android.graphics.Color.BLUE
import android.graphics.Color.RED
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import lord.kotlin.sensor_light.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), SensorEventListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var sensorManager: SensorManager
    private var lightSensor: Sensor? = null

    private var lowPassFilter = LowPassFilter(10f)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
    }

    override fun onStart() {
        if (lightSensor != null) {
            sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_UI)
        } else Log.i("MainActivity", "Датчик освещения недоступен")
        super.onStart()
    }

    override fun onStop() {
        sensorManager.unregisterListener(this)
        super.onStop()
    }

    @SuppressLint("SetTextI18n")
    override fun onSensorChanged(event: SensorEvent?) {
        if (event != null) {
            if (event.sensor.type == Sensor.TYPE_LIGHT) {
                var lightLevel = event.values[0]

                lightLevel = lowPassFilter.submit(lightLevel)

                binding.textView.apply {
                    text = "LIGHT: $lightLevel"
                    textSize = lightLevel * 0.09f + 7.91f
                    if (lightLevel < 50) {
                        // Ночь
                        rootView.setBackgroundColor(BLUE)
                        setTextColor(RED)
                    } else {
                        // День
                        rootView.setBackgroundColor(RED)
                        setTextColor(BLUE)
                    }
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        //
    }

    // Для аппаратов Samsung
    private enum class LightLevel {
        LIGHT_LVL_1, LIGHT_LVL_2, LIGHT_LVL_3, LIGHT_LVL_4
    }

    private fun getLightLevelBySensorValue(sensorValue: Float): LightLevel {
        return when {
            sensorValue >= 15000.0 -> LightLevel.LIGHT_LVL_4
            sensorValue in 9000.0..15000.0 -> LightLevel.LIGHT_LVL_3
            sensorValue in 1000.0..9000.0 -> LightLevel.LIGHT_LVL_2
            else -> LightLevel.LIGHT_LVL_1
        }
    }

    class LowPassFilter(private val smoothing: Float) {
        private var filteredValue = 0f
        private var firstTime = true

        fun submit(newValue: Float): Float {
            return if (firstTime) {
                filteredValue = newValue
                firstTime = false
                filteredValue
            } else {
                filteredValue += (newValue - filteredValue) / smoothing
                filteredValue
            }
        }
    }
}