package com.viettel.vht.main.model.ptz_jf

import com.google.gson.annotations.SerializedName

data class UpdateListPTZRequest(
    val deviceId: String,
    val list: List<ItemPTZ>
)

data class ItemPTZ(
    @SerializedName("id")
    val id: String,
    @SerializedName("label")
    val label: String,
    @SerializedName("pan")
    val pan: Int = 0,
    @SerializedName("tilt")
    val tilt: Int = 0,
    @SerializedName("zoom")
    val zoom: Int = 0,
)