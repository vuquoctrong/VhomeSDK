package com.viettel.vht.sdk.model.login

import com.google.gson.annotations.SerializedName

data class SecretKeyResponse(
    @SerializedName("errorCode")
    val errorCode: Int? = 0,
    @SerializedName("status")
    val status: Int? = 0,
    @SerializedName("errorName")
    val errorName: String = "",
    @SerializedName("data")
    val data: DataSecretKey? = null
)
data class DataSecretKey(
    @SerializedName("accessKey")
    val accessKey: String = "",
    @SerializedName("createDate")
    val createDate: String = "",
    @SerializedName("lastModified")
    val lastModified: String = "",
    @SerializedName("secretKey")
    val secretKey: String = "",
)
