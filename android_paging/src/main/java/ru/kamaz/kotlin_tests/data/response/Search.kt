package ru.kamaz.kotlin_tests.data.response

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import javax.annotation.Generated

@Generated("jsonschema2pojo")
data class Search(
    @SerializedName("ns")
    @Expose
    var ns: Int,

    @SerializedName("title")
    @Expose
    var title: String,

    @SerializedName("pageid")
    @Expose
    var pageid: Int,

    @SerializedName("size")
    @Expose
    var size: Int,

    @SerializedName("wordcount")
    @Expose
    var wordcount: Int,

    @SerializedName("snippet")
    @Expose
    var snippet: String?,

    @SerializedName("timestamp")
    @Expose
    var timestamp: String
)
