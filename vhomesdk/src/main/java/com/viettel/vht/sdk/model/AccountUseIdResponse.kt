package com.viettel.vht.sdk.model

import com.google.gson.annotations.SerializedName

data class AccountUseIdResponse(
    @SerializedName("email")
    var email: String? = null,
    @SerializedName("phone")
    val phone: String? = null,
    @SerializedName("username")
    val username: String? = null,
    @SerializedName("name")
    val name: String? = null,
)