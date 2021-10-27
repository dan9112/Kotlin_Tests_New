package lord.kotlin.date

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import lord.kotlin.date.databinding.ActivityMainBinding
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    @SuppressLint("SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        val formatter = SimpleDateFormat("dd/MM/yyyy")
        val dateString = formatter.format(Date())
        Log.d("MainActivity", "Date: $dateString")

        val calendar = Calendar.getInstance()
        val df = SimpleDateFormat("dd:MM:yyyy HH:mm:ss")
        val formattedDate = df.format(calendar.time)
        Log.d("MainActivity", "Calendar: $formattedDate")

        val curDateTime = Date()
        Log.d("MainActivity", "Calendar (1): $curDateTime")
        String.apply {
            val locale = Locale("ru")
            Log.d(
                "MainActivity",
                "Calendar (2): ${format(locale, "%tc\n", curDateTime)} ${
                    format(locale, "%tD\n", curDateTime)
                } ${
                    format(locale, "%tF\n", curDateTime)
                } ${
                    format(locale, "%tr\n", curDateTime)
                } ${
                    format(locale, "%tz\n", curDateTime)
                } ${
                    format(locale, "%tZ\n", curDateTime)
                }"
            )
        }
    }
}
