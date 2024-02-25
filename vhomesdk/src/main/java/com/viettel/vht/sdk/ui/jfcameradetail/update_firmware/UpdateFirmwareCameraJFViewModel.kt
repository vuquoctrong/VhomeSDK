package com.viettel.vht.sdk.ui.jfcameradetail.update_firmware

import android.os.Message
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.basic.G
import com.google.gson.Gson
import com.lib.ECONFIG
import com.lib.EFUN_ERROR
import com.lib.EUIMSG
import com.lib.FunSDK
import com.lib.IFunSDKResult
import com.lib.MsgContent
import com.lib.sdk.bean.HandleConfigData
import com.lib.sdk.bean.JsonConfig
import com.lib.sdk.bean.StringUtils
import com.lib.sdk.bean.SystemInfoBean
import com.vht.sdkcore.base.BaseViewModel
import com.vht.sdkcore.pref.AppPreferences.Companion.PREF_RECOMMEND_UPDATE_FIRMWARE_JF
import com.vht.sdkcore.pref.RxPreferences
import com.vht.sdkcore.utils.Define
import com.vht.sdkcore.utils.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class UpdateFirmwareCameraJFViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val rxPreferences: RxPreferences
) : BaseViewModel(), IFunSDKResult {

    var devId: String?

    private var systemInfo: SystemInfoBean? = null

    private var userId = 0

    private var networkConnectTypeDev = 0
    private var checkUpdateVersion = 0

    private var isUpdateFirmwareImmediately = false

    val isCanUpdateFirmware = MutableLiveData(false)

    val currentVersion = MutableLiveData<String>()
    val latestVersion = MutableLiveData<String>()

    val updateState = SingleLiveEvent<Pair<Int, Int>>()

    private var autoUpdateFirmwareJSON = JSONObject()
    val autoUpdateFirmwareState = MutableLiveData<Boolean?>(null)
    val isUpdateAutoUpdateFirmwareSuccess = SingleLiveEvent<Boolean>()

    val isShowDialogRecommendUpdateFirmware = SingleLiveEvent<Boolean>()

    init {
        userId = FunSDK.GetId(userId, this)
        devId = savedStateHandle.get<String>(Define.BUNDLE_KEY.PARAM_DEVICE_SERIAL)
        isUpdateFirmwareImmediately =
            savedStateHandle.get<Boolean>(Define.BUNDLE_KEY.PARAM_IS_UPDATE_FIRMWARE_IMMEDIATELY)
                ?: false
    }

    fun getConfig() {
        devId?.let {
            isLoading.value = true
            checkHasNewFirmware()
            FunSDK.DevGetConfigJsonWithoutCache(
                userId, devId,
                JsonConfig.SYSTEM_INFO,
                -1,
                1020,
                "",
                0, 0, 0
            )
            FunSDK.DevGetConfigByJson(
                userId,
                devId,
                NETWORK_AUTO_UPGRADE_IMP,
                1024,
                -1,
                Define.GET_SET_DEV_CONFIG_TIMEOUT,
                0
            )
        }
    }

    fun checkHasNewFirmware() {
        FunSDK.DevGetConnectType(userId, devId, 0)
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
                }
                if (StringUtils.contrast(msgContent.str, NETWORK_AUTO_UPGRADE_IMP)) {
                    autoUpdateFirmwareJSON = JSON.parseObject(jsonData) ?: JSONObject()
                    autoUpdateFirmwareState.value = try {
                        autoUpdateFirmwareJSON.getBoolean(NETWORK_AUTO_UPGRADE_IMP)
                    } catch (e: Exception) {
                        false
                    }
                }
                isLoading.value = false
            }

            EUIMSG.DEV_SET_JSON -> {
                if (StringUtils.contrast(msgContent.str, NETWORK_AUTO_UPGRADE_IMP)) {
                    val currentAutoUpdateFirmwareState = autoUpdateFirmwareState.value ?: false
                    if (message.arg1 >= 0) {
                        autoUpdateFirmwareState.value = !currentAutoUpdateFirmwareState
                        isUpdateAutoUpdateFirmwareSuccess.value = true
                    } else {
                        autoUpdateFirmwareState.value = currentAutoUpdateFirmwareState
                        isUpdateAutoUpdateFirmwareSuccess.value = false
                    }
                    autoUpdateFirmwareJSON[NETWORK_AUTO_UPGRADE_IMP] = autoUpdateFirmwareState.value
                }
                isLoading.value = false
            }

            EUIMSG.DEV_CHECK_UPGRADE -> {
                try {
                    val json = JSON.parseObject(msgContent.str) ?: JSONObject()
                    if (json.containsKey("FileVer")) {
                        latestVersion.value = json.getString("FileVer")
                    } else if (json.containsKey("FileName")) {
                        latestVersion.value = json.getString("FileName")
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                if (message.arg1 > 0) {
                    checkUpdateVersion = message.arg1
                    if (isUpdateFirmwareImmediately) {
                        updateFirmware()
                    }
                }
                isCanUpdateFirmware.value = message.arg1 > 0
                isLoading.value = false
            }

            EUIMSG.DEV_START_UPGRADE -> {
                if (message.arg1 < 0 && message.arg1 != EFUN_ERROR.EE_MNETSDK_IS_UPGRADING) {
                    updateState.value = UPDATE_FAILED to -1
                } else {
                    updateState.value = UPDATE_STATE_START to 0
                }
                isLoading.value = false
            }

            EUIMSG.DEV_ON_UPGRADE_PROGRESS -> {
                onUpgradeProgress(message)
            }

            EUIMSG.DEV_GET_CONNECT_TYPE -> {
                if (message.arg1 >= 0) {
                    networkConnectTypeDev = message.arg1
                    isLoading.value = true
                    FunSDK.DevCheckUpgradeAllNet(userId, devId, 0)
                }
            }

            else -> Unit
        }
        return 0
    }

    private fun dealWithSystemInfo(dataJson: String?) {
        try {
            if (dataJson == null) {
                return
            }
            val handleConfigData: HandleConfigData<*> = HandleConfigData<Any?>()
            if (handleConfigData.getDataObj(dataJson, SystemInfoBean::class.java)) {
                systemInfo = handleConfigData.obj as SystemInfoBean
                currentVersion.value = systemInfo?.softWareVersion ?: ""
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun onUpgradeProgress(message: Message) {
        when (message.arg1) {
            ECONFIG.EUPGRADE_STEP_DOWN -> {
                if (message.arg2 in 0..100) {
                    updateState.value = UPDATE_STATE_DOWNLOAD to message.arg2
                }
            }

            ECONFIG.EUPGRADE_STEP_UPGRADE -> {
                if (message.arg2 in 0..100) {
                    updateState.value = UPDATE_STATE_UPGRADE to message.arg2
                }
            }

            ECONFIG.EUPGRADE_STEP_COMPELETE -> {
                if (message.arg2 >= 0) {
                    viewModelScope.launch {
                        delay(5000)
                        updateState.value = UPDATE_STATE_COMPLETED to message.arg2
                        rxPreferences.remove(PREF_RECOMMEND_UPDATE_FIRMWARE_JF + devId)
                        isCanUpdateFirmware.value = false
                        currentVersion.value = latestVersion.value
                        checkUpdateVersion = 0
                    }
                }
            }
        }
    }

    fun updateFirmware() {
        devId?.let {
            if (checkUpdateVersion > 0) {
                isLoading.value = true
                FunSDK.DevStartUpgrade(userId, devId, checkUpdateVersion, 0)
            }
        }
    }

    fun setStateAutoUpdateFirmware(state: Boolean) {
        devId?.let {
            isLoading.value = true
            autoUpdateFirmwareJSON[NETWORK_AUTO_UPGRADE_IMP] = state
            FunSDK.DevSetConfigByJson(
                userId,
                devId,
                NETWORK_AUTO_UPGRADE_IMP,
                autoUpdateFirmwareJSON.toString(),
                -1,
                Define.GET_SET_DEV_CONFIG_TIMEOUT,
                0
            )
        }
    }

    fun checkShowRecommendUpdateDialog() {
        viewModelScope.launch {
            val local = rxPreferences.get(PREF_RECOMMEND_UPDATE_FIRMWARE_JF + devId)
            if (local.isNullOrEmpty()) {
                withContext(Dispatchers.Main) {
                    isShowDialogRecommendUpdateFirmware.value = true
                }
            } else {
                try {
                    val localRecommendUpdateDialogModel =
                        Gson().fromJson(local, RecommendUpdateDialogModel::class.java)
                    if (
                        (localRecommendUpdateDialogModel.nameVersion != latestVersion.value) ||
                        (System.currentTimeMillis() - localRecommendUpdateDialogModel.lastTime > 7 * 24 * 60 * 60 * 1000)
                    ) {
                        withContext(Dispatchers.Main) {
                            isShowDialogRecommendUpdateFirmware.value = true
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun saveThisTimeShowDialogRecommendUpdate() {
        viewModelScope.launch {
            val recommendUpdateDialogModel = RecommendUpdateDialogModel(
                latestVersion.value ?: "",
                System.currentTimeMillis()
            )
            rxPreferences.put(
                PREF_RECOMMEND_UPDATE_FIRMWARE_JF + devId,
                Gson().toJson(recommendUpdateDialogModel)
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        FunSDK.UnRegUser(userId)
    }

    companion object {
        private val TAG: String = UpdateFirmwareCameraJFViewModel::class.java.simpleName
        const val UPDATE_FAILED = -1
        const val UPDATE_STATE_START = 0
        const val UPDATE_STATE_DOWNLOAD = 1
        const val UPDATE_STATE_UPGRADE = 2
        const val UPDATE_STATE_COMPLETED = 3

        private const val NETWORK_AUTO_UPGRADE_IMP = "NetWork.OnlineUpgrade.AutoUpgradeImp"
    }
}

data class RecommendUpdateDialogModel(
    val nameVersion: String,
    val lastTime: Long
)
