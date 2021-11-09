package com.example.retrofit_coroutines

import com.google.gson.annotations.SerializedName

data class User(
    val id: Int,
    val name: String,
    @SerializedName("email")
    val email_user: String,
    val username: String
)