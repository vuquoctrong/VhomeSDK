package com.vht.sdkcore.camera

import com.google.gson.annotations.SerializedName


data class InfoEzvizResponse(
    @SerializedName("ezviztoken") val ezviztoken: String = "",
    @SerializedName("area_domain") val areaDomain: String = "",
    @SerializedName("expire_time") val expireTime: String = "",
)


