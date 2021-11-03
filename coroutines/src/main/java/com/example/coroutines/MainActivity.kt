package com.example.coroutines

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil.setContentView
import com.example.coroutines.databinding.ActivityMainBinding
import kotlinx.coroutines.*
import timber.log.Timber

class MainActivity : AppCompatActivity() {

    private lateinit var job: Job
    private lateinit var binding: ActivityMainBinding

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.run {
            binding = setContentView<ActivityMainBinding>(this@MainActivity, R.layout.activity_main)
                .apply {
                    textView.run {
                        buttonLaunch.setOnClickListener {
                            if (this@MainActivity::job.isInitialized && job.isActive) {
                                // cancel the job
                                job.cancel("Job cancelled by user")
                            }
                            text = ""
                            // initialize the job
                            job =
                                CoroutineScope(Dispatchers.Main).launch(CoroutineName("Counter")) {
                                    i("${coroutineContext[CoroutineName.Key]}")
                                    // Loop through 10 down to 0
                                    for (i in 10 downTo 0) {
                                        delay(1000)
                                        append("$i ")
                                    }
                                }.apply {
                                    // invoke on job completion
                                    invokeOnCompletion {
                                        append("\n Completed")
                                        it?.run {
                                            append("\n $message")
                                        }
                                    }
                                }
                        }

                        buttonCancel.setOnClickListener {
                            if (this@MainActivity::job.isInitialized) {
                                if (job.isActive) {
                                    // cancel the job
                                    job.cancel(
                                        "Job cancelled by user"
                                    )
                                } else {
                                    text = "Job is not active"
                                }
                            } else {
                                text = "Job is not initialized"
                            }
                        }
                    }
                }
        }
    }
}
