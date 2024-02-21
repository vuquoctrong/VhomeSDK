package com.viettel.vht.sdk.model.home

import com.google.gson.annotations.SerializedName

data class DataAddRequest(
    val name: String = "",
    val key: String = "",
    @SerializedName("org_id")
    val orgId: String = "",
    val attributes: MutableList<AttributeRequest>? = arrayListOf(),
    val ir: IR? = null,
    val type: String = "",
)

data class IR(
    @SerializedName("ir_type")
    val type: String = "",
    val vendor: String = "",
    val cmds: List<CMD> = listOf()
)

data class CMD(
    val protocol: Int = -1,
    @SerializedName("raw_data")
    val rawData: List<Int>?,
    val size: Int?,
    val data: Long?,
    val nbits: Int?
)