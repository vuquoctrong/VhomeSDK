package com.viettel.vht.sdk.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class DetailThingResponse(
        @SerializedName("mac")
        val mac: String = "",
        @SerializedName("apmac")
        val apMac: String = "",
        @SerializedName("model")
        val model: String = "",
        @SerializedName("description")
        val description: String = "",
        @SerializedName("modelInfo")
        val modelInfo: String = "",
        @SerializedName("manufacturer")
        val manufacturer: String = "",
        @SerializedName("brandId")
        val brandId: String = "",
        @SerializedName("uiid")
        val uiId: Int = 0,
        @SerializedName("ui")
        val ui: String = ""
) : Serializable