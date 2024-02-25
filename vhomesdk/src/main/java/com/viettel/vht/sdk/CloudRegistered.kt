package com.viettel.vht.sdk

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class CloudRegistered(
    val active: Boolean = false,
    val description: Description = Description(),
    val deviceId: String = "",
    val isActive: Boolean = false,
    var state: StateCloud = StateCloud.EXPIRED,
    var registrationDate: Long = 0L,
    val expireDate: Long = 0L,
    val serviceId: String = "",
    val serviceName: String = "",
    val servicePeriod: Int = 0,
    val servicePrice: Int = 0,
    val serviceType: String = "",
    val serviceMethod: Int = 0
) : Parcelable

enum class StateCloud {
    USED, NOT_ACTIVED, EXPIRED
}

@Parcelize
data class Description(
    val en: String = "",
    val enRaw: String = "",
    val vi: String = "",
    val viRaw: String = ""
): Parcelable



data class DataPricingCloud(
    @SerializedName("promotions")// khuyến mãi
    val promotions: DataPromotions? = null,
    @SerializedName("come_expired")// sắp hết hạn 3 ngày
    val comeExpired :List<DataFreePricing>? = listOf(),

    @SerializedName("just_expired")// săp hết hạn 1 ngày
    val justExpired :List<DataFreePricing>? = listOf(),
)

data class DataPromotions(

    @SerializedName("cloud_period")
    val cloudPeriod: Int? = 0,
    @SerializedName("cloud_time")
    val cloudTime: Int? = 0,
    @SerializedName("price")
    val price: String? = null,
    @SerializedName("free")// khuyến mãi
    val free: List<DataFreePricing>? = listOf(),
    @SerializedName("expiry_time")
    val ExpiryTime: String? = null
)

class DataFreePricing (
    @SerializedName("name")
    var name: String = "",

    @SerializedName("id")
    var id: String= "",

    @SerializedName("serial")
    var serial: String= "",

    @SerializedName("type")
    var type: String= "",

    @SerializedName("cameraModel")
    var cameraModel: String= "",

    var isSelected: Boolean = false

)

@Parcelize
data class RequestRegisterPromotionFree(val list: List<Item>, var otp: String = "",var order_id: String = "",var invitation_code: String = ""):Parcelable {
    @Parcelize
    data class Item(val name: String, val id: String, val serial: String, val type: String):Parcelable
}
