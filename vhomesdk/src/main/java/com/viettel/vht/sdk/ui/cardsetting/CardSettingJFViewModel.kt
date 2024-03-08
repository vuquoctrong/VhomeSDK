package com.viettel.vht.sdk.ui.cardsetting

import android.os.Message
import androidx.lifecycle.viewModelScope
import com.basic.G
import com.google.gson.Gson
import com.lib.EUIMSG
import com.lib.FunSDK
import com.lib.IFunSDKResult
import com.lib.MsgContent
import com.lib.SDKCONST
import com.lib.sdk.bean.AlarmInfoBean
import com.lib.sdk.bean.HandleConfigData
import com.lib.sdk.bean.JsonConfig
import com.lib.sdk.bean.OPStorageManagerBean
import com.lib.sdk.bean.RecordParamBean
import com.lib.sdk.bean.StorageInfoBean
import com.lib.sdk.bean.StringUtils
import com.manager.device.DeviceManager
import com.vht.sdkcore.base.BaseViewModel
import com.vht.sdkcore.utils.Define
import com.vht.sdkcore.utils.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject


@Suppress("UNCHECKED_CAST")
@HiltViewModel
class CardSettingJFViewModel @Inject constructor() : BaseViewModel(), IFunSDKResult {

    lateinit var devId: String
    private var storageInfoBeanList: List<StorageInfoBean>? = null
    private val deviceManager: DeviceManager

    private var userId = 0
    private var formatCount = 0

    val storageInfoBeanListLiveData = SingleLiveEvent<List<StorageInfoBean>?>()
    val formatState = SingleLiveEvent<Int>()

    private var recordParamBean: RecordParamBean? = null
    private var alarmInfoBean: AlarmInfoBean? = null

    val recordModeState = SingleLiveEvent<RecordMode>().apply { value = RecordMode.Closed }

    init {
        userId = FunSDK.GetId(userId, this)
        deviceManager = DeviceManager.getInstance()
    }

    fun getConfig() {
        if (!this::devId.isInitialized) return
        isLoading.value = true
        FunSDK.DevGetConfigByJson(
            userId, devId,
            JsonConfig.STORAGE_INFO,
            1024,
            -1,
            Define.GET_SET_DEV_CONFIG_TIMEOUT,
            0
        )
    }

    private fun getRecordMode() {
        if (!this::devId.isInitialized) return
        isLoading.value = true
        FunSDK.DevGetConfigByJson(
            userId, devId,
            JsonConfig.RECORD,
            1024,
            0,
            Define.GET_SET_DEV_CONFIG_TIMEOUT,
            0
        )
        FunSDK.DevGetConfigByJson(
            userId, devId,
            JsonConfig.DETECT_MOTIONDETECT,
            1024,
            0,
            Define.GET_SET_DEV_CONFIG_TIMEOUT,
            0
        )
    }

