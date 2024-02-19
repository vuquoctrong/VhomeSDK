package com.vht.sdkcore.camera

import java.io.Serializable

data class DetailDeviceCameraResponse(
    val deviceSerial: String? = "",
    val deviceName: String? = "",
    val nickName: String? = "",
    val model: String? = "",
    val deviceType: String? = "",
    val status: Int? = 0,
    val defence: Int? = 0,
    val deviceVersion: String? = "",
    val updateTime: String? = "",
    val isEncrypt: Int? = 0,
    val alarmSoundMode: Int? = 0,
    val offlineNotify: Int? = 0,
    val secondDeviceType: String? = ""
) : Serializable