package com.viettel.vht.sdk.model

import com.google.gson.annotations.SerializedName

data class FireAlarmResponse(
    @SerializedName("address")
    val address: String?,
    @SerializedName("main_person")
    val mainPerson: PersonFireAlarmResponse?,
    @SerializedName("sub_person_1")
    val subPerson1: PersonFireAlarmResponse?,
    @SerializedName("sub_person_2")
    val subPerson2: PersonFireAlarmResponse?
)