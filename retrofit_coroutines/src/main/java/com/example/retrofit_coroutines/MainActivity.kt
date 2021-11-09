package com.example.retrofit_coroutines

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import androidx.databinding.DataBindingUtil
import com.example.retrofit_coroutines.databinding.ActivityMainBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.moshi.MoshiConverterFactory

class MainActivity : AppCompatActivity() {
private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main).apply {
            button.setOnClickListener {
                val retrofit =
                    Retrofit.Builder().addConverterFactory(GsonConverterFactory.create())
                        .baseUrl("https://jsonplaceholder.typicode.com/")
                        .build()

                val jsonPlaceholderApi = retrofit.create(JsonPlaceholderApi::class.java)

                val call = jsonPlaceholderApi.getUsers()

                call.enqueue(object : Callback<List<User>> {
                    override fun onFailure(call: Call<List<User>>, t: Throwable) {
                        Log.e("Retrofit", t.message.toString())
                    }

                    override fun onResponse(
                        call: Call<List<User>>,
                        response: Response<List<User>>
                    ) {
                        val comments: List<User> = response.body()!!

                        val stringBuilder = StringBuilder()

                        for (comment in comments) {
                            stringBuilder.run {
                                append(comment.id)
                                append("\n")
                                append(comment.name)
                                append("\n")
                                append(comment.email_user)
                                append("\n\n")
                            }
                        }
                        infoText.text = stringBuilder
                    }
                })
            }
            infoText.movementMethod = ScrollingMovementMethod()
        }
    }
}
