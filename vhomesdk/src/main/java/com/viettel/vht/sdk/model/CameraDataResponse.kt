package com.viettel.vht.sdk.model

import com.google.gson.annotations.SerializedName

data class CameraDataResponse<T>(
    val code: String = "",
    val data: T?,
    val msg: String = "",
    val page: PageResponse = PageResponse(),
    val errorCode: Int = 0
)

data class StorageStatusResponse(
    @SerializedName("storageStatus") var storageStatus: StorageStatus? = StorageStatus(),
    @SerializedName("resultCode") var resultCode: String? = null,
    @SerializedName("resultDes") var resultDes: String? = null
)

data class StorageStatus(
    @SerializedName("result") var result: String? = null,
    @SerializedName("storageList") var storageList: ArrayList<StorageList> = arrayListOf(),
    @SerializedName("formatingRate") var formatingRate: String? = null
)

data class StorageList(
    @SerializedName("capacity") var capacity: Double? = null,
    @SerializedName("status") var status: Int? = null,
    @SerializedName("hdStatus") var hdStatus: Int? = null,
    @SerializedName("healthLevel") var healthLevel: Int? = null,
    @SerializedName("type") var type: Int? = null,
    @SerializedName("firstRecordTime") var firstRecordTime: String? = null,
    @SerializedName("index") var index: Int? = null
)