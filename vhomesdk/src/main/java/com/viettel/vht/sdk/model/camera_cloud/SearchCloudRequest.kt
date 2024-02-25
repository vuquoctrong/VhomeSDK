package com.viettel.vht.sdk.model.camera_cloud

data class SearchCloudRequest(
    val startTime: Long = 1L,
    val endTime: Long = System.currentTimeMillis(),
    val serial: String = "",
    var limit: Int = 10,
    var offset: Int = 0,
    val order: Int = 1,
    val skipExpired : Boolean = true,
)