package com.vht.sdkcore.camera

import com.google.gson.annotations.SerializedName


data class Login(
    @SerializedName("success") val success: String?,
    @SerializedName("retcode") val retcode: String,
    @SerializedName("redirectUrl") val redirectUrl: String?,
    @SerializedName("phone") val phone: String?,
    @SerializedName("limits") val limits: Int,
)


