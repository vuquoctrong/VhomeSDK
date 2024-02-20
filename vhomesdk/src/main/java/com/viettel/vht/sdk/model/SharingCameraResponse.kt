package com.viettel.vht.sdk.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

data class SharingCameraResponse(
    @SerializedName("data")
    var data: List<ItemSharingCamera>? = null,
    @SerializedName("error")
    val error: Boolean,
    @SerializedName("errorCode")
    var errorCode: Int,
    @SerializedName("errorName")
    val errorName: String = "",
    @SerializedName("status")
    val status: Int
)

@Parcelize
data class ItemSharingCamera(
    @SerializedName("camModel")
    val camModel: String? = "",
    @SerializedName("camName")
    val camName: String? = "",
    @SerializedName("camManufacturer")
    val camManufacturer: String? = "",
    @SerializedName("createDate")
    val createDate: Long? = 0,
    @SerializedName("lastModified")
    val lastModified: Long? = 0,
    @SerializedName("sharedDate")
    val sharedDate: Long? = 0,
    @SerializedName("connectionStatus")
    val connectionStatus: Int? = 1,
    @SerializedName("deviceId")
    val deviceId: String = "",
    @SerializedName("sharedFeatureList")
    val sharedFeatureList: List<String> = listOf(),
    @SerializedName("sharedUserId")
    val sharedUserId: String? = "",
    @SerializedName("sharedUserPhone")
    val sharedUserPhone: String? = "",
    @SerializedName("sharedUsername")
    val sharedUsername: String? = "",
    @SerializedName("sharingStatus")
    val sharingStatus: String? = "",
    @SerializedName("userId")
    val userId: String? = "",
    @SerializedName("userPhone")
    val userPhone: String? = "",
) : Parcelable
