package com.viettel.vht.sdk.utils.custom

data class EventHistory(
    val id: String,
    val eventID: String,
    val videoID: String = "",
    var eventType: Int,
    var timeStart: Long,
    var timeEnd: Long,
    val deviceID: String,
    var thumbnailUrl: String?,
    var videoPath: String?="",
    var thumbnailByteArray: ByteArray? = null,
    var type: Int = 6,
    var preTime:Long?=0,
    var isCheckedThumb: Boolean = false,
    var isNewest:Boolean = false
)