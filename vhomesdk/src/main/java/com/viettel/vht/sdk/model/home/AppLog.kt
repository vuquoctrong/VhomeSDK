package com.viettel.vht.sdk.model.home

import com.google.gson.annotations.SerializedName
import java.io.File

data class RequestGetAppLogUpLoadLink(
    @SerializedName("endpoint") var endpoint: String? = null,
    @SerializedName("userId") var userId: String? = null,
    @SerializedName("logId") var logId: String? = null,
    @SerializedName("logType") var logType: String? = null,
    @SerializedName("startTime") var startTime: Long? = null,
    @SerializedName("stopTime") var stopTime: Long? = null
) {
}

data class GetAppLogUpLoadLinkResponse(
    @SerializedName("data") var data: String? = null,
    @SerializedName("error") var error: Boolean? = null,
    @SerializedName("errorCode") var errorCode: Int? = null,
    @SerializedName("errorName") var errorName: String? = null,
    @SerializedName("status") var status: Int? = null
) {
}

data class AppLogUpLoadStatusResponse(
    @SerializedName("userId") var userId: String? = null,
    @SerializedName("logId") var logId: String? = null,
    @SerializedName("logType") var logType: String? = null,
    @SerializedName("startTime") var startTime: Int? = null,
    @SerializedName("stopTime") var stopTime: Int? = null
) {
}

data class RequestUpLoadAppLog(
    @SerializedName("endpoint") var endpoint: String? = null,
    @SerializedName("userId") var userId: String? = null,
    @SerializedName("logId") var logId: String? = null,
) {
}

data class UpLoadAppLogResponse(
    @SerializedName("data") var data: String? = null,
    @SerializedName("error") var error: Boolean? = null,
    @SerializedName("errorCode") var errorCode: Int? = null,
    @SerializedName("errorName") var errorName: String? = null,
    @SerializedName("status") var status: Int? = null
) {
}