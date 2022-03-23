package ru.kamaz.kotlin_tests.data.response

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

import javax.annotation.Generated

@Generated("jsonschema2pojo")
class WikiArticlesSearchResponse(
    @SerializedName("batchcomplete")
    @Expose
    var batchcomplete: Boolean = true,

    @SerializedName("continue")
    @Expose
    var _continue: Continue? = null,

    @SerializedName("query")
    @Expose
    var query: Query
)
