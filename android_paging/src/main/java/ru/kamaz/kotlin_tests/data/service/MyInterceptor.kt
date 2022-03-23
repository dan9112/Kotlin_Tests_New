package ru.kamaz.kotlin_tests.data.service

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Request.Builder
import java.io.IOException

class MyInterceptor : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain) = chain.run {
        request().url.toString().run {
            Log.e("MyInterceptor", this)
            proceed(
                Builder()
                    .url(this)
                    .build()
            )
        }
    }
}
