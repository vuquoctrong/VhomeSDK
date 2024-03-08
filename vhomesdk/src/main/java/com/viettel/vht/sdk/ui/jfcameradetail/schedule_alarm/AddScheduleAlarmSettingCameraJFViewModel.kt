package com.viettel.vht.sdk.ui.jfcameradetail.schedule_alarm

import android.os.Message
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.lib.*
import com.lib.AS.AlarmService
import com.manager.device.DeviceManager
import com.manager.device.config.DevConfigManager
import com.vht.sdkcore.base.BaseViewModel
import com.vht.sdkcore.utils.SingleLiveEvent
import com.viettel.vht.sdk.model.jfcall.NoDisturbingAlarmTimeInfoBean
import com.viettel.vht.sdk.ui.jfcameradetail.schedule_alarm.model.TimeItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class AddScheduleAlarmSettingCameraJFViewModel @Inject constructor() : BaseViewModel(),
    IFunSDKResult {

    lateinit var devId: String
    var scheduleId: Int = -1
    private val deviceManager: DeviceManager
    private lateinit var devConfigManager: DevConfigManager

    private var userId = 0

    private var noDisturbingAlarmTimeInfoBean: NoDisturbingAlarmTimeInfoBean? = null

    private val dayOfWeek = mutableListOf(true, true, true, true, true, true, true)
    val dayOfWeekSelected = SingleLiveEvent<List<Boolean>>().apply { value = dayOfWeek }

    private val timeList = mutableListOf("00", "00", "00", "23", "59", "59")
    val timeListData = SingleLiveEvent<List<String>>().apply { value = timeList }

    val saveScheduleState = SingleLiveEvent<Boolean>()
    val getScheduleState = SingleLiveEvent<Boolean>()

    val error = SingleLiveEvent<String>()

    init {
        userId = FunSDK.GetId(userId, this)
        deviceManager = DeviceManager.getInstance()
    }

    fun getConfig() {
        if (!this::devId.isInitialized) return
        isLoading.value = true
        getConfigNoDisturbingAlarmTime()
        devConfigManager = deviceManager.getDevConfigManager(devId)
    }

    // request get no disturbing alarm config
    private fun getConfigNoDisturbingAlarmTime() {
        Timber.tag(TAG).d("request get no disturbing alarm config,devId=$devId")
        val noDisturbingAlarmTimeInfoBean = NoDisturbingAlarmTimeInfoBean()
        noDisturbingAlarmTimeInfoBean.msg = GET_ALARM_TIME
        noDisturbingAlarmTimeInfoBean.sn = devId
        AlarmService.OptAlarmMsgDoNotDisturb(
            userId,
            Gson().toJson(noDisturbingAlarmTimeInfoBean),
            1
        )
    }

    override fun OnFunSDKResult(message: Message, msgContent: MsgContent): Int {
        Timber.tag(TAG).d("OnFunSDKResult")
        Timber.tag(TAG).d("message: ${message.what} - $message")
        Timber.tag(TAG).d("msgContent: ${Gson().toJson(msgContent)}")
        when (message.what) {
            EUIMSG.AS_OPT_ALARM_MSG_DO_NOT_DISTURB -> {
                if (msgContent.seq == 1) {
                    if (message.arg1 >= 0) {
                        val jsonData = msgContent.str
                        if (jsonData != null && jsonData.isNotEmpty()) {
                            noDisturbingAlarmTimeInfoBean = Gson().fromJson(
                                jsonData,
                                NoDisturbingAlarmTimeInfoBean::class.java
                            )
                            getScheduleAlarmSetting()
                            Timber.tag(TAG).d("get no disturbing alarm config success")
                        } else {
                            Timber.tag(TAG).d("get no disturbing alarm config empty")
                        }
                    } else {
                        Timber.tag(TAG)
                            .d("get no disturbing alarm config error,arg1=${message.arg1}")
                    }
                } else if (msgContent.seq == 2) {
                    saveScheduleState.value = message.arg1 >= 0
                    if (message.arg1 >= 0) {
                        Timber.tag(TAG).d("set no disturbing alarm config success")
                    } else {
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

    override fun onCleared() {
        super.onCleared()
        FunSDK.UnRegUser(userId)
    }

    private fun getScheduleAlarmSetting() {
        if (noDisturbingAlarmTimeInfoBean != null) {
            findScheduleId()
            dayOfWeek.clear()
            dayOfWeek.addAll(listOf(false, false, false, false, false, false, false))
            for (j in 0 until SDKCONST.NET_N_WEEKS) {
                val schedule =
                    noDisturbingAlarmTimeInfoBean?.ts?.get(j)?.get(scheduleId) ?: continue
                if (schedule.en == 1) {
                    dayOfWeek[j] = true
                }
                val eachTimeStart = mutableListOf<String>()
                eachTimeStart.addAll(schedule.st.split("\\W+".toRegex()))
                timeList[0] = eachTimeStart.getOrNull(0) ?: "00"
                timeList[1] = eachTimeStart.getOrNull(1) ?: "00"
                timeList[2] = eachTimeStart.getOrNull(2) ?: "00"

                if (schedule.et == "24:00:00") {
                    timeList[3] = "23"
                    timeList[4] = "59"
                    timeList[5] = "59"
                } else {
                    val eachTimeEnd = mutableListOf<String>()
                    eachTimeEnd.addAll(schedule.et.split("\\W+".toRegex()))
                    timeList[3] = eachTimeEnd.getOrNull(0) ?: "23"
                    timeList[4] = eachTimeEnd.getOrNull(1) ?: "59"
                    timeList[5] = eachTimeEnd.getOrNull(2) ?: "59"
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
        } else {
            getScheduleState.value = false
        }
    }

    private fun findScheduleId() {
        if (scheduleId >= 0) return
        for (i in 0 until TimeItem.TOTAL_SCHEDULE_ALARM) {
            var isScheduleEmpty = true
            for (j in 0 until SDKCONST.NET_N_WEEKS) {
                noDisturbingAlarmTimeInfoBean?.ts?.get(j)?.get(i)?.let {
                    if (it.et != "24:00:00") {
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

    private fun saveAlarmDetect() {
        if (this::devId.isInitialized && noDisturbingAlarmTimeInfoBean != null) {
            isLoading.value = true
            val infoBean = NoDisturbingAlarmTimeInfoBean()
            infoBean.msg = SET_ALARM_TIME
            infoBean.sn = devId
            infoBean.type = noDisturbingAlarmTimeInfoBean?.type ?: 0
            infoBean.ts = noDisturbingAlarmTimeInfoBean?.ts
            AlarmService.OptAlarmMsgDoNotDisturb(userId, Gson().toJson(infoBean), 2)
        }
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
        noDisturbingAlarmTimeInfoBean?.ts?.apply {
            for (i in 0 until SDKCONST.NET_N_WEEKS) {
                if (dayOfWeek[i]) {
                    this[i][scheduleId].en = TimeItem.SCHEDULE_ALARM_OPEN_STATE
                    this[i][scheduleId].st = String.format(
                        TimeItem.SCHEDULE_ALARM_TIME_FORMAT,
                        timeList[0], timeList[1], timeList[2]
                    )
                    this[i][scheduleId].et = String.format(
                        TimeItem.SCHEDULE_ALARM_TIME_FORMAT,
                        timeList[3], timeList[4], timeList[5]
                    )
                } else {
                    this[i][scheduleId].en = TimeItem.SCHEDULE_ALARM_CLOSE_STATE
                    this[i][scheduleId].st = "00:00:00"
                    this[i][scheduleId].et = "24:00:00"
                }
            }
            saveAlarmDetect()
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
        private val TAG: String = AddScheduleAlarmSettingCameraJFFragment::class.java.simpleName

        private const val GET_ALARM_TIME = "get_alarm_time"
        private const val SET_ALARM_TIME = "set_alarm_time"
    }
}
