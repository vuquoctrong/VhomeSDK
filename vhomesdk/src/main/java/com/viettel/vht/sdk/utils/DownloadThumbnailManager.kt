package com.viettel.vht.sdk.utils

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Message
import android.util.Log
import com.lib.*
import com.lib.cloud.CloudDirectory
import com.utils.FileUtils
import com.vht.sdkcore.utils.Utils
import java.io.File
import java.util.*

const val DOWNLOAD_MSG = 1

class DownloadThumbnailManager(
    private val context: Context?, private val devId: String,
    private var mListener: OnCloudImageListener?
) :
    IFunSDKResult {
    private var mUserId = 0
    private var mThumbDownloadId = 0
    private var handler: Handler? = null

    init {
        mUserId = FunSDK.GetId(mUserId, this)
        handler = @SuppressLint("HandlerLeak")
        object : Handler() {
            override fun handleMessage(msg: Message) {
                if (msg.what == DOWNLOAD_MSG) {
                    val obj = msg.obj as Triple<Int, Int, String>
                    Log.d("TAG", "downloadThumbnail: path = ${obj.third}")
                    mThumbDownloadId = CloudDirectory.DownloadThumbnailByTime(
                        mUserId,
                        devId,
                        obj.third,
                        "Main",
                        "MSG_ALARM_VIDEO_QUERY_REQ",
                        0,
                        obj.first,
                        obj.second,
                        0,
                        0,
                        SDKCONST.Switch.Close,
                        msg.arg1
                    )
                }
            }
        }
    }

    fun downloadThumbnail(timeStart: Long, timeEnd: Long, pos: Int): String? {
        var path = ""
        context?.let { context ->
            path = (Utils.folderThumbnailInternalStoragePath(context)
                    + File.separator
                    + timeStart + "_" + timeEnd + "_thumb.jpg")
        }
        if (FileUtils.isFileExist(path) && File(path).length() > 0) {
            Log.d("TAG_1", "File exist: path = $path")
            return path
        } else {
            Log.d("TAG_1", "download thumb: ${Date(timeStart)}")
            val s = Calendar.getInstance()
            s.time = Date(timeStart)
            val sTime = FunSDK.ToTimeType(
                intArrayOf(
                    s.get(Calendar.YEAR),
                    s.get(Calendar.MONTH) + 1,
                    s.get(Calendar.DAY_OF_MONTH),
                    s.get(Calendar.HOUR_OF_DAY),
                    s.get(Calendar.MINUTE),
                    s.get(Calendar.SECOND)
                )
            )
            val e = Calendar.getInstance()
            e.time = Date(timeEnd)
            val eTime = FunSDK.ToTimeType(
                intArrayOf(
                    e.get(Calendar.YEAR),
                    e.get(Calendar.MONTH) + 1,
                    e.get(Calendar.DAY_OF_MONTH),
                    e.get(Calendar.HOUR_OF_DAY),
                    e.get(Calendar.MINUTE),
                    e.get(Calendar.SECOND)
                )
            )
            val message = Message.obtain()
            message.what = DOWNLOAD_MSG
            message.arg1 = pos
            message.obj = Triple(sTime, eTime, path)
            Log.d("TAG", "downloadThumbnail: time = ${pos*1L}")
            handler?.sendMessageDelayed(message, pos * 1L)
            return path
        }
    }

    override fun OnFunSDKResult(msg: Message?, ex: MsgContent?): Int {
        when (msg!!.what) {
            EUIMSG.MC_DownloadMediaThumbnail, EUIMSG.DOWN_RECODE_BPIC_FILE -> if (mThumbDownloadId != 0) {
                mListener?.onDownloadResult(msg.arg1 >= 0, ex?.seq, ex?.str)
                Log.d("TAG", "OnFunSDKResult: ${ex?.str}")
            }
            else -> {}
        }
        return 0
    }

    interface OnCloudImageListener {
        fun onDownloadResult(
            isSuccess: Boolean,
            seq: Int? = -1,
            path: String? = ""
        )
    }
}