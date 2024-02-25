package com.viettel.vht.sdk.model

import java.io.Serializable

data class ThingResponse(
    val itemType: Int = 0,
    var itemData: DataDeviceResponse = DataDeviceResponse(),
    val index: Int = 0
) : Serializable