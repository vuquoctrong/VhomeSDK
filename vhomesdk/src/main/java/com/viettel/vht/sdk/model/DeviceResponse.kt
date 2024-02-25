package com.viettel.vht.sdk.model

import com.google.gson.annotations.SerializedName

data class DeviceResponse(
    @SerializedName("thingList")
    var thingList: List<ThingResponse> = arrayListOf(),
    @SerializedName("total")
    val total: Int = 0,
    @SerializedName("offset")
    val offset: Int = 0,
    @SerializedName("limit")
    val limit: Int = 0,
    @SerializedName("devices")
    var devices: ArrayList<DeviceDataResponse> = arrayListOf()
)