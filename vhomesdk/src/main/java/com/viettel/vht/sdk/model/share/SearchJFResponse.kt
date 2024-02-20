package com.viettel.vht.main.model.share

import com.google.gson.annotations.SerializedName

data class SearchJFResponse(
    @SerializedName("id") var id: String,
    @SerializedName("account") var account: String
)