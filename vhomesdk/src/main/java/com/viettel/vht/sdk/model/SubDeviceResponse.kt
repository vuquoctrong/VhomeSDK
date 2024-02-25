package com.viettel.vht.sdk.model

import java.io.Serializable

data class SubDeviceResponse(
    val subDevId: String = "",
    var deviceid: String = "",
    val uiid: String = ""
):Serializable