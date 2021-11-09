package com.example.retrofit_coroutines

import com.squareup.moshi.Json

data class Comment(
    val id: Int,
    val name: String,
    @Json(name = "email")
    val email_user: String,
    val body: String
)
