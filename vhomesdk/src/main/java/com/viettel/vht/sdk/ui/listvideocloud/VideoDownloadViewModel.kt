package com.viettel.vht.sdk.ui.listvideocloud

import android.annotation.SuppressLint
import android.content.Context
import android.os.Message
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.lib.EUIMSG
import com.lib.FunSDK
import com.lib.IFunSDKResult
import com.lib.MsgContent
import com.vht.sdkcore.base.BaseViewModel
import com.vht.sdkcore.utils.Utils
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import java.util.LinkedList
import javax.inject.Inject


@SuppressLint("StaticFieldLeak")
@HiltViewModel
class VideoDownloadViewModel @Inject constructor(
    @ApplicationContext val context: Context
) : BaseViewModel(), IFunSDKResult {

    var devId: String = ""

    private var userId = 0

    private var mDownloadVal = -1

    private val listVideoDownload = LinkedList<ItemVideo>()

    private var filePath: String? = null

    private var isSaveToGallery = false

    val isDownloading = MutableLiveData(false)
    val downloadProgress = MutableLiveData(0)

    init {
        userId = FunSDK.GetId(userId, this)
    }

    override fun OnFunSDKResult(message: Message, msgContent: MsgContent): Int {
        Timber.tag(TAG).d("OnFunSDKResult")
        Timber.tag(TAG).d("message: ${message.what} - $message")
        Timber.tag(TAG).d("msgContent: ${Gson().toJson(msgContent)}")
        if (message.arg1 < 0) {
            downloadFailed()
            return 0
        }
        when (message.what) {
            EUIMSG.ON_FILE_DOWNLOAD -> downloadStart()
            EUIMSG.ON_FILE_DLD_POS -> Unit
            EUIMSG.ON_FILE_DLD_COMPLETE -> {
                if (message.arg1 == 0) {
                    downloadFailed()
                } else {
                    downloadComplete()
                }
            }

            EUIMSG.EMSG_Stop_DownLoad -> Unit
            else -> Unit
        }
        return 0
    }

    private fun downloadStart() {
        isDownloading.postValue(true)
        downloadProgress.postValue(listVideoDownload.size + 1)
    }

    private fun downloadComplete() {
        if (isSaveToGallery) {
            filePath?.let { Utils.saveVideoToGallery(context, it) }
        }
        download()
    }

    private fun downloadFailed() {
        download()
    }

    private fun download() {
        val itemVideo = listVideoDownload.poll()
        if (itemVideo == null) {
            isDownloading.postValue(false)
            downloadProgress.postValue(0)
            filePath = null
            return
        }
        val data = itemVideo.video ?: return
        val startTime = FunSDK.ToTimeType(
            intArrayOf(
                data.st_3_beginTime.st_0_year,
                data.st_3_beginTime.st_1_month,
                data.st_3_beginTime.st_2_day,
                data.st_3_beginTime.st_4_hour,
                data.st_3_beginTime.st_5_minute,
                data.st_3_beginTime.st_6_second
            )
        )
        val endTime = FunSDK.ToTimeType(
            intArrayOf(
                data.st_4_endTime.st_0_year,
                data.st_4_endTime.st_1_month,
                data.st_4_endTime.st_2_day,
                data.st_4_endTime.st_4_hour,
                data.st_4_endTime.st_5_minute,
                data.st_4_endTime.st_6_second
            )
        )
        filePath = Utils.getRecordVideoPath(context, devId)
        mDownloadVal = FunSDK.MediaCloudRecordDownload(
            userId, devId,
            data.st_0_ch,
            if (data.st_6_StreamType == 0) "Main" else "Sub",
            startTime, endTime, filePath, 0
        )
    }

    fun startDownloadVideo(
        listVideo: List<ItemVideo>,
        devId: String,
        isSaveToGallery: Boolean = false
    ) {
        this.devId = devId
        listVideoDownload.addAll(listVideo)
        this.isSaveToGallery = isSaveToGallery
        download()
    }

    override fun onCleared() {
        super.onCleared()
        if (mDownloadVal > 0) {
            FunSDK.DevStopDownLoad(mDownloadVal)
        }
        mDownloadVal = -1
        FunSDK.UnRegUser(userId)
    }

    companion object {
        private val TAG: String = VideoDownloadViewModel::class.java.simpleName
    }
}
