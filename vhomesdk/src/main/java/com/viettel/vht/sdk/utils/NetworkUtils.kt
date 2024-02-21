package com.viettel.vht.sdk.utils

import android.content.Context
import android.net.wifi.WifiManager
import com.vht.sdkcore.utils.dialog.CommonAlertDialogNotification
import com.vht.sdkcore.utils.dialog.DialogType
fun Context.isConnectedToWifiBand2400Hz(onSuccess: () -> Unit) {
    val wifiManager = getSystemService(Context.WIFI_SERVICE) as WifiManager

    if (wifiManager.isWifiEnabled) {
        when (wifiManager.connectionInfo.frequency) {
            in 2400..2499 -> onSuccess.invoke()
            else -> {
                CommonAlertDialogNotification.getInstanceCommonAlertdialog(this)
                    .showDialog()
                    .setDialogTitleWithString(getString(com.vht.sdkcore.R.string.wifi_connect_may_not_be_on_band_support))
                    .setContent(getString(com.vht.sdkcore.R.string.please_check_again_to_ensure_connection))
                    .showCenterImage(DialogType.ERROR_CONFIG)
                    .setTextNegativeButtonWithString(getString(com.vht.sdkcore.R.string.cancel_text))
                    .setOnNegativePressed {
                        it.dismiss()
                    }
                    .setTextPositiveButtonWithString(getString(com.vht.sdkcore.R.string.continue_text))
                    .setOnPositivePressed {
                        it.dismiss()
                        onSuccess.invoke()
                    }
            }
        }
    }
}