package com.viettel.vht.sdk.model

import com.google.gson.annotations.SerializedName

data class ApiObjectResponse<T>(
    @SerializedName("error") var error: Int,
    @SerializedName("msg") var msg: String,
    @SerializedName("data") var dataResponse: T,
)

data class ApiException(
    @SerializedName("error") var error: String,
    @SerializedName("msg") var msg: String,
    @SerializedName("data") var dataResponse: Any,
    @SerializedName("code") var code: Int,
    @SerializedName("message") var message: String,
    @SerializedName("errorCode") var errorCode: Int
)