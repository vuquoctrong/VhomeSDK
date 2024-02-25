package com.viettel.vht.sdk.model

import java.io.Serializable

data class InforParamResponse(
        val bindInfos: Any = "",
        val subDevId: String = "",
        val parentid: String = "",
        val motion: Int = -1,
        val trigTime: Long = 0,
        val battery: Int = -1,
        val lock: Int = -1,
        val version: Int = 0,
        val subDevNum: Int = -1,
        val subDevMaxNum: Int = -1,
        val sledOnline: String="",
        val zled: String="",
        val fwVersion: String="",
        val subDevices: List<SubDeviceResponse> = arrayListOf()
) : Serializable