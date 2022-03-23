package ru.kamaz.kotlin_tests.data.response

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import javax.annotation.Generated

@Generated("jsonschema2pojo")
data class Query(
    @SerializedName("searchinfo")
    @Expose
    var searchinfo: SearchInfo,

    @SerializedName("search")
    @Expose
    var search: List<Search>
)
