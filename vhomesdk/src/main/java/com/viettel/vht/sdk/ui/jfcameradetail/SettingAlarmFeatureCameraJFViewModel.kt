package com.viettel.vht.sdk.ui.jfcameradetail

import android.os.Message
import androidx.lifecycle.viewModelScope
import com.alibaba.fastjson.JSONObject
import com.basic.G
import com.google.gson.Gson
import com.lib.EUIMSG
import com.lib.FunSDK
import com.lib.IFunSDKResult
import com.lib.Mps.MpsClient
import com.lib.MsgContent
import com.lib.SDKCONST
import com.lib.sdk.bean.AlarmInfoBean
import com.lib.sdk.bean.BrowserLanguageBean
import com.lib.sdk.bean.ChannelHumanRuleLimitBean
import com.lib.sdk.bean.DetectTrackBean
import com.lib.sdk.bean.HandleConfigData
import com.lib.sdk.bean.HumanDetectionBean
import com.lib.sdk.bean.JsonConfig
import com.lib.sdk.bean.StringUtils
import com.lib.sdk.bean.VoiceTipBean
import com.lib.sdk.bean.VoiceTipTypeBean
import com.manager.device.DeviceManager
import com.manager.device.config.DevConfigManager
import com.vht.sdkcore.base.BaseViewModel
import com.vht.sdkcore.utils.Define
import com.vht.sdkcore.utils.SingleLiveEvent
import com.viettel.vht.sdk.utils.Config
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject


@HiltViewModel
class SettingAlarmFeatureCameraJFViewModel @Inject constructor() : BaseViewModel(), IFunSDKResult {

    lateinit var devId: String
    private var alarmInfoBean: AlarmInfoBean? = null
    private var alarmInfoBeanTemp: AlarmInfoBean? = null
    private var humanDetectionBean: HumanDetectionBean? = null
    private var humanDetectionBeanTemp: HumanDetectionBean? = null
    private var detectTrackBean: DetectTrackBean? = null
    var channelHumanRuleLimitBean: ChannelHumanRuleLimitBean? = null
    private val deviceManager: DeviceManager
    private lateinit var devConfigManager: DevConfigManager

    private var userId = 0
    private val channel = 0

    val voiceTipList = arrayListOf<VoiceTipBean>()
    val voiceSilent = VoiceTipBean().apply {
        voiceEnum = -1
        voiceText = "Im lặng"
    }
    val getHumanDetection = SingleLiveEvent<HumanDetectionBean?>()
    val setHumanDetection = SingleLiveEvent<Pair<HumanDetectionBean?, Boolean>>()
    val getAlarmInfo = SingleLiveEvent<AlarmInfoBean?>()
    val updateAlarmInfoState = SingleLiveEvent<Boolean>()
    val voiceTip = SingleLiveEvent<VoiceTipBean?>().apply { value = null }

    val alarmPushInterval = SingleLiveEvent<Int>().apply { value = -1 }
    private var alarmPushIntervalTemp = -1
    val updateAlarmPushIntervalState = SingleLiveEvent<Boolean>()

    init {
        userId = FunSDK.GetId(userId, this)
        deviceManager = DeviceManager.getInstance()
    }

    fun getConfig() {
        if (!this::devId.isInitialized) return
        isLoading.value = true
        if (isSupportHumanDetect()) {
            FunSDK.DevCmdGeneral(
                userId, devId, 1360, JsonConfig.HUMAN_RULE_LIMIT,
                -1, Define.GET_SET_DEV_CONFIG_TIMEOUT, null, -1, 0
            )
        }
        FunSDK.DevGetConfigByJson(
            userId,
            devId,
            JsonConfig.DETECT_MOTIONDETECT,
            1024,
            channel,
            Define.GET_SET_DEV_CONFIG_TIMEOUT,
            0
        )
        if (isSupportAlarmVoiceTipsType()) {
            FunSDK.DevCmdGeneral(
                userId, devId, 1040,
                JsonConfig.CFG_BROWSER_LANGUAGE, -1, Define.GET_SET_DEV_CONFIG_TIMEOUT,
                HandleConfigData.getSendData(
                    JsonConfig.CFG_BROWSER_LANGUAGE,
                    "0x08",
                    BrowserLanguageBean().apply { browserLanguageType = 21 } // 21: Tiếng Việt
                ).toByteArray(),
                -1, 0
            )
        }
        if (isSupportAlarmVoiceTips()) {
            FunSDK.DevGetConfigByJson(
                userId, devId, JsonConfig.CFG_VOICE_TIP_TYPE,
                1024, -1, Define.GET_SET_DEV_CONFIG_TIMEOUT, 0
            )
        }
        getAlarmPushInterval()
        devConfigManager = deviceManager.getDevConfigManager(devId)
    }

