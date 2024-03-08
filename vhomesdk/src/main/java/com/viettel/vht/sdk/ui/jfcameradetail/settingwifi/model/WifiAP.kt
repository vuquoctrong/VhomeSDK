package com.viettel.vht.sdk.ui.jfcameradetail.settingwifi.model

import com.google.gson.annotations.SerializedName

data class WifiAP(
    @SerializedName("Auth")
    val auth: String,
    @SerializedName("Channel")
    val channel: Long,
    @SerializedName("EncrypType")
    val encryptType: String,
    @SerializedName("NetType")
    val netType: String,
    @SerializedName("RSSI")
    val rssi: String,
    @SerializedName("SSID")
    val ssid: String,
    @SerializedName("nRSSI")
    val nRssi: Long,
) {

    fun getAuth(): Auth {
        return when (auth) {
            "OPEN" -> Auth.OPEN
            "WPA2PSK" -> Auth.WPA2PSK
            else -> Auth.WPA2PSK
        }
    }

    fun getRssi(): Rssi {
        return when (rssi) {
            "Excellent" -> Rssi.Excellent
            "VeryGood" -> Rssi.VeryGood
            "Good" -> Rssi.Good
            "Low" -> Rssi.Low
            "VeryLow" -> Rssi.VeryLow
            else -> Rssi.VeryLow
        }
    }

    sealed class Auth {
        object OPEN : Auth()
        object WPA2PSK : Auth()
    }

    sealed class Rssi {
        object Excellent : Rssi()
        object VeryGood : Rssi()
        object Good : Rssi()
        object Low : Rssi()
        object VeryLow : Rssi()
    }
}
