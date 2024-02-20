package com.viettel.vht.sdk.model.home

import com.google.gson.annotations.SerializedName
import com.viettel.vht.sdk.utils.AttributeDataManager

data class AttributeRequest(
    @SerializedName("attribute_type")
    val attributeType: String = AttributeDataManager.ScopeType.SCOPE_SERVER,
    @SerializedName("attribute_key")
    val attributeKey: String = "",
    @SerializedName("new_attribute_key")
    val newAttributeKey: String? = null,
    @SerializedName("logged")
    val logged: Boolean = false,
    @SerializedName("value")
    val value: String = "",
    @SerializedName("value_t")
    val valueType: String = ""
)