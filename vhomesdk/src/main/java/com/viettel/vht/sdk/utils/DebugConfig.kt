@file:Suppress("ImplicitThis")

package com.viettel.vht.sdk.utils

import timber.log.Timber


object DebugConfig {
    var SHOW_DEBUG_LOG = false
    val tag = "VHomeSDKManager"

    fun log(TAG: String? = tag, message: String) {
        val str = " $message"
        if (SHOW_DEBUG_LOG) {
            Timber.tag(TAG).d(str)
        }
    }

    fun logd(TAG: String? = tag, message: String) {
        val str = " $message"
        if (SHOW_DEBUG_LOG) {
            Timber.tag(TAG).d(str)
        }
    }


    fun loge(TAG: String? = tag, message: String) {
        if (SHOW_DEBUG_LOG) {
            Timber.tag(TAG).e(message)
        }

    }
    //endregion Log
}