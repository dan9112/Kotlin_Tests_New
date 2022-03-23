package ru.kamaz.kotlin_tests.data.service

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import ru.kamaz.kotlin_tests.data.response.WikiArticlesSearchResponse

interface WikiArticleService {
    @GET("w/api.php")
    suspend fun searchArticles(
        @Query(value = "action") action: String = "query",
        @Query(value = "format") format: String = "json",
        @Query(value = "prop") prop: String = "revisions",
        @Query(value = "list") list: String = "search",
        @Query(value = "formatversion") formatVersions: String = "latest",
        @Query(value = "rvprop") rvProp: String = "timestamp|content",
        @Query(value = "srsearch") searchString: String,
        @Query(value = "srlimit") searchLimit: Int,
        @Query(value = "sroffset") searchOffset: Int
    ): WikiArticlesSearchResponse

    companion object {
        private const val BASE_URL = "https://en.wikipedia.org/"

        fun create(): WikiArticleService {
            val client = OkHttpClient.Builder().addInterceptor(MyInterceptor()).build()
            return Retrofit.Builder().baseUrl(BASE_URL).client(client)
                .addConverterFactory(GsonConverterFactory.create()).build()
                .create(WikiArticleService::class.java)
        }
    }
}
