package com.viettel.vht.sdk.ui.jfcameradetail.alarm_voice_period.model

import com.google.gson.annotations.SerializedName


data class AlarmVoicePeriodResponse(
    @SerializedName("Detect.AlarmVoicePeriod")
    val listAlarmVoicePeriod: ArrayList<ArrayList<ArrayList<String>>> = arrayListOf(),
    @SerializedName("Name")
    val name: String? = null,
    @SerializedName("Ret")
    val ret: Int? = null,
    @SerializedName("SessionID")
    val sessionID: String? = null
)
