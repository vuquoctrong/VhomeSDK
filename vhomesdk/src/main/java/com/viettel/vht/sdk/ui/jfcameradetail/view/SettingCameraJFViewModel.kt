package com.viettel.vht.sdk.ui.jfcameradetail.view

import android.annotation.SuppressLint
import android.os.Message
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.basic.G
import com.google.gson.Gson
import com.lib.AS.AlarmService
import com.lib.EUIMSG
import com.lib.FunSDK
import com.lib.IFunSDKResult
import com.lib.MsgContent
import com.lib.SDKCONST
import com.lib.sdk.bean.AlarmInfoBean
import com.lib.sdk.bean.CameraParamBean
import com.lib.sdk.bean.ChannelHumanRuleLimitBean
import com.lib.sdk.bean.DSTimeBean
import com.lib.sdk.bean.DayLightTimeBean
import com.lib.sdk.bean.DetectTrackBean
import com.lib.sdk.bean.DevVolumeBean
import com.lib.sdk.bean.FbExtraStateCtrlBean
import com.lib.sdk.bean.HandleConfigData
import com.lib.sdk.bean.HumanDetectionBean
import com.lib.sdk.bean.JsonConfig
import com.lib.sdk.bean.LocationBean
import com.lib.sdk.bean.StringUtils
import com.lib.sdk.bean.TimeZoneBean
import com.lib.sdk.bean.WhiteLightBean
import com.manager.account.BaseAccountManager
import com.manager.account.XMAccountManager
import com.manager.db.DevDataCenter
import com.manager.device.DeviceManager
import com.manager.device.config.DevConfigManager
import com.vht.sdkcore.base.BaseViewModel
import com.vht.sdkcore.utils.Define
import com.vht.sdkcore.utils.SingleLiveEvent
import com.vht.sdkcore.utils.eventbus.RxEvent
import com.viettel.vht.sdk.jfmanager.JFCameraManager
import com.viettel.vht.sdk.model.ResponseCheckDeviceSpread
import com.viettel.vht.sdk.model.camera_cloud.CloudStorageRegistered
import com.viettel.vht.sdk.model.home.FeatureConfigResponse
import com.viettel.vht.sdk.model.jfcall.NoDisturbingAlarmTimeInfoBean
import com.viettel.vht.sdk.network.repository.CloudRepository
import com.viettel.vht.sdk.network.repository.HomeRepository
import com.viettel.vht.sdk.ui.jfcameradetail.smart_feature.IAlarmPushSetView
import com.viettel.vht.sdk.ui.jfcameradetail.smart_feature.IHumanDetectView
import com.viettel.vht.sdk.ui.jfcameradetail.view.dialog.DateFormatJF
import com.viettel.vht.sdk.ui.jfcameradetail.view.dialog.SmartSettingNotificationCameraJF
import com.viettel.vht.sdk.ui.jfcameradetail.view.dialog.WhiteLight
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Response
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject
import kotlin.math.abs


