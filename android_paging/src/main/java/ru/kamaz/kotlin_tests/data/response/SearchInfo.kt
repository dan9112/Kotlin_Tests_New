package ru.kamaz.kotlin_tests.data.response

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import javax.annotation.Generated

@Generated("jsonschema2pojo")
data class SearchInfo(
    @SerializedName("totalhits")
    @Expose
    val totalhits: Int
)
