package com.vht.sdkcore.camera

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize


data class CameraInfoResponse(
    @SerializedName("code") val code: Int,
    @SerializedName("msg") val msg: String,
    @SerializedName("data") val data: List<CameraInfo>? = listOf(),
    @SerializedName("page") val page: Page,
)

@Parcelize
data class CameraInfo(
    @SerializedName("channelName") val channelName: String = "",
    @SerializedName("channelNo") val channelNo: Int = 0,
    @SerializedName("deviceSerial") val deviceSerial: String = "",
    @SerializedName("isEncrypt") val isEncrypt: Int = 0,
    @SerializedName("isShared") val isShared: String = "",
    @SerializedName("permission") val permission: Int = 0,
    @SerializedName("picUrl") val picUrl: String = "",
    @SerializedName("status") val status: Int = 0,
    @SerializedName("videoLevel") var videoLevel: Int = 0
) : Parcelable

data class Page(
    @SerializedName("page") val page: Int,
    @SerializedName("size") val size: Int,
    @SerializedName("total") val total: Int
)

