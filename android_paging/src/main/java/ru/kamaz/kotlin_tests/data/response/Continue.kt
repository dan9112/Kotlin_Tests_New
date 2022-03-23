package ru.kamaz.kotlin_tests.data.response

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import javax.annotation.Generated

@Generated("jsonschema2pojo")
data class Continue(
    @SerializedName("sroffset")
    @Expose
    var sroffset: Int = 100,// [1, 500]

    @SerializedName("continue")
    @Expose
    var `continue`: String? = null
)

