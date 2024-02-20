package com.viettel.vht.sdk.model

import com.google.gson.annotations.SerializedName

data class PersonFireAlarmResponse(
    @SerializedName("name")
    var name: String?,
    @SerializedName("phone")
    var phone: String?,
    @SerializedName("pronoun")
    val pronoun: String?
)