    override fun OnFunSDKResult(message: Message, msgContent: MsgContent): Int {
        Timber.tag(TAG).d("OnFunSDKResult")
        Timber.tag(TAG).d("message: ${message.what} - $message")
        Timber.tag(TAG).d("msgContent: ${Gson().toJson(msgContent)}")
        when (message.what) {
            EUIMSG.DEV_GET_JSON -> {
                isLoading.value = false
                val jsonData = G.ToString(msgContent.pData) ?: return 0
                Timber.tag(TAG).d("jsonData: $jsonData")
                if (StringUtils.contrast(msgContent.str, JsonConfig.STORAGE_INFO)) {
                    val handleConfigData: HandleConfigData<*> = HandleConfigData<Any?>()
                    if (handleConfigData.getDataObj(jsonData, StorageInfoBean::class.java)) {
                        storageInfoBeanList = handleConfigData.obj as? List<StorageInfoBean>
                        storageInfoBeanListLiveData.value = storageInfoBeanList
                        getRecordMode()
                    }
                }
                if (StringUtils.contrast(msgContent.str, JsonConfig.RECORD)) {
                    val handleConfigData: HandleConfigData<*> = HandleConfigData<Any?>()
                    if (handleConfigData.getDataObj(jsonData, RecordParamBean::class.java)) {
                        recordParamBean = handleConfigData.obj as? RecordParamBean
                        when (recordParamBean?.RecordMode) {
                            RECORD_MODE_CONFIG -> {
                                val mask = try {
                                    G.getIntFromHex(recordParamBean?.Mask?.get(0)?.get(0))
                                } catch (e: Exception) {
                                    0
                                }
                                if (mask == 7) {
                                    recordModeState.value = RecordMode.Normal
                                } else {
                                    recordModeState.value = RecordMode.Alarm
                                }
                            }

                            RECORD_MODE_CLOSED -> {
                                recordModeState.value = RecordMode.Closed
                            }
                        }
                    }
                }
                if (StringUtils.contrast(msgContent.str, JsonConfig.DETECT_MOTIONDETECT)) {
                    val handleConfigData: HandleConfigData<*> = HandleConfigData<Any?>()
                    if (handleConfigData.getDataObj(jsonData, AlarmInfoBean::class.java)) {
                        alarmInfoBean = handleConfigData.obj as? AlarmInfoBean
                    }
                }
            }

            EUIMSG.DEV_SET_JSON -> {
                if (StringUtils.contrast(JsonConfig.OPSTORAGE_MANAGER, msgContent.str)) {
                    isLoading.value = false
                    try {
                        if (formatState.value != FORMAT_FAIL && formatState.value != FORMAT_SUCCESS) {
                            if (message.arg1 >= 0) {
                                if (formatCount == 1) {
                                    viewModelScope.launch {
                                        delay(1000)
                                        getConfig()
                                    }
                                    formatState.value = FORMAT_SUCCESS
                                } else {
                                    formatCount--
                                }
                            } else {
                                formatState.value = FORMAT_FAIL
                                formatCount--
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                if (StringUtils.contrast(JsonConfig.RECORD, msgContent.str)) {
                    getRecordMode()
                }
            }

            else -> Unit
        }
        return 0
    }

    override fun onCleared() {
        super.onCleared()
        FunSDK.UnRegUser(userId)
    }

    fun formatStorage() {
        val storageInfoBeans = storageInfoBeanList ?: return
        formatState.value = FORMAT_START
        formatCount = 0
        for (id in storageInfoBeans.indices) {
            val partitions = storageInfoBeans[id].Partition ?: continue
            for (idPartition in partitions.indices) {
                val partition = partitions[idPartition] ?: continue
                if (G.getLongFromHex(partition.TotalSpace) != 0L) {
                    val oPStorageInfo = OPStorageManagerBean().apply {
                        action = "Clear"
                        type = "Data"
                        serialNo = id
                        partNo = idPartition
                    }
                    FunSDK.DevSetConfigByJson(
                        userId, devId, JsonConfig.OPSTORAGE_MANAGER,
                        HandleConfigData.getSendData(
                            JsonConfig.OPSTORAGE_MANAGER, "0xbc2", oPStorageInfo
                        ),
                        -1, 60 * 1000, 0
                    )
                    formatCount++
                }
            }
        }
        if (formatCount == 0) {
            formatState.value = FORMAT_SUCCESS
        }
    }

    fun saveRecordModeSet(recordMode: RecordMode) {
        val recordParamBean = recordParamBean ?: return
        when (recordMode) {
            RecordMode.Closed -> {
                recordParamBean.RecordMode = RECORD_MODE_CLOSED
                for (i in 0 until SDKCONST.NET_N_WEEKS) {
                    try {
                        recordParamBean.Mask[i][0] = G.getHexFromInt(0)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            RecordMode.Normal -> {
                recordParamBean.RecordMode = RECORD_MODE_CONFIG
                for (i in 0 until SDKCONST.NET_N_WEEKS) {
                    try {
                        recordParamBean.Mask[i][0] = G.getHexFromInt(7)
                        recordParamBean.TimeSection[i][0] = "1 00:00:00-24:00:00"
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                openRecordAlarm()
            }

            RecordMode.Alarm -> {
                recordParamBean.RecordMode = RECORD_MODE_CONFIG
                for (i in 0 until SDKCONST.NET_N_WEEKS) {
                    try {
                        recordParamBean.Mask[i][0] = G.getHexFromInt(6)
                        recordParamBean.TimeSection[i][0] = "1 00:00:00-24:00:00"
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                openRecordAlarm()
            }
        }
        FunSDK.DevSetConfigByJson(
            userId, devId, JsonConfig.RECORD,
            HandleConfigData.getSendData(JsonConfig.RECORD, "0x08", listOf(recordParamBean)),
            -1, Define.GET_SET_DEV_CONFIG_TIMEOUT, 0
        )
        isLoading.value = true
    }

    private fun openRecordAlarm() {
        try {
            alarmInfoBean?.let {
                it.EventHandler?.RecordEnable = true
                FunSDK.DevSetConfigByJson(
                    userId, devId, JsonConfig.DETECT_MOTIONDETECT,
                    HandleConfigData.getSendData(
                        HandleConfigData.getFullName(JsonConfig.DETECT_MOTIONDETECT, 0),
                        "0x08",
                        it
                    ),
                    0, Define.GET_SET_DEV_CONFIG_TIMEOUT, 0
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        private val TAG: String = CardSettingJFViewModel::class.java.simpleName

        const val FORMAT_SUCCESS = 0
        const val FORMAT_FAIL = -1
        const val FORMAT_START = 1

        const val RECORD_MODE_CONFIG = "ConfigRecord"
        const val RECORD_MODE_CLOSED = "ClosedRecord"
    }

    sealed class RecordMode {
        object Normal : RecordMode()
        object Alarm : RecordMode()
        object Closed : RecordMode()
    }
}
