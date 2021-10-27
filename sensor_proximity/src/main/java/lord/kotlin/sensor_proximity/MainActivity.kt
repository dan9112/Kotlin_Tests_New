package lord.kotlin.sensor_proximity

import android.graphics.Color.GREEN
import android.graphics.Color.RED
import android.hardware.Sensor
import android.hardware.Sensor.TYPE_PROXIMITY
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.SensorManager.SENSOR_DELAY_NORMAL
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import lord.kotlin.sensor_proximity.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var sensor: Sensor? = null

    private lateinit var textView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main).apply {
            this@MainActivity.textView = textView
        }
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        sensor = sensorManager.getDefaultSensor(TYPE_PROXIMITY)
    }

    override fun onResume() {
        super.onResume()
        if (sensor != null) {
            sensorManager.registerListener(this, sensor, SENSOR_DELAY_NORMAL)
            Log.d("MainActivity", "Name: ${sensor?.name}")
            Log.d("MainActivity", "Maximum range: ${sensor?.maximumRange}")
        } else {
            Log.d("MainActivity", "Датчик не доступен")
            finish()
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == TYPE_PROXIMITY) {
            textView.apply {
                text = "Считывание показаний: "
                append(event.values[0].toString())
            }

            window.decorView.apply {
                // Проверяем наличие кота на телефоне
                if (event.values[0] < sensor!!.maximumRange) {
                    // Кот рядом!
                    setBackgroundColor(RED)
                } else {
                    // Кот ушёл
                    setBackgroundColor(GREEN)
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {

    }
}