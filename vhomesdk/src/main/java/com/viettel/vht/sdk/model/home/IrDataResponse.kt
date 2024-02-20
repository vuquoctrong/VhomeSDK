package com.viettel.vht.sdk.model.home

import com.google.gson.annotations.SerializedName
import java.io.Serializable

class IrDataResponse(
    var name: String? = null,
    var btnName: String = "",
    @SerializedName("IRCode")
    var iRCode: String = "",
    val btnType: String? = null,
    val btnColor: String? = null,
    var backUp: String? = null,
) : Serializable