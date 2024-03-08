package com.viettel.vht.sdk.ui.jfcameradetail.alarm_voice_period

import android.os.Message
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.basic.G
import com.google.gson.Gson
import com.lib.EUIMSG
import com.lib.FunSDK
import com.lib.IFunSDKResult
import com.lib.MsgContent
import com.lib.SDKCONST
import com.lib.sdk.bean.StringUtils
import com.manager.device.DeviceManager
import com.manager.device.config.DevConfigManager
import com.vht.sdkcore.base.BaseViewModel
import com.vht.sdkcore.utils.Define
import com.vht.sdkcore.utils.SingleLiveEvent
import com.viettel.vht.sdk.ui.jfcameradetail.alarm_voice_period.model.AlarmVoicePeriodResponse
import com.viettel.vht.sdk.ui.jfcameradetail.schedule_alarm.model.TimeItem
import com.viettel.vht.sdk.utils.AlarmVoicePeriodType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ListAlarmVoicePeriodJFViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : BaseViewModel(), IFunSDKResult {

    val devId: String
    private val deviceManager: DeviceManager
    private lateinit var devConfigManager: DevConfigManager
    var alarmVoicePeriodResponse: AlarmVoicePeriodResponse? = null
    private var alarmVoicePeriodResponseTemp: AlarmVoicePeriodResponse? = null

    private var userId = 0

    private val alarmScheduleTimeList = mutableListOf<TimeItem>()
    val scheduleList = SingleLiveEvent<List<TimeItem>>()

    val deleteState = SingleLiveEvent<Boolean>().apply { value = false }
    val selectAllDeleteState = SingleLiveEvent<Boolean>().apply { value = false }
    val selectDeleteStateCount = SingleLiveEvent<Int>().apply { value = 0 }

    val typeAlarmVoicePeriod = SingleLiveEvent<AlarmVoicePeriodType>()
        .apply { value = AlarmVoicePeriodType.ALWAYS }

    init {
        userId = FunSDK.GetId(userId, this)
        devId = savedStateHandle[Define.BUNDLE_KEY.PARAM_ID] ?: ""
        deviceManager = DeviceManager.getInstance()
    }

    fun getConfig() {
        if (isSupportAlarmVoicePeriod()) {
            isLoading.value = true
            FunSDK.DevCmdGeneral(
                userId, devId,
                GET_ALARM_VOICE_PERIOD_CMD_ID,
                ALARM_VOICE_PERIOD, -1,
                Define.GET_SET_DEV_CONFIG_TIMEOUT, null, 0, 0
            )
        }
        devConfigManager = deviceManager.getDevConfigManager(devId)
    }

    fun isSupportAlarmVoicePeriod(): Boolean {
        return FunSDK.GetDevAbility(devId, "OtherFunction/SupportControlAlarmVoicePeriod") > 0
    }

    override fun OnFunSDKResult(message: Message, msgContent: MsgContent): Int {
        Timber.tag(TAG).d("OnFunSDKResult")
        Timber.tag(TAG).d("message: ${message.what} - $message")
        Timber.tag(TAG).d("msgContent: ${Gson().toJson(msgContent)}")
        when (message.what) {
            EUIMSG.DEV_CMD_EN -> {
                if (ALARM_VOICE_PERIOD == msgContent.str) {
                    when (msgContent.arg3) {
                        GET_ALARM_VOICE_PERIOD_CMD_ID ->
                            handleAlarmVoicePeriodResponse(G.ToString(msgContent.pData))

                        SET_ALARM_VOICE_PERIOD_CMD_ID -> {
                            if (message.arg1 >= 0) {
                                alarmVoicePeriodResponse = alarmVoicePeriodResponseTemp?.copy()
                                checkTypeAlarmVoicePeriod()
                            } else {
                                alarmVoicePeriodResponseTemp = alarmVoicePeriodResponse?.copy()
                                getListScheduleAlarmSetting()
                            }
                        }

                        else -> Unit
                    }
                    isLoading.value = false
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

    private fun handleAlarmVoicePeriodResponse(dataJson: String?) {
        try {
            alarmVoicePeriodResponse =
                Gson().fromJson(dataJson, AlarmVoicePeriodResponse::class.java)
            alarmVoicePeriodResponseTemp = alarmVoicePeriodResponse?.copy()
            getListScheduleAlarmSetting()
            checkTypeAlarmVoicePeriod()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun checkTypeAlarmVoicePeriod() {
        alarmVoicePeriodResponse?.let {
            if (it.listAlarmVoicePeriod[0][0][0] == ALL_DAY_OPEN) {
                typeAlarmVoicePeriod.value = AlarmVoicePeriodType.ALWAYS
            } else {
                typeAlarmVoicePeriod.value = AlarmVoicePeriodType.SCHEDULE
            }
        }
    }

    fun setTypeAlarmVoicePeriod(type: AlarmVoicePeriodType) {
        if (typeAlarmVoicePeriod.value == type) return
        alarmVoicePeriodResponseTemp?.apply {
            val result = if (type == AlarmVoicePeriodType.ALWAYS) ALL_DAY_OPEN else ALL_DAY_CLOSE
            for (i in 0 until SDKCONST.NET_N_WEEKS) {
                listAlarmVoicePeriod[0][i][0] = result
            }
            saveAlarmVoicePeriod()
        }
    }

    private fun getListScheduleAlarmSetting() = viewModelScope.launch {
        alarmVoicePeriodResponse?.let { response ->
            alarmScheduleTimeList.clear()
            for (i in 1 until (TimeItem.TOTAL_SCHEDULE_ALARM + 1)) {
                val info = TimeItem(position = i, stateDelete = false)
                for (j in 0 until SDKCONST.NET_N_WEEKS) {
                    val eachTime = mutableListOf<String>()
                    val time = response.listAlarmVoicePeriod[0][j][i]
                    if (time != ALL_DAY_CLOSE) {
                        eachTime.addAll(time.split("\\W+".toRegex()))
                        eachTime.takeIf { eachTime.size == 7 }?.let {
                            if (StringUtils.contrast(
                                    it[0],
                                    TimeItem.SCHEDULE_ALARM_OPEN_STATE.toString()
                                )
                            ) {
                                info.isOpen = true
                            }
                            if (it[4] == "24") {
                                info.time = it[1] + ":" + it[2] + "-" + "23" + ":" + "59"
                            } else {
                                info.time = it[1] + ":" + it[2] + "-" + it[4] + ":" + it[5]
                            }
                        }
                        info.dayOfWeek[j] = true
                    } else {
                        info.dayOfWeek[j] = false
                    }
                }
                if (info.time != TimeItem.ALL_DAY_TIME) {
                    alarmScheduleTimeList.add(info)
                }
            }
            alarmScheduleTimeList.sortBy { it.position }
            scheduleList.value = alarmScheduleTimeList
        } ?: run {
            scheduleList.value = emptyList()
        }
    }

    private fun saveAlarmVoicePeriod() {
        isLoading.value = true
        FunSDK.DevCmdGeneral(
            userId, devId,
            SET_ALARM_VOICE_PERIOD_CMD_ID,
            ALARM_VOICE_PERIOD, -1,
            Define.GET_SET_DEV_CONFIG_TIMEOUT,
            Gson().toJson(alarmVoicePeriodResponseTemp).toByteArray(), 0, 0
        )
    }

    fun changeDeleteState(enable: Boolean) {
        alarmScheduleTimeList.forEach {
            if (!enable) {
                it.isSelectDelete = false
            }
            it.stateDelete = enable
        }
        deleteState.value = enable
        scheduleList.value = alarmScheduleTimeList
        selectAllDeleteState.value = false
        selectDeleteStateCount.value = 0
    }

    fun changeOnOffStateSchedule(state: Boolean, item: TimeItem) {
        alarmVoicePeriodResponseTemp?.apply {
            val eachTime = mutableListOf<String>()
            eachTime.addAll(item.time.split("\\W+".toRegex()))
            for (i in 0 until SDKCONST.NET_N_WEEKS) {
                if (item.dayOfWeek[i]) {
                    eachTime.takeIf { eachTime.size == 4 }?.let {
                        listAlarmVoicePeriod[0][i][item.position] = String.format(
                            ALARM_VOICE_PERIOD_FORMAT,
                            if (state) TimeItem.SCHEDULE_ALARM_OPEN_STATE.toString()
                            else TimeItem.SCHEDULE_ALARM_CLOSE_STATE.toString(),
                            it[0], it[1], "00", it[2], it[3], "59"
                        )
                    }
                }
            }
            saveAlarmVoicePeriod()
        }
    }

    fun changeSelectedDeleteStateSchedule(state: Boolean, item: TimeItem) {
        var isSelectAll = true
        var selectCount = 0
        alarmScheduleTimeList.forEach {
            if (it.id == item.id) {
                it.isSelectDelete = state
            }
            if (it.isSelectDelete) {
                selectCount++
            } else {
                isSelectAll = false
            }
        }
        scheduleList.value = alarmScheduleTimeList
        selectAllDeleteState.value = isSelectAll
        selectDeleteStateCount.value = selectCount
    }

    fun selectedDeleteStateAllSchedule(state: Boolean) {
        var selectCount = 0
        alarmScheduleTimeList.forEach {
            it.isSelectDelete = state
            if (it.isSelectDelete) {
                selectCount++
            }
        }
        scheduleList.value = alarmScheduleTimeList
        selectAllDeleteState.value = state
        selectDeleteStateCount.value = selectCount
    }

    fun clearSchedule() {
        val alarmScheduleTimeListTemp = alarmScheduleTimeList.toMutableList()
        alarmScheduleTimeList.forEach {
            if (it.isSelectDelete) {
                alarmScheduleTimeListTemp.remove(it)
            }
            it.stateDelete = false
        }
        alarmScheduleTimeListTemp.forEachIndexed { index, timeItem ->
            timeItem.position = index + 1
        }
        alarmVoicePeriodResponseTemp?.listAlarmVoicePeriod?.get(0)?.apply {
            for (i in 1 until (TimeItem.TOTAL_SCHEDULE_ALARM + 1)) {
                for (j in 0 until SDKCONST.NET_N_WEEKS) {
                    this[j][i] = ALL_DAY_CLOSE
                }
            }
            alarmScheduleTimeListTemp.forEach { item ->
                val eachTime = mutableListOf<String>()
                eachTime.addAll(item.time.split("\\W+".toRegex()))
                for (i in 0 until SDKCONST.NET_N_WEEKS) {
                    if (item.dayOfWeek[i]) {
                        eachTime.takeIf { eachTime.size == 4 }?.let {
                            this[i][item.position] = String.format(
                                ALARM_VOICE_PERIOD_FORMAT,
                                if (item.isOpen) TimeItem.SCHEDULE_ALARM_OPEN_STATE.toString()
                                else TimeItem.SCHEDULE_ALARM_CLOSE_STATE.toString(),
                                it[0], it[1], "00", it[2], it[3], "59"
                            )
                        }
                    } else {
                        this[i][item.position] = ALL_DAY_CLOSE
                    }
                }
            }
        }
        alarmScheduleTimeList.clear()
        alarmScheduleTimeList.addAll(alarmScheduleTimeListTemp)
        deleteState.value = false
        scheduleList.value = alarmScheduleTimeList
        selectAllDeleteState.value = false
        selectDeleteStateCount.value = 0
        saveAlarmVoicePeriod()
    }

    companion object {
        private val TAG: String = ListAlarmVoicePeriodJFViewModel::class.java.simpleName

        private const val ALARM_VOICE_PERIOD = "Detect.AlarmVoicePeriod"
        private const val GET_ALARM_VOICE_PERIOD_CMD_ID = 1042
        private const val SET_ALARM_VOICE_PERIOD_CMD_ID = 1040
        private const val ALL_DAY_CLOSE = "0 00:00:00-24:00:00"
        private const val ALL_DAY_OPEN = "1 00:00:00-24:00:00"
        private const val ALARM_VOICE_PERIOD_FORMAT = "%s %s:%s:%s-%s:%s:%s"
    }
}
