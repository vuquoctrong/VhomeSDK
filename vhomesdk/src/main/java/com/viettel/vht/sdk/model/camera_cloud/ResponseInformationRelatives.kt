package com.viettel.vht.sdk.model.camera_cloud

import com.google.gson.annotations.SerializedName

data class ResponseInformationRelatives(
    @SerializedName("code")
    var code: Int? = null,

    @SerializedName("error")
    var error: String? = null,

    @SerializedName("data")
    var data: DataInformationRelatives? = null,

    @SerializedName("message")
    var message: String? = null,
)
data class DataInformationRelatives(
    @SerializedName("device_id")
    var deviceId: String? = null,

    @SerializedName("serial")
    var serial: String? = null,

    @SerializedName("camera_model")
    var cameraModel: String? = null,

    @SerializedName("device_name")
    var deviceName: String? = null,

    @SerializedName("user_id")
    var userId: String? = null,
)
