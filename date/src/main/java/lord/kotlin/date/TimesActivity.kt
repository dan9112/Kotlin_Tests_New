package lord.kotlin.date

import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import lord.kotlin.date.databinding.ActivityTimesBinding
import java.text.DecimalFormat
import java.util.*

class TimesActivity : AppCompatActivity() {
    private lateinit var spinnerAvailableID: Spinner
    private lateinit var textTimeZone: TextView
    private lateinit var idAdapter: ArrayAdapter<String>

    private val startTimeMs = SystemClock.elapsedRealtime()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DataBindingUtil.setContentView<ActivityTimesBinding>(this, R.layout.activity_times).apply {
            textTimeZone = timezone
            spinnerAvailableID = availableID
            button.setOnClickListener {
                Log.d(
                    "TimesActivity",
                    "Время после запуска: ${DecimalFormat("#.##").format((SystemClock.elapsedRealtime() - startTimeMs).toDouble() / 1000)} с."
                )
            }
        }
        idAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            TimeZone.getAvailableIDs()
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        spinnerAvailableID.apply {
            adapter = idAdapter
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View, position: Int, id: Long
                ) {
                    TimeZone.getTimeZone(parent.getItemAtPosition(position) as String).apply {
                        textTimeZone.text =
                            ("$displayName : ${rawOffset / (60 * 60 * 1000)}")
                    }
                }

                override fun onNothingSelected(arg0: AdapterView<*>?) {

                }
            }
        }
    }
}
