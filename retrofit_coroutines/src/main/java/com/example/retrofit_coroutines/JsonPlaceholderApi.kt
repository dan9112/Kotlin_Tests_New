package com.example.retrofit_coroutines

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface JsonPlaceholderApi {
    @GET("posts")
    fun getPosts(): Call<List<Post>>

    @GET("posts")
    fun getPostsByUserId(@Query("userId") id: Int): Call<List<Post>>

    @GET("comments")
    fun getComments(): Call<List<Comment>>

    @GET("users")
    fun getUsers(): Call<List<User>>
}