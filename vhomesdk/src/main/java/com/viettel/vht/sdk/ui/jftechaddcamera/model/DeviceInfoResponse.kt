package com.viettel.vht.sdk.ui.jftechaddcamera.model

import com.google.gson.annotations.SerializedName

data class DeviceInfoResponse(
    @SerializedName("NetWork.NetCommon")
    val deviceInfo: DeviceInfo? = null,
)

data class DeviceInfo(
    @SerializedName("AuxFunctionMask")
    val auxFunctionMask: Int? = null,
    @SerializedName("BuildDate")
    val buildDate: String? = null,
    @SerializedName("ChannelNum")
    val channelNum: Int? = null,
    @SerializedName("DeviceType")
    val deviceType: Int? = null,
    @SerializedName("GateWay")
    val gateWay: String? = null,
    @SerializedName("HostIP")
    val hostIP: String? = null,
    @SerializedName("HostName")
    val hostName: String? = null,
    @SerializedName("HttpPort")
    val httpPort: Int? = null,
    @SerializedName("MAC")
    val mac: String? = null,
    @SerializedName("MonMode")
    val monMode: String? = null,
    @SerializedName("NetConnectState")
    val netConnectState: Int? = null,
    @SerializedName("OtherFunction")
    val otherFunction: String? = null,
    @SerializedName("SN")
    val sn: String? = null,
    @SerializedName("SSLPort")
    val sslPort: String? = null,
    @SerializedName("Submask")
    val submask: String? = null,
    @SerializedName("TCPMaxConn")
    val TCPMaxConn: Int? = null,
    @SerializedName("TCPPort")
    val tcpPort: String? = null,
    @SerializedName("UDPPort")
    val udpPort: String? = null,
    @SerializedName("UseHSDownLoad")
    val useHSDownLoad: Boolean? = null,
    @SerializedName("Version")
    val version: String? = null,
)