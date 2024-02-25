package com.viettel.vht.sdk.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class FamilyHomeResponse(
    @SerializedName("familyid")
    val familyId: String = "",
    @SerializedName("index")
    val index: Int = 0,
    @SerializedName("roomid")
    val roomId: String = "",
    val roomName: String? = null,
    val isActiveOnRoom: Boolean? = false
) : Serializable