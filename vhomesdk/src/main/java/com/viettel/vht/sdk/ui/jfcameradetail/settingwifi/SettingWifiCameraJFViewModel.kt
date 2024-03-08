package com.viettel.vht.sdk.ui.jfcameradetail.settingwifi

import android.os.Message
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
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
import com.manager.device.DeviceManager
import com.vht.sdkcore.base.BaseViewModel
import com.vht.sdkcore.utils.Define
import com.vht.sdkcore.utils.SingleLiveEvent
import com.viettel.vht.sdk.jfmanager.JFCameraManager
import com.viettel.vht.sdk.ui.jfcameradetail.settingwifi.model.WifiAP
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class SettingWifiCameraJFViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : BaseViewModel(), IFunSDKResult {

    private val devId: String
    private val deviceManager: DeviceManager
    private var userId = 0
    var networkWifi: NetworkWifi? = null

    private val wifiList = mutableListOf<WifiAP>()
    val wifiListLiveData = MutableLiveData<List<WifiAP>>()
    val isLoadingWifiList = MutableLiveData(false)
    val isConnectingWifi = MutableLiveData(false)
    val isConnectWifiSuccess = SingleLiveEvent<Boolean>()
    val wifiRouteSignalLevel = SingleLiveEvent<Pair<Int, Boolean>>()


    init {
        userId = FunSDK.GetId(userId, this)
        deviceManager = DeviceManager.getInstance()
        devId = savedStateHandle[Define.BUNDLE_KEY.PARAM_ID] ?: ""
        getWifiInfo()
        viewModelScope.launch {
            repeat(Int.MAX_VALUE - 1) {
                getWifiRouteSignalLevel()
                delay(10_000L)
            }
        }
    }

    override fun OnFunSDKResult(message: Message, msgContent: MsgContent): Int {
        Timber.tag(TAG).d("OnFunSDKResult")
        Timber.tag(TAG).d("message: ${message.what} - $message")
        Timber.tag(TAG).d("msgContent: ${Gson().toJson(msgContent)}")
        when (message.what) {
            EUIMSG.DEV_GET_JSON -> {
                if (StringUtils.contrast(msgContent.str, JsonConfig.NETWORK_WIFI)) {
                    try {
                        val jsonData = G.ToString(msgContent.pData)
                        val handleConfigData: HandleConfigData<*> = HandleConfigData<Any?>()
                        if (handleConfigData.getDataObj(jsonData, NetworkWifi::class.java)) {
                            networkWifi = handleConfigData.obj as NetworkWifi
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        getWifiList()
                    }
                }
            }

            EUIMSG.DEV_CMD_EN -> {
                try {
                    wifiList.clear()
                    JSONObject(G.ToString(msgContent.pData))
                        .takeIf { it.has(WIFI_AP) }
                        ?.getJSONObject(WIFI_AP)
                        ?.takeIf { it.has(WIFI_AP) }
                        ?.getJSONArray(WIFI_AP)
                        ?.let {
                            val gson = Gson()
                            for (i in 0 until it.length()) {
                                try {
                                    val wifi = gson.fromJson(it[i].toString(), WifiAP::class.java)
                                    if (wifi.ssid == networkWifi?.ssid) continue
                                    wifiList.add(wifi)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                            wifiList.sortWith(
                                compareByDescending<WifiAP> { wifi -> wifi.nRssi }.thenBy { wifi -> wifi.ssid }
                            )
                        }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    isLoadingWifiList.value = false
                    wifiListLiveData.value = wifiList
                }
            }

            EUIMSG.DEV_SET_WIFI_CFG -> {
                isConnectingWifi.value = false
                isConnectWifiSuccess.value = message.arg1 >= 0
            }

            else -> Unit
        }

        return 0
    }

    override fun onCleared() {
        super.onCleared()
        FunSDK.UnRegUser(userId)
    }

    private fun getWifiInfo() {
        isLoadingWifiList.value = true
        FunSDK.DevGetConfigByJson(
            userId, devId, JsonConfig.NETWORK_WIFI, 1024,
            -1, Define.GET_SET_DEV_CONFIG_TIMEOUT, 0
        )
    }

    private fun getWifiList() {
        FunSDK.DevCmdGeneral(userId, devId, 1020, WIFI_AP, 0, TIMEOUT_GET_WIFI_LIST, null, 0, -1)
    }

    private fun getWifiRouteSignalLevel() {
        JFCameraManager.getWifiRouteSignalLevel(devId) { signalLevel, isLAN ->
            viewModelScope.launch(Dispatchers.Main) {
                wifiRouteSignalLevel.value = signalLevel to isLAN
            }
        }
    }

    fun refreshWifiList() {
        getWifiInfo()
        getWifiRouteSignalLevel()
    }

    fun connectWifi(wifiSsid: String?, password: String?) {
        isConnectingWifi.value = true
        FunSDK.DevStartWifiConfig(userId, devId, wifiSsid, password, TIMEOUT_CONNECT_WIFI)
    }

    companion object {
        private const val TAG = "SettingWifiCameraJFViewModel"
        private const val WIFI_AP = "WifiAP"
        private const val TIMEOUT_CONNECT_WIFI = 20000
        private const val TIMEOUT_GET_WIFI_LIST = 20000
    }
}
