package com.viettel.vht.sdk.model.home

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.RawValue
import kotlinx.parcelize.Parcelize

@Parcelize
data class AttributeResponse(
    @SerializedName("attribute_type") val attributeType: String = "",
    @SerializedName("attribute_key") val attributeKey: String = "",
    @SerializedName("logged") val logged: Boolean = false,
    @SerializedName("value") var value: @RawValue Any? = null,
    @SerializedName("value_as_string") var valueAsString: String = "",
    @SerializedName("last_update_ts") val lastUpdateTs: Long = 0,
    @SerializedName("value_type") val valueType: String = "",
) : Parcelable