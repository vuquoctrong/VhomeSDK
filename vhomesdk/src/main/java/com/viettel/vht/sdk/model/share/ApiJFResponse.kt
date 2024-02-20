package com.viettel.vht.sdk.model.share

import com.google.gson.annotations.SerializedName

data class ApiJFResponse<T>(
    @SerializedName("msg") var msg: String,
    @SerializedName("code") var code: Int,
    @SerializedName("data") var dataResponse: T?
)

data class JFCheckShare(
    @SerializedName("id") var id: String,
    @SerializedName("account") var account: String
)

data class JFItemShare(
    @SerializedName("id") var id: String,

    @SerializedName("uuid") var uuid: String,

    @SerializedName("shareTime") var shareTime: Int,

    @SerializedName("acceptTime") var acceptTime: Int,

    @SerializedName("ret") var ret: Int,

    @SerializedName("powers") var powers: String,

    @SerializedName("account") var account: String,

    @SerializedName("permissions") var permissions: List<String>
)

data class JFItemReceive(
    @SerializedName("id") var id: String,

    @SerializedName("uuid") var uuid: String,

    @SerializedName("phone") var phone: String,

    @SerializedName("ret") var ret: Int,

    @SerializedName("powers") var powers: String,

    @SerializedName("account") var account: String,

    @SerializedName("permissions") var permissions: List<Any>
)