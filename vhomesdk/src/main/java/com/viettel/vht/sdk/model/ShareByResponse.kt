package com.viettel.vht.sdk.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ShareByResponse(
    @SerializedName("apikey")
    val apiKey: String = "",
    @SerializedName("email")
    val email: String = "",
    @SerializedName("permit")
    val permit: String = "",
    @SerializedName("shareTime")
    val shareTime: Long = 0
) : Serializable