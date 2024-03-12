package com.viettel.vht.sdk.ui.quickhistory

import android.annotation.SuppressLint
import android.app.Application
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lib.FunSDK
import com.lib.sdk.struct.H264_DVR_FILE_DATA
import com.manager.device.media.playback.RecordManager
import com.utils.TimeUtils
import com.vht.sdkcore.base.BaseViewModel
import com.vht.sdkcore.file.AppLogFileManager
import com.vht.sdkcore.pref.RxPreferences
import com.vht.sdkcore.utils.AppLog
import com.vht.sdkcore.utils.Constants
import com.vht.sdkcore.utils.REPAppLogModel
import com.viettel.vht.sdk.jfmanager.JFCameraManager
import com.viettel.vht.sdk.model.camera_cloud.CloudStatus
import com.viettel.vht.sdk.model.camera_cloud.CloudStorageRegistered
import com.viettel.vht.sdk.model.camera_cloud.Descriptions
import com.viettel.vht.sdk.network.repository.CloudRepository
import com.viettel.vht.sdk.utils.Config
import com.viettel.vht.sdk.utils.DebugConfig
import com.viettel.vht.sdk.utils.ImageUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.Date
import java.util.concurrent.TimeUnit
import javax.inject.Inject


@HiltViewModel
class HistoryDetailLiveViewModel @Inject constructor(
    private val application: Application,
    private val appLogFileManager: AppLogFileManager,
    private val cloudRepository: CloudRepository
) : BaseViewModel() {
    private var packageInfo: PackageInfo? = null

    private var currentCloudPackage: CloudStorageRegistered? = null
    private var deviceId: String? = null
    private var isTheFirstTime: Boolean = true

    init {

        packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            application.applicationContext.packageManager.getPackageInfo(
                application.applicationContext.packageName,
                PackageManager.PackageInfoFlags.of(0L)
            )
        } else {
            application.applicationContext.packageManager.getPackageInfo(
                application.applicationContext.packageName,
                0
            )
        }
    }

    val dateLiveData = MutableLiveData(Date().time)
    val listDateCloudDayAlso = mutableListOf<Date>()



    suspend fun previousDay() {
        withContext(Dispatchers.Main) {
            val currentDate = dateLiveData.value
            dateLiveData.value = currentDate?.minus(86_400_000)
        }
    }

    suspend fun nextDay() {
        withContext(Dispatchers.Main) {
            val currentDate = dateLiveData.value
            dateLiveData.value = currentDate?.plus(86_400_000)
        }
    }




    // Camera JF

    var isDisableVolume = MutableLiveData(false)


    fun searchVideo(recordManager: RecordManager) {
        JFCameraManager.searchRecordByFile(Date(dateLiveData.value ?: 0), recordManager)
        JFCameraManager.searchRecordByTime(Date(dateLiveData.value ?: 0), recordManager)
    }

    fun searchVideoCloud(
        recordManager: RecordManager,
        onSearchResult: (MutableList<H264_DVR_FILE_DATA>) -> Unit
    ) {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                JFCameraManager.searchRecordCloudAllDay(
                    recordManager.devId,
                    Date(dateLiveData.value ?: 0)
                )
            }
            onSearchResult.invoke(result)
        }
    }

    fun playVideo(recordManager: RecordManager, item: H264_DVR_FILE_DATA) {
        val playCalendar = TimeUtils.getNormalFormatCalender(item.startTimeOfYear)
        DebugConfig.log("PlayVideo", "playCalendar = ${playCalendar.time}")
        val endCalendar: Calendar
        endCalendar = Calendar.getInstance()
        endCalendar.time = playCalendar.time
        endCalendar[Calendar.HOUR_OF_DAY] = 23
        endCalendar[Calendar.MINUTE] = 59
        endCalendar[Calendar.SECOND] = 59
        DebugConfig.log("PlayVideo", "endCalendar = ${endCalendar.time}")
        recordManager.startPlay(playCalendar, endCalendar)
    }

    fun convertToTimeSecond(callSecond: Long?): String? {
        var callSecond = callSecond
        if (callSecond == null) {
            return "00:00:00"
        }
        val oneHourSecond = TimeUnit.HOURS.toSeconds(1)
        val oneMinuteSecond = TimeUnit.MINUTES.toSeconds(1)
        val hour = (callSecond / oneHourSecond).toInt()
        callSecond = callSecond % oneHourSecond
        val minute = (callSecond / oneMinuteSecond).toInt()
        val seconds = (callSecond % 60).toInt()
        return String.format("%02d:%02d:%02d", hour, minute, seconds)
    }

    fun seekToTime(recordManager: RecordManager, times: Int) {
        val searchTime = Calendar.getInstance()
        searchTime.time = Date(dateLiveData.value ?: 0)
        searchTime.set(Calendar.HOUR_OF_DAY, 0)
        searchTime.set(Calendar.MINUTE, 0)
        searchTime.set(Calendar.SECOND, 0)
        DebugConfig.log("seekToTime", "searchTime = ${searchTime.time}")
        val time = intArrayOf(
            searchTime.get(Calendar.YEAR), searchTime.get(Calendar.MONTH) + 1,
            searchTime.get(Calendar.DAY_OF_MONTH), 0, 0, 0
        )
        val absTime: Int = FunSDK.ToTimeType(time) + times
        Log.d("TAG", "DEBUG_TIME: absTime = ${absTime}")
        Log.d("TAG", "DEBUG_TIME: times = ${times}")
        Log.d("TAG", "DEBUG_TIME: convertToTimeSecond = ${convertToTimeSecond(times.toLong())}")
        Log.d("TAG", "DEBUG_TIME: seekTime = ${Date(absTime * 1000L)}")
        recordManager.seekToTime(times, absTime)
    }


    fun capture(dir: String, recordManager: RecordManager, callBack: (String) -> Unit) {
        val path = recordManager.capture(dir)
        callBack.invoke(path)
    }

    fun saveVideoToGallery(path: String, devId: String) {
        io.launch {
            ImageUtils.saveVideoToGalleryJFCamera(application.applicationContext, path, devId)
        }
    }

    var cloudRegistered = MutableLiveData<List<CloudStorageRegistered>?>()

    @SuppressLint("NewApi")
    fun getListCloudStorageRegistered(
        serial: String,
        screenID: String,
        actionID: String,
    ) {
        viewModelScope.launch {
            cloudRepository.getListCloudStorageRegistered(serial).collect {
                when (it) {
                    is com.viettel.vht.sdk.network.Result.Success -> {
                        cloudRegistered.postValue(it.data?.data)
                        deviceId = serial
                        currentCloudPackage = it.data?.data?.find {
                            it.serviceStatus == CloudStatus.IN_USE.cloudStatus
                        }

                        if (currentCloudPackage == null) {
                            currentCloudPackage = CloudStorageRegistered(
                                "",
                                "",
                                "",
                                "",
                                0L,
                                0L,
                                0L,
                                "",
                                0L,
                                "",
                                "",
                                "",
                                "",
                                "",
                                "",
                                Descriptions("", "", "", "", 0, 0, true),
                                0,
                                userId = "",
                                userUse = ""
                            )
                        }
                        if (isTheFirstTime) {
                            sendLogForCloudPackage(
                                screenID = AppLog.LogPlayBackCloud.TRACKING_SELECT_DATE_CLOUD.screenID,
                                actionID = AppLog.LogPlayBackCloud.TRACKING_SELECT_DATE_CLOUD.actionID
                            )
                            isTheFirstTime = false
                        }

                        // lấy danh sách ngày lưu cloud.
                        listDateCloudDayAlso.addAll(cloudRepository.listDayCloudAlsoSored(it.data?.data))
                    }

                    is com.viettel.vht.sdk.network.Result.Error -> {
                        cloudRegistered.postValue(mutableListOf())
                    }

                    else -> {}
                }
            }
        }
    }

    fun sendLogForCloudPackage(screenID: String, actionID: String) {
        val diffInMs: Long = System.currentTimeMillis() - (dateLiveData.value!!)
        val diffInSec = TimeUnit.MILLISECONDS.toDays(diffInMs)
        sendCloudLog(
            screenID = screenID,
            actionID = actionID,
            paymentName = currentCloudPackage?.packageType ?: Constants.EMPTY,
            paymentNumber = currentCloudPackage?.getNumberFromCloudVi() ?: "0",
            deviceID = deviceId ?: "",
            dateStart = (currentCloudPackage?.startDateTime ?: 0).toString(),
            dateEnd = if (currentCloudPackage?.isPostpaid() == true) "0"
            else (currentCloudPackage?.endDateTime ?: 0).toString(),
            dateSelect = diffInSec.toString(),
        )
    }


    private fun sendCloudLog(
        screenID: String,
        actionID: String,
        deviceID: String,
        paymentName: String,
        dateStart: String,
        dateEnd: String,
        paymentNumber: String,
        dateSelect: String,
        report: String = "REPORT"
    ) {
        appLogFileManager.saveREPAppLogFile(
            REPAppLogModel(
                logMode = Constants.LOG_APP_REP,
                timeStamp = System.currentTimeMillis().toString(),
                screenId = screenID,
                actionId = actionID,
                deviceId = deviceID,
                paymentName = paymentName,
                dateStart = dateStart,
                dateEnd = dateEnd,
                paymentNumber = paymentNumber,
                dateSelect = dateSelect,
                report = report,
                serverDomainIP = Config.sdkBASE_URL
            )
        )
    }

}