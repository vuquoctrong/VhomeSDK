package com.viettel.vht.sdk.ui.informationcamera

import android.os.Message
import com.basic.G
import com.google.gson.Gson
import com.lib.EUIMSG
import com.lib.FunSDK
import com.lib.IFunSDKResult
import com.lib.MsgContent
import com.lib.sdk.bean.HandleConfigData
import com.lib.sdk.bean.JsonConfig
import com.lib.sdk.bean.NetworkWifi
import com.lib.sdk.bean.StringUtils
import com.lib.sdk.bean.SystemInfoBean
import com.vht.sdkcore.base.BaseViewModel
import com.vht.sdkcore.utils.Define
import com.vht.sdkcore.utils.SingleLiveEvent
import com.viettel.vht.sdk.jfmanager.JFCameraManager
import dagger.hilt.android.lifecycle.HiltViewModel
import timber.log.Timber
import javax.inject.Inject


@HiltViewModel
class InformationCameraJFViewModel @Inject constructor() : BaseViewModel(), IFunSDKResult {

    lateinit var devId: String
    private var networkWifi: NetworkWifi? = null
    private var systemInfoBean: SystemInfoBean? = null

    private var userId = 0

    val getSystemInfoState = SingleLiveEvent<SystemInfoBean?>().apply { value = systemInfoBean }
    val getNetworkWifiState = SingleLiveEvent<NetworkWifi?>().apply { value = networkWifi }
    var isLANNetworkWifi = false

    init {
        userId = FunSDK.GetId(userId, this)
    }

    fun getConfig() {
        if (!this::devId.isInitialized) return
        isLoading.value = true

        FunSDK.DevGetConfigJsonWithoutCache(
            userId, devId,
            JsonConfig.SYSTEM_INFO,
            -1,
            1020,
            "",
            0, 0, 0
        )
    }

    private fun dealWithSystemInfo(dataJson: String?) {
        try {
            if (dataJson == null) {
                return
            }
            val handleConfigData: HandleConfigData<*> = HandleConfigData<Any?>()
            if (handleConfigData.getDataObj(dataJson, SystemInfoBean::class.java)) {
                systemInfoBean = handleConfigData.obj as SystemInfoBean
                getSystemInfoState.value = systemInfoBean
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun dealWithNetworkWifi(dataJson: String?) {
        try {
            if (dataJson == null) {
                return
            }
            val handleConfigData: HandleConfigData<*> = HandleConfigData<Any?>()
            if (handleConfigData.getDataObj(dataJson, NetworkWifi::class.java)) {
                networkWifi = handleConfigData.obj as NetworkWifi
                getNetworkWifiState.value = networkWifi
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun OnFunSDKResult(message: Message, msgContent: MsgContent): Int {
        Timber.tag(TAG).d("OnFunSDKResult")
        Timber.tag(TAG).d("message: ${message.what} - $message")
        Timber.tag(TAG).d("msgContent: ${Gson().toJson(msgContent)}")
        when (message.what) {
            EUIMSG.DEV_GET_JSON -> {
                val jsonData = G.ToString(msgContent.pData)
                if (StringUtils.contrast(msgContent.str, JsonConfig.SYSTEM_INFO)) {
                    dealWithSystemInfo(jsonData)
                    JFCameraManager.getWifiRouteSignalLevel(devId) { _, isLAN ->
                        isLANNetworkWifi = isLAN
                        FunSDK.DevGetConfigByJson(
                            userId, devId,
                            JsonConfig.NETWORK_WIFI,
                            1024,
                            -1,
                            Define.GET_SET_DEV_CONFIG_TIMEOUT,
                            0
                        )
                    }
                } else if (StringUtils.contrast(msgContent.str, JsonConfig.NETWORK_WIFI)) {
                    dealWithNetworkWifi(jsonData)
                }
                isLoading.value = false
            }

            EUIMSG.DEV_SET_JSON -> {
                isLoading.value = false
            }

            else -> Unit
        }
        return 0
    }

    override fun onCleared() {
        super.onCleared()
        FunSDK.UnRegUser(userId)
    }

    companion object {
        private val TAG: String = InformationCameraJFViewModel::class.java.simpleName
    }
}
