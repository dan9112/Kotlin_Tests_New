package lord.kotlin.sensor_light

import android.annotation.SuppressLint
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.databinding.DataBindingUtil
import lord.kotlin.sensor_light.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), SensorEventListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var sensorManager: SensorManager
    private var lightSensor: Sensor? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
    }

    override fun onStart() {
        super.onStart()
        if (lightSensor != null) {
            sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL)
        } else Log.i("MainActivity", "Датчик освещения недоступен")
    }

    override fun onStop() {
        super.onStop()
        sensorManager.unregisterListener(this)
    }

    @SuppressLint("SetTextI18n")
    override fun onSensorChanged(event: SensorEvent?) {
        if (event != null) {
            val lightLevel = event.values[0]

            if (event.sensor.type == Sensor.TYPE_LIGHT) {
                binding.textView.text = "LIGHT: $lightLevel"
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
}