package com.viettel.vht.sdk.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class DataDeviceResponse(
    @SerializedName("name")
    val name: String = "",
    @SerializedName("deviceid")
    val deviceId: String = "",
    @SerializedName("apikey")
    val apiKey: String = "",
    @SerializedName("extra")
    val extra: DetailThingResponse = DetailThingResponse(),
    @SerializedName("brandName")
    val brandName: String = "",
    @SerializedName("brandLogo")
    val brandLogo: String = "",
    @SerializedName("showBrand")
    val showBrand: Boolean = false,
    @SerializedName("productModel")
    val productModel: String = "",
    @SerializedName("devConfig")
    val devConfig: Any = "",
    @SerializedName("family")
    val family: FamilyHomeResponse = FamilyHomeResponse(),
    @SerializedName("sharedBy")
    val sharedBy: ShareByResponse = ShareByResponse(),
    @SerializedName("devicekey")
    val deviceKey: String = "",
    @SerializedName("online")
    var online: Boolean = false,
    @SerializedName("params")
    val params: InforParamResponse = InforParamResponse(),
    val trigTime: String = ""
) : Serializable