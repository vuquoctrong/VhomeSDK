package com.viettel.vht.sdk.ui.jfcameradetail.schedule_alarm

import android.os.Message
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.lib.AS.AlarmService
import com.lib.EUIMSG
import com.lib.FunSDK
import com.lib.IFunSDKResult
import com.lib.MsgContent
import com.lib.SDKCONST
import com.manager.device.DeviceManager
import com.manager.device.config.DevConfigManager
import com.vht.sdkcore.base.BaseViewModel
import com.vht.sdkcore.utils.SingleLiveEvent
import com.viettel.vht.sdk.model.jfcall.NoDisturbingAlarmTimeInfoBean
import com.viettel.vht.sdk.ui.jfcameradetail.schedule_alarm.model.TimeItem
import com.viettel.vht.sdk.ui.jfcameradetail.schedule_alarm.model.TimeItem.Companion.SCHEDULE_ALARM_CLOSE_STATE
import com.viettel.vht.sdk.ui.jfcameradetail.schedule_alarm.model.TimeItem.Companion.SCHEDULE_ALARM_OPEN_STATE
import com.viettel.vht.sdk.ui.jfcameradetail.schedule_alarm.model.TimeItem.Companion.TOTAL_SCHEDULE_ALARM
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ListScheduleAlarmSettingCameraJFViewModel @Inject constructor() : BaseViewModel(),
    IFunSDKResult {

    lateinit var devId: String
    private val deviceManager: DeviceManager
    private lateinit var devConfigManager: DevConfigManager

    private var userId = 0

    private var noDisturbingAlarmTimeInfoBean: NoDisturbingAlarmTimeInfoBean? = null
    private var noDisturbingAlarmTimeInfoBeanTemp: NoDisturbingAlarmTimeInfoBean? = null
    private val alarmScheduleTimeList = mutableListOf<TimeItem>()
    val scheduleList = SingleLiveEvent<List<TimeItem>>()

    val deleteState = SingleLiveEvent<Boolean>().apply { value = false }
    val selectAllDeleteState = SingleLiveEvent<Boolean>().apply { value = false }
    val selectDeleteStateCount = SingleLiveEvent<Int>().apply { value = 0 }

    init {
        userId = FunSDK.GetId(userId, this)
        deviceManager = DeviceManager.getInstance()
    }

    fun getConfig() {
        if (!this::devId.isInitialized) return
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
                            noDisturbingAlarmTimeInfoBeanTemp =
                                noDisturbingAlarmTimeInfoBean?.clone()
                            getListScheduleAlarmSetting()
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
                        Timber.tag(TAG).d("set no disturbing alarm config success")
                    } else {
                        noDisturbingAlarmTimeInfoBeanTemp = noDisturbingAlarmTimeInfoBean?.clone()
                        getListScheduleAlarmSetting()
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

    private fun saveAlarmDetect() {
        if (this::devId.isInitialized && noDisturbingAlarmTimeInfoBeanTemp != null) {
            isLoading.value = true
            val infoBean = NoDisturbingAlarmTimeInfoBean()
            infoBean.msg = SET_ALARM_TIME
            infoBean.sn = devId
            infoBean.type = noDisturbingAlarmTimeInfoBeanTemp?.type ?: 0
            infoBean.ts = noDisturbingAlarmTimeInfoBeanTemp?.ts
            AlarmService.OptAlarmMsgDoNotDisturb(userId, Gson().toJson(infoBean), 2)
        }
    }

    private fun getListScheduleAlarmSetting() {
        viewModelScope.launch {
            if (noDisturbingAlarmTimeInfoBean != null) {
                alarmScheduleTimeList.clear()
                for (i in 0 until TOTAL_SCHEDULE_ALARM) {
                    val info = TimeItem(position = i, stateDelete = false)
                    for (j in 0 until SDKCONST.NET_N_WEEKS) {
                        val schedule = noDisturbingAlarmTimeInfoBean?.ts?.get(j)?.get(i) ?: continue
                        if (schedule.et != "24:00:00") {
                            info.dayOfWeek[j] = true
                            if (schedule.en == SCHEDULE_ALARM_OPEN_STATE) {
                                info.isOpen = true
                            }
                            val eachTimeStart = mutableListOf<String>()
                            eachTimeStart.addAll(schedule.st.split("\\W+".toRegex()))
                            val eachTimeEnd = mutableListOf<String>()
                            eachTimeEnd.addAll(schedule.et.split("\\W+".toRegex()))
                            info.time = (eachTimeStart.getOrNull(0) ?: "00") +
                                    ":" +
                                    (eachTimeStart.getOrNull(1) ?: "00") +
                                    "-" +
                                    (eachTimeEnd.getOrNull(0) ?: "23") +
                                    ":" +
                                    (eachTimeEnd.getOrNull(1) ?: "59")
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
            } else {
                scheduleList.value = emptyList()
            }
        }
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
        noDisturbingAlarmTimeInfoBeanTemp?.ts?.apply {
            val eachTime = mutableListOf<String>()
            eachTime.addAll(item.time.split("\\W+".toRegex()))
            for (i in 0 until SDKCONST.NET_N_WEEKS) {
                if (item.dayOfWeek[i]) {
                    this[i][item.position].en =
                        if (state) SCHEDULE_ALARM_OPEN_STATE else SCHEDULE_ALARM_CLOSE_STATE
                    eachTime.takeIf { eachTime.size == 4 }?.let {
                        this[i][item.position].st = String.format(
                            TimeItem.SCHEDULE_ALARM_TIME_FORMAT,
                            eachTime[0], eachTime[1], "00"
                        )
                        this[i][item.position].et = String.format(
                            TimeItem.SCHEDULE_ALARM_TIME_FORMAT,
                            eachTime[2], eachTime[3], "59"
                        )
                    }
                } else {
                    this[i][item.position].en = SCHEDULE_ALARM_CLOSE_STATE
                    this[i][item.position].st = "00:00:00"
                    this[i][item.position].et = "24:00:00"
                }
            }
            saveAlarmDetect()
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
            timeItem.position = index
        }
        for (i in 0 until TOTAL_SCHEDULE_ALARM) {
            for (j in 0 until SDKCONST.NET_N_WEEKS) {
                val schedule = noDisturbingAlarmTimeInfoBeanTemp?.ts?.get(j)?.get(i) ?: continue
                schedule.en = SCHEDULE_ALARM_CLOSE_STATE
                schedule.st = "00:00:00"
                schedule.et = "24:00:00"
            }
        }
        noDisturbingAlarmTimeInfoBeanTemp?.ts?.apply {
            alarmScheduleTimeListTemp.forEach { item ->
                val eachTime = mutableListOf<String>()
                eachTime.addAll(item.time.split("\\W+".toRegex()))
                for (i in 0 until SDKCONST.NET_N_WEEKS) {
                    if (item.dayOfWeek[i]) {
                        this[i][item.position].en =
                            if (item.isOpen) SCHEDULE_ALARM_OPEN_STATE else SCHEDULE_ALARM_CLOSE_STATE
                        eachTime.takeIf { eachTime.size == 4 }?.let {
                            this[i][item.position].st = String.format(
                                TimeItem.SCHEDULE_ALARM_TIME_FORMAT,
                                eachTime[0], eachTime[1], "00"
                            )
                            this[i][item.position].et = String.format(
                                TimeItem.SCHEDULE_ALARM_TIME_FORMAT,
                                eachTime[2], eachTime[3], "59"
                            )
                        }
                    } else {
                        this[i][item.position].en = SCHEDULE_ALARM_CLOSE_STATE
                        this[i][item.position].st = "00:00:00"
                        this[i][item.position].et = "24:00:00"
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
        saveAlarmDetect()
    }

    companion object {
        private val TAG: String = ListScheduleAlarmSettingCameraJFViewModel::class.java.simpleName

        private const val GET_ALARM_TIME = "get_alarm_time"
        private const val SET_ALARM_TIME = "set_alarm_time"
    }
}
