package com.viettel.vht.sdk.model.home

import com.google.gson.annotations.SerializedName

data class FeatureConfigResponse(
    @SerializedName("spreadCamera")
    val spreadCamera: SpreadCamera? = null,
    @SerializedName("invitationCode")
    val invitationCode: String = "",//disable or enable
)
data class SpreadCamera(
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("status")
    val status: String? = null,
    @SerializedName("title")
    val title: String? = null,
)