    fun isSupportAlarmVoiceTips(): Boolean {
        if (!this::devId.isInitialized) return false
        return FunSDK.GetDevAbility(devId, "OtherFunction/SupportAlarmVoiceTips") > 0
    }

    private fun isSupportAlarmVoiceTipsType(): Boolean {
        return FunSDK.GetDevAbility(devId, "OtherFunction/SupportAlarmVoiceTipsType") > 0
    }

    private fun isSupportHumanDetect(): Boolean {
        return FunSDK.GetDevAbility(devId, "AlarmFunction/PEAInHumanPed") > 0
    }

    fun isDetectTrackEnable(): Boolean {
        return detectTrackBean != null && detectTrackBean?.enable == SDKCONST.Switch.Open
    }

    fun isMotionHumanDetection(): Boolean {
        return FunSDK.GetDevAbility(devId, "AlarmFunction/MotionHumanDection") > 0
    }

    fun isTrackSupport(): Boolean {
        return channelHumanRuleLimitBean?.isShowTrack ?: false
    }

    private fun getAlarmPushInterval() {
        val jsonObject = JSONObject()
        jsonObject["sn"] = devId
        jsonObject["msg"] = GET_PUSH_INTERVAL
        MpsClient.OptAlarmMsgPushInterval(userId, jsonObject.toJSONString(), 0)
    }

