package com.viettel.vht.sdk.ui.listvideocloud

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.Config
import com.lib.sdk.struct.H264_DVR_FILE_DATA
import com.manager.device.media.playback.RecordManager
import com.vht.sdkcore.base.BaseViewModel
import com.vht.sdkcore.file.AppLogFileManager
import com.vht.sdkcore.utils.AppLog
import com.vht.sdkcore.utils.Constants
import com.vht.sdkcore.utils.REPAppLogModel
import com.viettel.vht.sdk.jfmanager.JFCameraManager
import com.viettel.vht.sdk.model.camera_cloud.CloudStatus
import com.viettel.vht.sdk.model.camera_cloud.CloudStorageRegistered
import com.viettel.vht.sdk.model.camera_cloud.Descriptions
import com.viettel.vht.sdk.network.repository.CloudRepository
import com.viettel.vht.sdk.utils.Config

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class ListVideoCloudViewModel @Inject constructor(
    private val cloudRepository: CloudRepository,
    private val appLogFileManager: AppLogFileManager
) : BaseViewModel() {

    val date = MutableLiveData(System.currentTimeMillis())
    val listPlaybackLiveData = MutableLiveData(mutableListOf<GroupVideo>())
    val listEventLiveData = MutableLiveData(mutableListOf<GroupVideo>())
    private var currentCloudPackage: CloudStorageRegistered? = null
    private var deviceId: String? = null


    // Camera JF

    fun searchVideo(
        recordManager: RecordManager,
        onSearchResult: (MutableList<H264_DVR_FILE_DATA>) -> Unit
    ) {
        viewModelScope.launch {
            val result =
                withContext(Dispatchers.IO) {
                    JFCameraManager.searchRecordCloudAllDay(
                        recordManager.devId,
                        Date(date.value ?: 0)
                    )
                }
            onSearchResult.invoke(result)
        }
    }

    suspend fun checkCloudStorageRegistered(serial: String) =
        cloudRepository.checkRegistedCloud(serial)

    var cloudRegistered = MutableLiveData<List<CloudStorageRegistered>?>()
    val listDateCloudDayAlso = MutableLiveData<List<Date>>()
    fun getListCloudStorageRegistered(serial: String) {
        viewModelScope.launch {
            deviceId = serial
            cloudRepository.getListCloudStorageRegistered(serial).collect {
                when (it) {
                    is com.viettel.vht.sdk.network.Result.Success -> {
                        cloudRegistered.postValue(it.data?.data)
                        listDateCloudDayAlso.value =
                            cloudRepository.listDayCloudAlsoSored(it.data?.data)
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
                        sendLogForCloudPackage(
                            screenID = AppLog.ScreenID.CLOUD_LIST,
                            actionID = AppLog.LogPlayBackCloud.TRACKING_SELECT_DATE_CLOUD.actionID
                        )
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
        val diffInMs: Long = System.currentTimeMillis() - (date.value!!)
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