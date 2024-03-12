package com.viettel.vht.sdk.ui.quickhistory

import com.lib.sdk.struct.H264_DVR_FILE_DATA

data class PlaybackFile(var startTime:Long, val endTime:Long,val item: H264_DVR_FILE_DATA)
