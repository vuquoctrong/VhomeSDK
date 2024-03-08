package com.viettel.vht.sdk.ui.jfcameradetail.alarm_voice_period

import android.os.Message
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.basic.G
import com.google.gson.Gson
import com.lib.*
import com.lib.sdk.bean.StringUtils
import com.manager.device.DeviceManager
import com.manager.device.config.DevConfigManager
import com.vht.sdkcore.base.BaseViewModel
import com.vht.sdkcore.utils.Define
import com.vht.sdkcore.utils.SingleLiveEvent
import com.viettel.vht.sdk.ui.jfcameradetail.alarm_voice_period.AddAlarmVoicePeriodJFFragment.Companion.ARG_ID_SCHEDULE_PARAM
import com.viettel.vht.sdk.ui.jfcameradetail.alarm_voice_period.model.AlarmVoicePeriodResponse
import com.viettel.vht.sdk.ui.jfcameradetail.schedule_alarm.model.TimeItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class AddAlarmVoicePeriodJFViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : BaseViewModel(), IFunSDKResult {

    private val devId: String
    private var scheduleId: Int
    private val deviceManager: DeviceManager
    private lateinit var devConfigManager: DevConfigManager

    private var userId = 0

    private var alarmVoicePeriodResponse: AlarmVoicePeriodResponse? = null

    private val dayOfWeek = mutableListOf(true, true, true, true, true, true, true)
    val dayOfWeekSelected = SingleLiveEvent<List<Boolean>>().apply { value = dayOfWeek }

    private val timeList = mutableListOf("00", "00", "00", "23", "59", "59")
    val timeListData = SingleLiveEvent<List<String>>().apply { value = timeList }

    val saveScheduleState = SingleLiveEvent<Boolean>()
    val getScheduleState = SingleLiveEvent<Boolean>()

    val error = SingleLiveEvent<String>()

    init {
        userId = FunSDK.GetId(userId, this)
        devId = savedStateHandle[Define.BUNDLE_KEY.PARAM_ID] ?: ""
        scheduleId = savedStateHandle[ARG_ID_SCHEDULE_PARAM] ?: -1
        deviceManager = DeviceManager.getInstance()
    }

    fun getConfig() {
        isLoading.value = true
        if (isSupportAlarmVoicePeriod(devId)) {
            FunSDK.DevCmdGeneral(
                userId, devId,
                GET_ALARM_VOICE_PERIOD_CMD_ID,
                ALARM_VOICE_PERIOD, -1,
                Define.GET_SET_DEV_CONFIG_TIMEOUT, null, 0, 0
            )
        }
        devConfigManager = deviceManager.getDevConfigManager(devId)
    }

    private fun isSupportAlarmVoicePeriod(devId: String): Boolean {
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
                        GET_ALARM_VOICE_PERIOD_CMD_ID -> {
                            try {
                                alarmVoicePeriodResponse =
                                    Gson().fromJson(
                                        G.ToString(msgContent.pData),
                                        AlarmVoicePeriodResponse::class.java
                                    )
                                getScheduleAlarmSetting()
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }

                        SET_ALARM_VOICE_PERIOD_CMD_ID -> {
                            saveScheduleState.value = message.arg1 >= 0
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

    private fun getScheduleAlarmSetting() {
        alarmVoicePeriodResponse?.let { response ->
            findScheduleId()
            dayOfWeek.clear()
            dayOfWeek.addAll(listOf(false, false, false, false, false, false, false))
            for (j in 0 until SDKCONST.NET_N_WEEKS) {
                val schedule = response.listAlarmVoicePeriod[0][j][scheduleId]
                val eachTime = mutableListOf<String>()
                eachTime.addAll(schedule.split("\\W+".toRegex()))
                eachTime.takeIf { eachTime.size == 7 }?.let {
                    if (
                        StringUtils.contrast(it[0], TimeItem.SCHEDULE_ALARM_OPEN_STATE.toString())
                    ) {
                        dayOfWeek[j] = true
                        timeList[0] = it[1]
                        timeList[1] = it[2]
                        timeList[2] = it[3]
                        if (it[4] == "24") {
                            timeList[3] = "23"
                            timeList[4] = "59"
                            timeList[5] = "59"
                        } else {
                            timeList[3] = it[4]
                            timeList[4] = it[5]
                            timeList[5] = it[6]
                        }
                    }
                }
            }
            var isOpen = false
            dayOfWeek.forEach {
                if (it) isOpen = true
            }
            if (!isOpen) {
                dayOfWeek.clear()
                dayOfWeek.addAll(listOf(true, true, true, true, true, true, true))
            }
            dayOfWeekSelected.value = dayOfWeek
            timeListData.value = timeList
        } ?: run {
            getScheduleState.value = false
        }
    }

    private fun findScheduleId() {
        if (scheduleId >= 0) return
        for (i in 1 until (TimeItem.TOTAL_SCHEDULE_ALARM + 1)) {
            var isScheduleEmpty = true
            for (j in 0 until SDKCONST.NET_N_WEEKS) {
                alarmVoicePeriodResponse?.listAlarmVoicePeriod?.get(0)?.get(j)?.get(i)?.let {
                    if (it != ALL_DAY_CLOSE) {
                        isScheduleEmpty = false
                    }
                }
            }
            if (isScheduleEmpty) {
                scheduleId = i
                return
            }
        }
    }

    private fun saveAlarmVoicePeriod() {
        isLoading.value = true
        FunSDK.DevCmdGeneral(
            userId, devId,
            SET_ALARM_VOICE_PERIOD_CMD_ID,
            ALARM_VOICE_PERIOD, -1,
            Define.GET_SET_DEV_CONFIG_TIMEOUT,
            Gson().toJson(alarmVoicePeriodResponse).toByteArray(), 0, 0
        )
    }

    fun selectDayOfWeek(position: Int) {
        dayOfWeek[position] = !dayOfWeek[position]
        dayOfWeekSelected.value = dayOfWeek
    }

    fun selectTime(time: List<String>) {
        timeList[0] = time[0]
        timeList[1] = time[1]
        timeList[3] = time[2]
        timeList[4] = time[3]
        timeListData.value = timeList
    }

    fun saveSchedule() = viewModelScope.launch {
        if (scheduleId < 0) return@launch
        if (!validDayOfWeek()) {
            return@launch
        }
        if (!validTime()) {
            return@launch
        }
        alarmVoicePeriodResponse?.listAlarmVoicePeriod?.get(0)?.apply {
            for (i in 0 until SDKCONST.NET_N_WEEKS) {
                if (dayOfWeek[i]) {
                    this[i][scheduleId] = String.format(
                        ALARM_VOICE_PERIOD_FORMAT,
                        TimeItem.SCHEDULE_ALARM_OPEN_STATE.toString(),
                        timeList[0], timeList[1], timeList[2], timeList[3], timeList[4], timeList[5]
                    )
                } else {
                    this[i][scheduleId] = ALL_DAY_CLOSE
                }
            }
            saveAlarmVoicePeriod()
        }
    }

    private fun validTime(): Boolean {
        var isTimeValid = false
        try {
            isTimeValid = when {
                timeList[0].toInt() > timeList[3].toInt() -> false
                timeList[0].toInt() == timeList[3].toInt() && timeList[1].toInt() >= timeList[4].toInt() -> false
                timeList[0].toInt() == timeList[3].toInt() && timeList[1].toInt() < timeList[4].toInt() -> true
                timeList[0].toInt() < timeList[3].toInt() -> true
                else -> false
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (!isTimeValid) {
            error.value = "Thời gian bắt đầu phải nhỏ hơn thời gian kết thúc"
        }
        return isTimeValid
    }

    private fun validDayOfWeek(): Boolean {
        var isOpen = false
        dayOfWeek.forEach {
            if (it) isOpen = true
        }
        if (!isOpen) {
            error.value = "Vui lòng chọn ngày lặp lại"
        }
        return isOpen
    }

    companion object {
        private val TAG: String = AddAlarmVoicePeriodJFFragment::class.java.simpleName

        private const val ALARM_VOICE_PERIOD = "Detect.AlarmVoicePeriod"
        private const val GET_ALARM_VOICE_PERIOD_CMD_ID = 1042
        private const val SET_ALARM_VOICE_PERIOD_CMD_ID = 1040
        private const val ALL_DAY_CLOSE = "0 00:00:00-24:00:00"
        private const val ALARM_VOICE_PERIOD_FORMAT = "%s %s:%s:%s-%s:%s:%s"
    }
}
