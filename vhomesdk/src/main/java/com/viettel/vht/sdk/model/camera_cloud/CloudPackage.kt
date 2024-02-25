package com.viettel.vht.sdk.model.camera_cloud

import com.viettel.vht.sdk.model.camera_cloud.Description

data class CloudPackage(
    val description: Description = Description(),
    val id: String = "",
    val serviceExpireDisplay: String = "",
    val serviceName: String = "",
    val servicePeriodDisplay: String = "",
    val servicePrice: Int = 0,
    val serviceType: Int = 0,
    val serviceMethod: Int = 0,
    var registered: Boolean = false
)