@HiltViewModel
class SettingCameraJFViewModel @Inject constructor(
    private val cloudRepository: CloudRepository,
    private val homeRepository: HomeRepository
) : BaseViewModel(), IFunSDKResult {

    lateinit var devId: String
    lateinit var deviceId: String
    private var locationBean: LocationBean? = null
    private var currentCameraParamBean: CameraParamBean? = null
    var currentVolumeBean: DevVolumeBean? = null
    val currentVolumeBeanState =
        SingleLiveEvent<DevVolumeBean?>().apply { value = currentVolumeBean }
    var currentMicVolumeBean: DevVolumeBean? = null
    val currentMicVolumeBeanState =
        SingleLiveEvent<DevVolumeBean?>().apply { value = currentMicVolumeBean }
    var currentStatusLEDBean: FbExtraStateCtrlBean? = null
    var currentDetectTrackBean: DetectTrackBean? = null
    var currentHumanDetectionBean: HumanDetectionBean? = null
    var channelHumanRuleLimitBean: ChannelHumanRuleLimitBean? = null
    private var detectTrackBean: DetectTrackBean? = null
    private var alarmInfoBean: AlarmInfoBean? = null
    var whiteLight: WhiteLightBean? = null
    private var whiteLightTemp: WhiteLightBean? = null
    var cameraParam: CameraParamBean? = null
    private var cameraParamTemp: CameraParamBean? = null
    private val deviceManager: DeviceManager
    private lateinit var devConfigManager: DevConfigManager

    val openSettingLiveEvent = SingleLiveEvent<RxEvent<String>>()
    val openSecurityFenceEvent = SingleLiveEvent<RxEvent<String>>()

    private var userId = 0
    private val channel = 0
    private var iHumanDetectView: IHumanDetectView? = null
    private var iAlarmPushSetView: IAlarmPushSetView? = null

    private val _deleteResponse = SingleLiveEvent<com.vht.sdkcore.network.Result<Response<Unit>>>()
    val deleteResponse: SingleLiveEvent<com.vht.sdkcore.network.Result<Response<Unit>>> get() = _deleteResponse

    val systemTimezone = SingleLiveEvent<String>().apply { value = "" }
    val syncSystemTimezoneState = SingleLiveEvent<Boolean>()
    val isDeleteSuccessDeviceFromJF = SingleLiveEvent<Boolean>()

    private var noDisturbingAlarmTimeInfoBean: NoDisturbingAlarmTimeInfoBean? = null
    private var noDisturbingAlarmTimeInfoBeanTemp: NoDisturbingAlarmTimeInfoBean? = null
    val notificationAlarm = SingleLiveEvent<SmartSettingNotificationCameraJF>().apply {
        value = SmartSettingNotificationCameraJF.OFF
    }
    val updateAlarmInfoState = SingleLiveEvent<Boolean>()

    val getWhiteLightState = SingleLiveEvent<WhiteLight>().apply { value = WhiteLight.OFF }
    val setWhiteLightState = SingleLiveEvent<Boolean>()

    val cloudRegistered = MutableLiveData<List<CloudStorageRegistered>?>()

    var dateFormatObject: JSONObject? = null
    val dateFormatState = SingleLiveEvent<DateFormatJF>().apply { value = DateFormatJF.MMDDYYYY() }
    val updateDateFormatState = SingleLiveEvent<Boolean>()

    val errorSDKId = SingleLiveEvent<Int>()

    init {
        userId = FunSDK.GetId(userId, this)
        deviceManager = DeviceManager.getInstance()
    }


    fun getListCloudStorageRegistered(serial: String) {
        viewModelScope.launch(Dispatchers.IO) {
            cloudRepository.getListCloudStorageRegistered(serial).collect {
                when (it) {
                    is com.viettel.vht.sdk.network.Result.Loading -> {
                        isLoading.postValue(true)
                    }

                    is com.viettel.vht.sdk.network.Result.Error -> {
                        isLoading.postValue(false)
                        Timber.e("${it.error}")
                    }

                    is com.viettel.vht.sdk.network.Result.Success -> {
                        isLoading.postValue(false)
                        cloudRegistered.postValue(it.data?.data)
                    }

                    else -> Unit
                }
            }
        }
    }

    fun setIHumanDetectView(iHumanDetectView: IHumanDetectView) {
        this.iHumanDetectView = iHumanDetectView
    }

    fun setIAlarmPushSetView(iAlarmPushSetView: IAlarmPushSetView) {
        this.iAlarmPushSetView = iAlarmPushSetView
    }

    fun setEditNameDevice(nameEdit: String, callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            homeRepository.editNameDevice(
                deviceId = deviceId,
                nameEdit = nameEdit
            ).collect {
                if (it is com.vht.sdkcore.network.Result.Success) {
                    callback(true)
                    JFCameraManager.modifyDeviceName(devId, nameEdit) {}
                } else if (it is com.vht.sdkcore.network.Result.Error) {
                    callback(false)
                }
            }
        }
    }

    fun flipCamera(callback: (Int) -> Unit) {
        JFCameraManager.flipCamera(devId, currentCameraParamBean, callback)
    }

    fun flipCameraLeftRight(callback: (Boolean) -> Unit) {
        JFCameraManager.flipCameraLeftRight(devId, currentCameraParamBean, callback)
    }

    fun setVolume(volume: Int) {
        isLoading.postValue(true)
        JFCameraManager.setVolume(devId, volume, currentVolumeBean) { isSuccess ->
            isLoading.postValue(false)
            if (isSuccess) {
                currentVolumeBean?.leftVolume = volume
                currentVolumeBean?.rightVolume = volume
            }
            currentVolumeBeanState.postValue(currentVolumeBean)
        }
    }

    fun setMicVolume(volume: Int) {
        isLoading.postValue(true)
        JFCameraManager.setMicVolume(devId, volume, currentMicVolumeBean) { isSuccess ->
            isLoading.postValue(false)
            if (isSuccess) {
                currentMicVolumeBean?.leftVolume = volume
                currentMicVolumeBean?.rightVolume = volume
            }
            currentMicVolumeBeanState.postValue(currentMicVolumeBean)
        }
    }

    fun rebootOrReset(cmd: String, callback: (Boolean) -> Unit) {
        JFCameraManager.rebootOrResetIPC(devId, cmd, callback)
    }

    fun setStatusLED(newState: Boolean, callback: (Boolean) -> Unit) {
        JFCameraManager.setStatusLED(devId, newState, currentStatusLEDBean) {
            if (!it) {
                currentStatusLEDBean?.ison =
                    if (!newState) SDKCONST.Switch.Open else SDKCONST.Switch.Close
            }
            callback(it)
        }
    }

    fun setStatusMask(newState: Boolean, callback: (Boolean) -> Unit) {
        JFCameraManager.setStatusMask(devId, newState, callback)
    }

    fun setDetectTrack(newState: Boolean, callback: (Boolean) -> Unit) {
        currentDetectTrackBean?.enable = if (newState) {
            1
        } else {
            0
        }
        JFCameraManager.setDetectTrack(devId, currentDetectTrackBean, callback)
    }

    fun getSystemFunction() {
        if (!this::devId.isInitialized) return
        isLoading.value = true
        viewModelScope.launch {
            var userIdGetSystemFunction = 0
            userIdGetSystemFunction = FunSDK.GetId(userIdGetSystemFunction) { message, _ ->
                errorSDKId.value = message.arg1
                isLoading.value = false
                0
            }

            FunSDK.DevGetConfigByJson(
                userIdGetSystemFunction,
                devId,
                "SystemFunction",
                1024,
                -1,
                Define.GET_SET_DEV_CONFIG_TIMEOUT,
                0
            )
        }
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
//        if (isSupportRemoteCallAlarm()) {
//            FunSDK.DevGetConfigByJson(
//                userId, devId, AlarmRemoteCallBean.JSON_NAME,
//                1024, -1, Define.GET_SET_DEV_CONFIG_TIMEOUT, 0
//            )
//        }

        FunSDK.DevGetConfigByJson(
            userId,
            devId,
            JsonConfig.GENERAL_LOCATION,
            1024,
            -1,
            Define.GET_SET_DEV_CONFIG_TIMEOUT,
            0
        )

        getSystemTimezone()

        getConfigNoDisturbingAlarmTime()


        if (isSupportWhiteLight()) {
            FunSDK.DevGetConfigByJson(
                userId, devId, JsonConfig.CAMERA_PARAM, 1024,
                0, Define.GET_SET_DEV_CONFIG_TIMEOUT, 0
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
        FunSDK.DevGetConfigByJson(
            userId, devId,
            DATE_FORMAT, 1024, -1,
            Define.GET_SET_DEV_CONFIG_TIMEOUT, 0
        )

        JFCameraManager.getVolumeCamera(devId) { bean ->
            currentVolumeBean = bean
            currentVolumeBeanState.postValue(currentVolumeBean)
        }
        JFCameraManager.getMicVolumeCamera(devId) { bean ->
            currentMicVolumeBean = bean
            currentMicVolumeBeanState.postValue(currentMicVolumeBean)
        }

        JFCameraManager.getCameraParamex(devId) { bean ->
            currentCameraParamBean = bean
        }
        devConfigManager = deviceManager.getDevConfigManager(devId)
    }

    // request get no disturbing alarm config
    private fun getConfigNoDisturbingAlarmTime() {
        Timber.tag(TAG).d("request get no disturbing alarm config,devId=$devId")
        val infoBean = NoDisturbingAlarmTimeInfoBean()
        infoBean.msg = GET_ALARM_TIME
        infoBean.sn = devId
        AlarmService.OptAlarmMsgDoNotDisturb(userId, Gson().toJson(infoBean), 1)
    }

    private fun getSystemTimezone() {
        FunSDK.DevGetConfigByJson(
            userId,
            devId,
            JsonConfig.SYSTEM_TIMEZONE,
            1024,
            -1,
            Define.GET_SET_DEV_CONFIG_TIMEOUT,
            0
        )
    }

    @SuppressLint("SimpleDateFormat")
    private fun getDayLightTimeInfo(tz: TimeZone): DayLightTimeBean {
        val dltInfo = DayLightTimeBean().apply { useDLT = tz.useDaylightTime() }
        if (dltInfo.useDLT) {
            try {
                val date = Date()
                val sdfYear = SimpleDateFormat("yyyy").apply { timeZone = tz }
                val sdfDay = SimpleDateFormat("yyyy-MM-dd").apply { timeZone = tz }

                dltInfo.year = sdfYear.format(date).toInt()

                for (month in 1..12) {
                    val tmpStr = String.format("%04d-%02d-%02d", dltInfo.year, month, 1)
                    if (dltInfo.beginMonth == 0 && tz.inDaylightTime(sdfDay.parse(tmpStr))) {
                        dltInfo.beginMonth = month
                    }
                    if (
                        dltInfo.beginMonth > 0 && dltInfo.endMonth == 0 &&
                        !tz.inDaylightTime(sdfDay.parse(tmpStr))
                    ) {
                        dltInfo.endMonth = month
                    }
                    if (
                        dltInfo.beginMonth > 0 && dltInfo.endMonth > 0 &&
                        tz.inDaylightTime(sdfDay.parse(tmpStr))
                    ) {
                        dltInfo.beginMonth = month
                        break
                    }
                }

                if (dltInfo.beginMonth > 1) {
                    dltInfo.beginDay = 1
                    for (day in 1..31) {
                        val tmpStr = String.format(
                            "%04d-%02d-%02d",
                            dltInfo.year,
                            dltInfo.beginMonth - 1,
                            day
                        )
                        if (tz.inDaylightTime(sdfDay.parse(tmpStr))) {
                            dltInfo.beginDay = day
                            dltInfo.beginMonth = dltInfo.beginMonth - 1
                            break
                        }
                    }
                }

                if (dltInfo.endMonth > 1) {
                    dltInfo.endDay = 1
                    for (day in 1..31) {
                        val year =
                            if (dltInfo.beginMonth > dltInfo.endMonth) dltInfo.year + 1 else dltInfo.year
                        val tmpStr =
                            String.format("%04d-%02d-%02d", year, dltInfo.endMonth - 1, day)
                        if (!tz.inDaylightTime(sdfDay.parse(tmpStr))) {
                            dltInfo.endMonth = dltInfo.endMonth - 1
                            dltInfo.endDay = day
                            break
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return dltInfo
    }

    private fun setTimeZone(): Int {
        val cal = Calendar.getInstance(Locale.getDefault())
        val zoneOffset = cal[Calendar.ZONE_OFFSET].toFloat()
        val isDST = cal.timeZone.inDaylightTime(Date(cal.timeInMillis))
        val zoneOffsetWithoutDST = if (isDST) zoneOffset + cal.timeZone.dstSavings else zoneOffset
        val zone = (zoneOffsetWithoutDST / 60.0 / 60.0 / 1000.0).toFloat()
        return (-zone * 60).toInt()
    }

    fun syncDevTimeZone() {
        val timeZoneBean = TimeZoneBean()
        timeZoneBean.timeMin = setTimeZone()
        timeZoneBean.FirstUserTimeZone = 0
        FunSDK.DevSetConfigByJson(
            userId, devId, JsonConfig.SYSTEM_TIMEZONE,
            HandleConfigData.getSendData(JsonConfig.SYSTEM_TIMEZONE, "0x1", timeZoneBean),
            -1, Define.GET_SET_DEV_CONFIG_TIMEOUT, 0
        )
        locationBean?.let {
            val dayLightTimeBean: DayLightTimeBean = getDayLightTimeInfo(TimeZone.getDefault())
            if (dayLightTimeBean.useDLT) {
                it.setdSTRule("On")
                val dstStart = DSTimeBean()
                dstStart.year = dayLightTimeBean.year
                dstStart.month = dayLightTimeBean.beginMonth
                dstStart.day = dayLightTimeBean.beginDay
                val dstEnd = DSTimeBean()
                dstEnd.year =
                    if (dayLightTimeBean.beginMonth > dayLightTimeBean.endMonth) dayLightTimeBean.year + 1 else dayLightTimeBean.year
                dstEnd.month = dayLightTimeBean.endMonth
                dstEnd.day = dayLightTimeBean.endDay
                it.setdSTStart(dstStart)
                it.setdSTEnd(dstEnd)
            } else {
                it.setdSTRule("Off")
            }
            FunSDK.DevSetConfigByJson(
                userId, devId, JsonConfig.GENERAL_LOCATION,
                HandleConfigData.getSendData(JsonConfig.GENERAL_LOCATION, "0x02", locationBean),
                -1, Define.GET_SET_DEV_CONFIG_TIMEOUT, 0
            )
        }
    }

    fun syncSystemTimezone() {
        try {
            val time = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(Calendar.getInstance(Locale.getDefault()).time)
            FunSDK.DevSetConfigByJson(
                userId, devId,
                JsonConfig.OPTIME_SET,
                HandleConfigData.getSendData(JsonConfig.OPTIME_SET, "0x00000001", time),
                -1,
                Define.GET_SET_DEV_CONFIG_TIMEOUT,
                0
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun isSupportWhiteLight(): Boolean {
        if (!this::devId.isInitialized) return false
        return FunSDK.GetDevAbility(devId, "OtherFunction/SupportCameraWhiteLight") > 0 &&
                FunSDK.GetDevAbility(devId, "OtherFunction/SupportDoubleLightBoxCamera") > 0
    }

    private fun isSupportHumanDetect(): Boolean {
        return FunSDK.GetDevAbility(devId, "AlarmFunction/PEAInHumanPed") > 0
    }

    fun setAlarmSensitivityLevel(level: Int) {
        if (alarmInfoBean != null) {
            alarmInfoBean!!.Level = level
            saveAlarmDetect()
        } else {
            if (iAlarmPushSetView != null) {
                iAlarmPushSetView?.onGetConfigFailed()
            }
        }
    }

    fun getAlarmSensitivityLevel(): Int {
        return if (alarmInfoBean != null) {
            alarmInfoBean!!.Level
        } else 0
    }

    private fun saveAlarmDetect() {
        FunSDK.DevSetConfigByJson(
            userId, devId, JsonConfig.DETECT_MOTIONDETECT,
            HandleConfigData.getSendData(
                HandleConfigData.getFullName(JsonConfig.DETECT_MOTIONDETECT, channel),
                "0x08", alarmInfoBean
            ),
            channel, Define.GET_SET_DEV_CONFIG_TIMEOUT, -1
        )
    }

    fun saveHumanDetect(): Boolean {
        Timber.tag(TAG).d("saveHumanDetect")
        if (currentHumanDetectionBean == null) {
            return false
        }
        FunSDK.DevSetConfigByJson(
            userId, devId,
            JsonConfig.DETECT_HUMAN_DETECTION,
            HandleConfigData.getSendData(
                HandleConfigData.getFullName(
                    JsonConfig.DETECT_HUMAN_DETECTION,
                    channel
                ), "0x08", currentHumanDetectionBean
            ),
            channel, Define.GET_SET_DEV_CONFIG_TIMEOUT, 0
        )
        return true
    }

    fun getRuleType(): Int {
        if (currentHumanDetectionBean != null) {
            if (currentHumanDetectionBean!!.pedRules.isNotEmpty()) {
                return currentHumanDetectionBean!!.pedRules[0].ruleType
            }
        }
        return HumanDetectionBean.IA_TRIPWIRE
    }

    fun setRuleType(ruleType: Int) {
        if (currentHumanDetectionBean != null && currentHumanDetectionBean!!.pedRules.isNotEmpty()) {
            currentHumanDetectionBean?.pedRules?.get(0)?.ruleType = ruleType
        }
    }

    private fun dealWithMotionDetect(dataJson: String?) {
        if (dataJson == null) {
            return
        }
        val handleConfigData: HandleConfigData<*> = HandleConfigData<Any?>()
        if (handleConfigData.getDataObj(dataJson, AlarmInfoBean::class.java)) {
            alarmInfoBean = handleConfigData.obj as AlarmInfoBean
            if (alarmInfoBean != null) {
                if (iAlarmPushSetView != null) {
                    iAlarmPushSetView?.onGetMotionDetectResult(true)
                }
            } else {
                if (iAlarmPushSetView != null) {
                    iAlarmPushSetView?.onGetConfigFailed()
                }
            }
        }
    }

    private fun dealWithHumanDetect(dataJson: String?): Boolean {
        if (dataJson == null) {
            return false
        }
        val handleConfigData: HandleConfigData<*> = HandleConfigData<Any?>()
        if (handleConfigData.getDataObj(dataJson, HumanDetectionBean::class.java)) {
            currentHumanDetectionBean = handleConfigData.obj as HumanDetectionBean
            if (currentHumanDetectionBean != null) {
                if (iHumanDetectView != null) {
                    iHumanDetectView?.updateHumanDetectResult(true)
                    return true
                }
            }
        }
        return false
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

    private fun dealWithLocation(dataJson: String?) {
        try {
            if (dataJson == null) {
                return
            }
            val handleConfigData: HandleConfigData<*> = HandleConfigData<Any?>()
            if (handleConfigData.getDataObj(dataJson, LocationBean::class.java)) {
                locationBean = handleConfigData.obj as LocationBean
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun dealWithWhiteLight(dataJson: String?) {
        try {
            if (dataJson == null) {
                return
            }
            val handleConfigData: HandleConfigData<*> = HandleConfigData<Any?>()
            val handleConfigDataTemp: HandleConfigData<*> = HandleConfigData<Any?>()
            if (
                handleConfigData.getDataObj(dataJson, WhiteLightBean::class.java) &&
                handleConfigDataTemp.getDataObj(dataJson, WhiteLightBean::class.java)
            ) {
                whiteLight = handleConfigData.obj as WhiteLightBean
                whiteLightTemp = handleConfigDataTemp.obj as WhiteLightBean
                handleWhiteLightState()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun dealWithCameraParam(dataJson: String?) {
        try {
            if (dataJson == null) {
                return
            }
            val handleConfigData: HandleConfigData<*> = HandleConfigData<Any?>()
            val handleConfigDataTemp: HandleConfigData<*> = HandleConfigData<Any?>()
            if (
                handleConfigData.getDataObj(dataJson, CameraParamBean::class.java) &&
                handleConfigDataTemp.getDataObj(dataJson, CameraParamBean::class.java)
            ) {
                cameraParam = handleConfigData.obj as CameraParamBean
                cameraParamTemp = handleConfigDataTemp.obj as CameraParamBean
                FunSDK.DevGetConfigByJson(
                    userId, devId,
                    JsonConfig.WHITE_LIGHT, 1024, -1,
                    Define.GET_SET_DEV_CONFIG_TIMEOUT, 0
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun handleWhiteLightState() {
        when (Integer.decode(cameraParam?.DayNightColor ?: "0x1").toInt()) {
            OFF_WHITE_LIGHT -> {
                getWhiteLightState.value = WhiteLight.OFF
            }

            ON_WHITE_LIGHT -> {
                when (whiteLight?.workMode) {
                    WhiteLight.AUTO.value -> {
                        getWhiteLightState.value = WhiteLight.AUTO
                    }

                    WhiteLight.CLOSE.value -> {
                        getWhiteLightState.value = WhiteLight.CLOSE
                    }

                    WhiteLight.INTELLIGENT.value -> {
                        getWhiteLightState.value = WhiteLight.INTELLIGENT
                    }

                    else -> {
                        getWhiteLightState.value = WhiteLight.OFF
                    }
                }
            }
        }
    }

    private fun dealWithSystemTimezone(dataJson: String?) {
        try {
            if (dataJson == null) {
                return
            }
            val handleConfigData: HandleConfigData<*> = HandleConfigData<Any?>()
            if (handleConfigData.getDataObj(dataJson, TimeZoneBean::class.java)) {
                (handleConfigData.obj as? TimeZoneBean)?.let {
                    val timeZone = if (it.timeMin > 0) {
                        "GMT-" + abs(it.timeMin / 60)
                    } else {
                        "GMT+" + abs(it.timeMin / 60)
                    }
                    systemTimezone.value = timeZone
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun deleteDevice() {
        XMAccountManager.getInstance()?.deleteDev(devId, object :
            BaseAccountManager.OnAccountManagerListener {
            override fun onSuccess(msgId: Int) {
                Timber.tag(JFCameraManager.TAG).d("deleteDevice $devId and msgId:$msgId Success")
                isDeleteSuccessDeviceFromJF.postValue(true)
                viewModelScope.launch {
                    homeRepository.deleteDevice(deviceId).collect { resultResponse ->
                        _deleteResponse.value = resultResponse
                    }
                }
                DevDataCenter.getInstance()?.removeDev(devId)
            }

            override fun onFailed(msgId: Int, errorId: Int) {
                Timber.tag(JFCameraManager.TAG).d("deleteDevice $devId onFailed")
                isDeleteSuccessDeviceFromJF.postValue(false)
            }

            override fun onFunSDKResult(message: Message, msgContent: MsgContent) {
                Timber.d(message.toString())
            }
        })

    }

    override fun OnFunSDKResult(message: Message, msgContent: MsgContent): Int {
        Timber.tag(TAG).d("OnFunSDKResult")
        Timber.tag(TAG).d("message: ${message.what} - $message")
        Timber.tag(TAG).d("msgContent: ${Gson().toJson(msgContent)}")
        if (message.arg1 < 0) {
            if (iHumanDetectView != null) {
                iHumanDetectView?.saveHumanDetectResult(false)
            }
            return 0
        }
        when (message.what) {
            EUIMSG.DEV_GET_JSON -> {
                val jsonData = G.ToString(msgContent.pData)
                if (StringUtils.contrast(msgContent.str, JsonConfig.DETECT_HUMAN_DETECTION)) {
                    if (!dealWithHumanDetect(jsonData)) {
                        iHumanDetectView?.updateHumanDetectResult(false)
                    }
                    FunSDK.DevGetConfigByJson(
                        userId, devId, JsonConfig.CFG_DETECT_TRACK,
                        4096, -1, Define.GET_SET_DEV_CONFIG_TIMEOUT, 0
                    )
                } else if (StringUtils.contrast(msgContent.str, JsonConfig.CFG_DETECT_TRACK)
                    || StringUtils.contrast(
                        msgContent.str,
                        Define.DEV_CONFIG_PENETRATE_PREFIX + JsonConfig.CFG_DETECT_TRACK
                    )
                ) {
                    dealWithDetectTrack(jsonData)
                } else if (StringUtils.contrast(msgContent.str, JsonConfig.DETECT_MOTIONDETECT)) {
                    dealWithMotionDetect(jsonData)
                } else if (StringUtils.contrast(msgContent.str, JsonConfig.GENERAL_LOCATION)) {
                    dealWithLocation(jsonData)
                } else if (StringUtils.contrast(msgContent.str, JsonConfig.SYSTEM_TIMEZONE)) {
                    dealWithSystemTimezone(jsonData)
                } else if (StringUtils.contrast(msgContent.str, JsonConfig.WHITE_LIGHT)) {
                    dealWithWhiteLight(jsonData)
                } else if (StringUtils.contrast(msgContent.str, JsonConfig.CAMERA_PARAM)) {
                    dealWithCameraParam(jsonData)
                } else if (StringUtils.contrast(msgContent.str, DATE_FORMAT)) {
                    try {
                        dateFormatObject = JSON.parseObject(jsonData)
                        val dateFormat = dateFormatObject?.getString(DATE_FORMAT)
                        dateFormatState.value = DateFormatJF.valueOf(dateFormat)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                isLoading.value = false
            }

            EUIMSG.DEV_SET_JSON -> {
                if (StringUtils.contrast(msgContent.str, JsonConfig.DETECT_HUMAN_DETECTION)
                    || StringUtils.contrast(
                        HandleConfigData.getFullName(
                            JsonConfig.DETECT_HUMAN_DETECTION,
                            channel
                        ), msgContent.str
                    )
                ) {
                    if (iHumanDetectView != null) {
                        iHumanDetectView?.saveHumanDetectResult(true)
                    }
                } else if (StringUtils.contrast(JsonConfig.DETECT_MOTIONDETECT, msgContent.str)) {
                    if (iAlarmPushSetView != null) {
                        iAlarmPushSetView?.onSaveConfigResult(true)
                    }
                } else if (StringUtils.contrast(JsonConfig.OPTIME_SET, msgContent.str)) {
                    if (message.arg1 >= 0) {
                        syncSystemTimezoneState.value = true
                        getSystemTimezone()
                    } else {
                        syncSystemTimezoneState.value = false
                    }
                } else if (StringUtils.contrast(JsonConfig.WHITE_LIGHT, msgContent.str)) {
                    if (message.arg1 >= 0) {
                        whiteLight?.workMode = whiteLightTemp?.workMode
                        val dayNightColor =
                            Integer.decode(cameraParamTemp?.DayNightColor ?: "0x1").toInt()
                        if (dayNightColor == OFF_WHITE_LIGHT) {
                            FunSDK.DevSetConfigByJson(
                                userId, devId, JsonConfig.CAMERA_PARAM,
                                HandleConfigData.getSendData(
                                    HandleConfigData.getFullName(JsonConfig.CAMERA_PARAM, 0),
                                    "0x01",
                                    cameraParamTemp
                                ), 0, Define.GET_SET_DEV_CONFIG_TIMEOUT, 0
                            )
                        } else {
                            setWhiteLightState.value = true
                            handleWhiteLightState()
                            isLoading.value = false
                        }
                    } else {
                        whiteLightTemp?.workMode = whiteLight?.workMode
                        setWhiteLightState.value = false
                        handleWhiteLightState()
                        isLoading.value = false
                    }
                } else if (StringUtils.contrast(JsonConfig.CAMERA_PARAM, msgContent.str)) {
                    if (message.arg1 >= 0) {
                        cameraParam?.DayNightColor = cameraParamTemp?.DayNightColor
                        val dayNightColor =
                            Integer.decode(cameraParamTemp?.DayNightColor ?: "0x1").toInt()
                        if (dayNightColor == ON_WHITE_LIGHT) {
                            FunSDK.DevSetConfigByJson(
                                userId, devId, JsonConfig.WHITE_LIGHT,
                                HandleConfigData.getSendData(
                                    JsonConfig.WHITE_LIGHT,
                                    "0x01",
                                    whiteLightTemp
                                ), -1, Define.GET_SET_DEV_CONFIG_TIMEOUT, 0
                            )
                        } else {
                            setWhiteLightState.value = true
                            handleWhiteLightState()
                            isLoading.value = false
                        }
                    } else {
                        cameraParamTemp?.DayNightColor = cameraParam?.DayNightColor
                        setWhiteLightState.value = false
                        handleWhiteLightState()
                        isLoading.value = false
                    }
                } else if (StringUtils.contrast(DATE_FORMAT, msgContent.str)) {
                    if (message.arg1 >= 0) {
                        val dateFormat = dateFormatObject?.getString(DATE_FORMAT)
                        dateFormatState.value = DateFormatJF.valueOf(dateFormat)
                    } else {
                        dateFormatObject?.put(DATE_FORMAT, dateFormatState.value?.valueInJF)
                        updateDateFormatState.value = false
                    }
                    isLoading.value = false
                }
            }

            EUIMSG.DEV_CMD_EN -> {
                if (StringUtils.contrast(msgContent.str, JsonConfig.HUMAN_RULE_LIMIT)
                    || StringUtils.contrast(
                        msgContent.str,
                        HandleConfigData.getFullName("ChannelHumanRuleLimit", channel)
                    )
                ) {
                    val handleConfigData: HandleConfigData<*> = HandleConfigData<Any?>()
                    if (handleConfigData.getDataObj(
                            G.ToString(msgContent.pData),
                            ChannelHumanRuleLimitBean::class.java
                        )
                    ) {
                        channelHumanRuleLimitBean =
                            handleConfigData.obj as ChannelHumanRuleLimitBean
                        FunSDK.DevGetConfigByJson(
                            userId, devId, JsonConfig.DETECT_HUMAN_DETECTION,
                            4096, channel, Define.GET_SET_DEV_CONFIG_TIMEOUT, 0
                        )
                    } else {
                        if (iHumanDetectView != null) {
                            iHumanDetectView?.updateHumanDetectResult(false)
                        }
                    }
                }
            }

            EUIMSG.AS_OPT_ALARM_MSG_DO_NOT_DISTURB -> {
                if (msgContent.seq == 1) {
                    if (message.arg1 >= 0) {
                        val jsonData = msgContent.str
                        if (jsonData != null && jsonData.isNotEmpty()) {
                            noDisturbingAlarmTimeInfoBean = Gson().fromJson(
                                jsonData,
                                NoDisturbingAlarmTimeInfoBean::class.java
                            )
                            noDisturbingAlarmTimeInfoBeanTemp =
                                noDisturbingAlarmTimeInfoBean?.clone()
                            refreshSmartSettingNotificationAlarmCamera()
                            Timber.tag(TAG).d("get no disturbing alarm config success")
                        } else {
                            Timber.tag(TAG).d("get no disturbing alarm config empty")
                        }
                    } else {
                        Timber.tag(TAG)
                            .d("get no disturbing alarm config error,arg1=${message.arg1}")
                    }
                } else if (msgContent.seq == 2) {
                    if (message.arg1 >= 0) {
                        noDisturbingAlarmTimeInfoBean = noDisturbingAlarmTimeInfoBeanTemp?.clone()
                        refreshSmartSettingNotificationAlarmCamera()
                        Timber.tag(TAG).d("set no disturbing alarm config success")
                    } else {
                        noDisturbingAlarmTimeInfoBeanTemp = noDisturbingAlarmTimeInfoBean?.clone()
                        updateAlarmInfoState.value = false
                        Timber.tag(TAG)
                            .d("set no disturbing alarm config error,arg1=${message.arg1}")
                    }
                } else if (msgContent.seq == 3) {
                    if (message.arg1 >= 0) {
                        Timber.tag(TAG).d("delete no disturbing alarm config success")
                    } else {
                        Timber.tag(TAG)
                            .d("delete no disturbing alarm config error,arg1=${message.arg1}")
                    }
                }
                isLoading.value = false
            }

            else -> {

            }
        }
        return 0
    }

    fun setWhiteLightState(whiteLight: WhiteLight) {
        when (whiteLight) {
            WhiteLight.AUTO, WhiteLight.CLOSE, WhiteLight.INTELLIGENT -> {
                whiteLightTemp?.workMode = whiteLight.value
                cameraParamTemp?.DayNightColor = String.format("%#x", ON_WHITE_LIGHT)
                FunSDK.DevSetConfigByJson(
                    userId, devId, JsonConfig.CAMERA_PARAM,
                    HandleConfigData.getSendData(
                        HandleConfigData.getFullName(JsonConfig.CAMERA_PARAM, 0),
                        "0x01",
                        cameraParamTemp
                    ), 0, Define.GET_SET_DEV_CONFIG_TIMEOUT, 0
                )
                isLoading.value = true
            }

            WhiteLight.OFF -> {
                whiteLightTemp?.workMode = WhiteLight.AUTO.value
                cameraParamTemp?.DayNightColor = String.format("%#x", OFF_WHITE_LIGHT)
                FunSDK.DevSetConfigByJson(
                    userId, devId, JsonConfig.WHITE_LIGHT,
                    HandleConfigData.getSendData(
                        JsonConfig.WHITE_LIGHT,
                        "0x01",
                        whiteLightTemp
                    ), -1, Define.GET_SET_DEV_CONFIG_TIMEOUT, 0
                )
                isLoading.value = true
            }
        }
    }

    fun rebootDev(callback: (Boolean) -> Unit) {
        JFCameraManager.rebootDev(devId, callback)
    }


    fun release() {
        FunSDK.UnRegUser(userId)
    }

    override fun onCleared() {
        super.onCleared()
        release()
    }

    private fun refreshSmartSettingNotificationAlarmCamera() {
        viewModelScope.launch {
            var result = SmartSettingNotificationCameraJF.OFF
            noDisturbingAlarmTimeInfoBean?.let {
                result = when (it.type) {
                    0 -> SmartSettingNotificationCameraJF.OFF
                    1 -> SmartSettingNotificationCameraJF.ALL_DAY_24H
                    2 -> SmartSettingNotificationCameraJF.SCHEDULE
                    else -> SmartSettingNotificationCameraJF.OFF
                }
            }
            notificationAlarm.value = result
        }
    }

    fun setNotificationAlarmEnable(notification: SmartSettingNotificationCameraJF) {
        if (this::devId.isInitialized && noDisturbingAlarmTimeInfoBeanTemp != null) {
            isLoading.value = true
            noDisturbingAlarmTimeInfoBeanTemp?.type = when (notification) {
                SmartSettingNotificationCameraJF.OFF -> 0
                SmartSettingNotificationCameraJF.ALL_DAY_24H -> 1
                SmartSettingNotificationCameraJF.SCHEDULE -> 2
            }
            val infoBean = NoDisturbingAlarmTimeInfoBean()
            infoBean.msg = SET_ALARM_TIME
            infoBean.sn = devId
            infoBean.type = noDisturbingAlarmTimeInfoBeanTemp?.type ?: 0
            infoBean.ts = noDisturbingAlarmTimeInfoBeanTemp?.ts
            AlarmService.OptAlarmMsgDoNotDisturb(userId, Gson().toJson(infoBean), 2)
        } else {
            updateAlarmInfoState.value = false
        }
    }

    fun setDateFormatState(dateFormatJF: DateFormatJF) {
        viewModelScope.launch {
            dateFormatObject?.let { dateFormatObj ->
                isLoading.value = true
                dateFormatObj[DATE_FORMAT] = dateFormatJF.valueInJF
                FunSDK.DevSetConfigByJson(
                    userId, devId, DATE_FORMAT,
                    dateFormatObj.toJSONString(), 0,
                    Define.GET_SET_DEV_CONFIG_TIMEOUT, 0
                )
            }
        }
    }

    private val _responseCheckSpreadCamera = SingleLiveEvent<com.vht.sdkcore.network.Result<ResponseCheckDeviceSpread>>()
    val dataResponseCheckSpreadCamera: SingleLiveEvent<com.vht.sdkcore.network.Result<ResponseCheckDeviceSpread>> get() = _responseCheckSpreadCamera

    fun getCheckSpreadCamera(deviceId: String) {
        viewModelScope.launch {
            homeRepository.checkDeviceSpread(
            deviceId
            ).collect {
                _responseCheckSpreadCamera.value = it
            }
        }
    }

    private val _dataCheckInvitationCode =
        SingleLiveEvent<com.vht.sdkcore.network.Result<FeatureConfigResponse>>()
    val dataCheckInvitationCode: SingleLiveEvent<com.vht.sdkcore.network.Result<FeatureConfigResponse>> get() = _dataCheckInvitationCode

    fun getCheckInvitationCode(){
        viewModelScope.launch {
            homeRepository.getConfigFeatureList().collect{ result ->
                _dataCheckInvitationCode.value = result
            }
        }
    }


    companion object {
        private val TAG: String = SettingCameraJFViewModel::class.java.simpleName
        private const val ON_WHITE_LIGHT = 3
        private const val OFF_WHITE_LIGHT = 1
        private const val GET_ALARM_TIME = "get_alarm_time"
        private const val SET_ALARM_TIME = "set_alarm_time"

        private const val DATE_FORMAT = "General.Location.DateFormat"
    }
}