    override fun OnFunSDKResult(message: Message, msgContent: MsgContent): Int {
        Timber.tag(TAG).d("OnFunSDKResult")
        Timber.tag(TAG).d("message: ${message.what} - $message")
        Timber.tag(TAG).d("msgContent: ${Gson().toJson(msgContent)}")
        when (message.what) {
            EUIMSG.DEV_GET_JSON -> {
                val jsonData = G.ToString(msgContent.pData)
                if (StringUtils.contrast(msgContent.str, JsonConfig.DETECT_MOTIONDETECT)) {
                    dealWithMotionDetect(jsonData)
                }
                if (StringUtils.contrast(msgContent.str, JsonConfig.DETECT_HUMAN_DETECTION)) {
                    dealWithHumanDetect(jsonData)
                    FunSDK.DevGetConfigByJson(
                        userId, devId, JsonConfig.CFG_DETECT_TRACK,
                        4096, -1, Define.GET_SET_DEV_CONFIG_TIMEOUT, 0
                    )
                }
                if (StringUtils.contrast(msgContent.str, JsonConfig.CFG_DETECT_TRACK)) {
                    dealWithDetectTrack(jsonData)
                }
                if (StringUtils.contrast(msgContent.str, JsonConfig.CFG_VOICE_TIP_TYPE)) {
                    dealWithVoiceName(jsonData)
                }
                isLoading.value = false
            }

            EUIMSG.DEV_SET_JSON -> {
                if (StringUtils.contrast(JsonConfig.DETECT_MOTIONDETECT, msgContent.str)) {
                    if (message.arg1 >= 0) {
                        updateAlarmInfoState.value = true
                        alarmInfoBean = alarmInfoBeanTemp?.clone()
                    } else {
                        updateAlarmInfoState.value = false
                        alarmInfoBeanTemp = alarmInfoBean?.clone()
                    }
                    getAlarmInfo.value = alarmInfoBean
                    refreshVoiceTip()
                }
                if (StringUtils.contrast(JsonConfig.DETECT_HUMAN_DETECTION, msgContent.str)) {
                    if (message.arg1 >= 0) {
                        humanDetectionBean?.isEnable = humanDetectionBeanTemp?.isEnable ?: false
                        if (humanDetectionBean?.isEnable == true) {
                            detectTrackBean?.enable = SDKCONST.Switch.Close
                        }
                        humanDetectionBean?.isShowTrack =
                            humanDetectionBeanTemp?.isShowTrack ?: false
                        humanDetectionBean?.pedRules?.get(0)?.isEnable =
                            humanDetectionBeanTemp?.pedRules?.get(0)?.isEnable ?: false
                        humanDetectionBean?.pedRules?.get(0)?.ruleType =
                            humanDetectionBeanTemp?.pedRules?.get(0)?.ruleType
                                ?: HumanDetectionBean.IA_TRIPWIRE
                        setHumanDetection.value = Pair(humanDetectionBean, true)
                    } else {
                        humanDetectionBeanTemp?.isEnable = humanDetectionBean?.isEnable ?: false
                        humanDetectionBeanTemp?.isShowTrack =
                            humanDetectionBean?.isShowTrack ?: false
                        humanDetectionBeanTemp?.pedRules?.get(0)?.isEnable =
                            humanDetectionBean?.pedRules?.get(0)?.isEnable ?: false
                        humanDetectionBeanTemp?.pedRules?.get(0)?.ruleType =
                            humanDetectionBean?.pedRules?.get(0)?.ruleType
                                ?: HumanDetectionBean.IA_TRIPWIRE
                        setHumanDetection.value = Pair(humanDetectionBean, false)
                    }
                }
                isLoading.value = false
            }

            EUIMSG.DEV_CMD_EN -> {
                if (StringUtils.contrast(msgContent.str, JsonConfig.HUMAN_RULE_LIMIT)) {
                    val handleConfigData: HandleConfigData<*> = HandleConfigData<Any?>()
                    if (
                        handleConfigData.getDataObj(
                            G.ToString(msgContent.pData),
                            ChannelHumanRuleLimitBean::class.java
                        )
                    ) {
                        channelHumanRuleLimitBean =
                            handleConfigData.obj as ChannelHumanRuleLimitBean
                        FunSDK.DevGetConfigByJson(
                            userId, devId, JsonConfig.DETECT_HUMAN_DETECTION,
                            4096, 0, Define.GET_SET_DEV_CONFIG_TIMEOUT, 0
                        )
                    }
                }
            }

            EUIMSG.MC_OPT_ALARM_MSG_PUSH_INTERVAL -> {
                try {
                    val jsonObject = JSONObject.parseObject(msgContent.str)
                    if (jsonObject["msg"] == GET_PUSH_INTERVAL) {
                        if (jsonObject["code"] == "200") {
                            val interval = jsonObject.getInteger("interval")
                            if (interval != null) {
                                alarmPushInterval.value = if (interval <= 0) 10 else interval
                            }
                        }
                    } else if (jsonObject["msg"] == SET_PUSH_INTERVAL) {
                        if (jsonObject["code"] == "200") {
                            alarmPushInterval.value = alarmPushIntervalTemp
                        } else {
                            alarmPushIntervalTemp = 0
                            updateAlarmPushIntervalState.value = false
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                isLoading.value = false
            }

            else -> {
            }
        }
        return 0
    }

    override fun onCleared() {
        super.onCleared()
        FunSDK.UnRegUser(userId)
    }

    private fun dealWithMotionDetect(dataJson: String?) {
        if (dataJson == null) {
            return
        }
        val handleConfigData: HandleConfigData<*> = HandleConfigData<Any?>()
        val handleConfigDataTemp: HandleConfigData<*> = HandleConfigData<Any?>()
        if (
            handleConfigData.getDataObj(dataJson, AlarmInfoBean::class.java) &&
            handleConfigDataTemp.getDataObj(dataJson, AlarmInfoBean::class.java)
        ) {
            alarmInfoBean = handleConfigData.obj as AlarmInfoBean
            alarmInfoBeanTemp = handleConfigDataTemp.obj as AlarmInfoBean
            getAlarmInfo.value = alarmInfoBean
            refreshVoiceTip()
        }
    }

    private fun dealWithHumanDetect(dataJson: String?) {
        if (dataJson == null) {
            return
        }
        val handleConfigData: HandleConfigData<*> = HandleConfigData<Any?>()
        val handleConfigDataTemp: HandleConfigData<*> = HandleConfigData<Any?>()
        if (
            handleConfigData.getDataObj(dataJson, HumanDetectionBean::class.java) &&
            handleConfigDataTemp.getDataObj(dataJson, HumanDetectionBean::class.java)
        ) {
            humanDetectionBean = handleConfigData.obj as HumanDetectionBean
            humanDetectionBeanTemp = handleConfigDataTemp.obj as HumanDetectionBean
            getHumanDetection.value = humanDetectionBean
        }
    }

    private fun dealWithDetectTrack(dataJson: String?) {
        try {
            if (dataJson == null) {
                return
            }
            val handleConfigData: HandleConfigData<*> = HandleConfigData<Any?>()
            if (handleConfigData.getDataObj(dataJson, DetectTrackBean::class.java)) {
                detectTrackBean = handleConfigData.obj as DetectTrackBean
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun dealWithVoiceName(dataJson: String?) {
        try {
            if (dataJson == null) {
                return
            }
            val handleConfigData: HandleConfigData<*> = HandleConfigData<Any?>()
            if (handleConfigData.getDataObj(dataJson, VoiceTipTypeBean::class.java)) {
                val voiceTipTypeBean = handleConfigData.obj as VoiceTipTypeBean
                voiceTipList.clear()
                voiceTipList.add(voiceSilent)
                voiceTipList.addAll(voiceTipTypeBean.voiceTips)
                voiceTipList.removeLastOrNull()
                refreshVoiceTip()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun refreshVoiceTip() {
        if (alarmInfoBean != null) {
            if (alarmInfoBean?.EventHandler?.VoiceEnable != true) {
                voiceTip.value = voiceSilent
                return
            }
            voiceTipList.forEach {
                if (it.voiceEnum == alarmInfoBean?.EventHandler?.VoiceType) {
                    voiceTip.value = it
                    return
                }
            }
        }
    }

    fun setAlarmSensitivityLevel(level: Int) {
        if (alarmInfoBean != null && alarmInfoBeanTemp != null) {
            alarmInfoBeanTemp?.Level = level
            saveAlarmDetect()
        } else {
            updateAlarmInfoState.value = false
        }
    }

    fun setVoiceTipAlarm(voiceTipBean: VoiceTipBean) {
        if (alarmInfoBean != null && alarmInfoBeanTemp != null) {
            if (voiceTipBean.voiceEnum == voiceSilent.voiceEnum) {
                alarmInfoBeanTemp?.EventHandler?.VoiceEnable = false
            } else {
                alarmInfoBeanTemp?.EventHandler?.VoiceEnable = true
                alarmInfoBeanTemp?.EventHandler?.VoiceType = voiceTipBean.voiceEnum
            }
            saveAlarmDetect()
        } else {
            updateAlarmInfoState.value = false
        }
    }

    private fun saveAlarmDetect() {
        alarmInfoBeanTemp?.let {
            isLoading.value = true
            FunSDK.DevSetConfigByJson(
                userId, devId, JsonConfig.DETECT_MOTIONDETECT,
                HandleConfigData.getSendData(
                    HandleConfigData.getFullName(JsonConfig.DETECT_MOTIONDETECT, channel),
                    "0x08",
                    it
                ),
                channel, Define.GET_SET_DEV_CONFIG_TIMEOUT, -1
            )
        }
    }

    fun setHumanDetectEnable(enable: Boolean) {
        if (humanDetectionBean != null && humanDetectionBeanTemp != null) {
            humanDetectionBeanTemp?.isEnable = enable
            saveHumanDetect()
        } else {
            setHumanDetection.value = Pair(humanDetectionBean, false)
        }
    }

    fun setAlarmTrackEnable(enable: Boolean) {
        if (humanDetectionBean != null && humanDetectionBeanTemp != null) {
            humanDetectionBeanTemp?.isShowTrack = enable
            saveHumanDetect()
        } else {
            setHumanDetection.value = Pair(humanDetectionBean, false)
        }
    }

    private fun saveHumanDetect() {
        humanDetectionBeanTemp?.let {
            isLoading.value = true
            FunSDK.DevSetConfigByJson(
                userId, devId, JsonConfig.DETECT_HUMAN_DETECTION,
                HandleConfigData.getSendData(
                    HandleConfigData.getFullName(JsonConfig.DETECT_HUMAN_DETECTION, 0),
                    "0x08", it
                ),
                0, Define.GET_SET_DEV_CONFIG_TIMEOUT, -1
            )
        }
    }

    fun getRuleType(): String {
        humanDetectionBean?.let {
            if (it.pedRules.isNotEmpty()) {
                return if (it.pedRules[0].isEnable) {
                    when (it.pedRules[0].ruleType) {
                        HumanDetectionBean.IA_TRIPWIRE -> {
                            humanDetectionBean?.pedRules?.get(0)?.isEnable = false
                            humanDetectionBeanTemp?.pedRules?.get(0)?.isEnable = false
                            HandleConfigData.getSendData(
                                HandleConfigData.getFullName(JsonConfig.DETECT_HUMAN_DETECTION, 0),
                                "0x08",
                                humanDetectionBean
                            ).let { data ->
                                FunSDK.DevSetConfigByJson(
                                    userId, devId, JsonConfig.DETECT_HUMAN_DETECTION,
                                    data, 0, Define.GET_SET_DEV_CONFIG_TIMEOUT, -1
                                )
                            }
                            Config.EventKey.EVENT_SETTING_CAMERA_SECURITY_FENCE_DEFAULT
                        }

                        HumanDetectionBean.IA_PERIMETER -> {
                            Config.EventKey.EVENT_SETTING_CAMERA_SAFE_AREA
                        }

                        else -> {
                            Config.EventKey.EVENT_SETTING_CAMERA_SECURITY_FENCE_DEFAULT
                        }
                    }
                } else {
                    Config.EventKey.EVENT_SETTING_CAMERA_SECURITY_FENCE_DEFAULT
                }
            }
        }
        return Config.EventKey.EVENT_SETTING_CAMERA_SECURITY_FENCE_DEFAULT
    }

    fun setRuleType(ruleType: String) {
        if (humanDetectionBean != null && humanDetectionBeanTemp != null) {
            if (humanDetectionBeanTemp?.pedRules?.isNotEmpty() == true) {
                when (ruleType) {
                    Config.EventKey.EVENT_SETTING_CAMERA_SECURITY_FENCE_DEFAULT -> {
                        humanDetectionBeanTemp?.pedRules?.get(0)?.isEnable = false
                    }

                    Config.EventKey.EVENT_SETTING_CAMERA_SECURITY_FENCE -> {
                        humanDetectionBeanTemp?.pedRules?.get(0)?.isEnable = true
                        humanDetectionBeanTemp?.pedRules?.get(0)?.ruleType =
                            HumanDetectionBean.IA_TRIPWIRE
                    }

                    Config.EventKey.EVENT_SETTING_CAMERA_SAFE_AREA -> {
                        humanDetectionBeanTemp?.pedRules?.get(0)?.isEnable = true
                        humanDetectionBeanTemp?.pedRules?.get(0)?.ruleType =
                            HumanDetectionBean.IA_PERIMETER
                    }
                }
            }
            saveHumanDetect()
        } else {
            setHumanDetection.value = Pair(humanDetectionBean, false)
        }
    }

    fun securityFence(humanDetection: HumanDetectionBean) {
        if (humanDetectionBean != null && humanDetectionBeanTemp != null) {
            humanDetectionBeanTemp?.pedRules = humanDetection.pedRules
            saveHumanDetect()
        } else {
            setHumanDetection.value = Pair(humanDetectionBean, false)
        }
    }

    fun setAlarmInterval(interval: Int) {
        viewModelScope.launch {
            isLoading.postValue(true)
            alarmPushIntervalTemp = interval
            val jsonObject = JSONObject()
            jsonObject["sn"] = devId
            jsonObject["msg"] = SET_PUSH_INTERVAL
            jsonObject["interval"] = interval
            MpsClient.OptAlarmMsgPushInterval(userId, jsonObject.toJSONString(), 0)
        }
    }

    companion object {
        private val TAG: String = SettingAlarmFeatureCameraJFViewModel::class.java.simpleName

        private const val GET_PUSH_INTERVAL = "get_push_interval"
        private const val SET_PUSH_INTERVAL = "set_push_interval"
    }
}
