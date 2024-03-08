package com.viettel.vht.sdk.model.camera_cloud

import com.google.gson.annotations.SerializedName

data class PaymentRequest(
    @SerializedName("camera_serial")
    val cameraSerial: String = "",
    @SerializedName("package_id")
    val packageId: String = "",
    val price: String = ""
)
data class BuyCloudVeRequest(
    @SerializedName("device_id")
    val deviceId: String = "",
    @SerializedName("package_code")
    val packageCode: String = "",
    @SerializedName("method")
    val method: String = "",//  VTPAY-WALLET : là thanh toán qua cổng lên kết or CTT: thanh toán qua cổng.
    @SerializedName("invitation_code")
    val invitationCode: String = "",// Mã giới thiệu.
    @SerializedName("to_user")
    val userIdRelative: String? = ""
)

data class PaymentPayLinkRequest(
    @SerializedName("order_id")
    val orderId: String = "",
    @SerializedName("otp")
    val otp: String = "",
)

data class OnOffAutoChargingCloud(
    @SerializedName("serial")
    val serial: String = "",
    @SerializedName("status")
    val status: String = "",
    @SerializedName("method")
    val method: String = "",
    @SerializedName("otp")
    val otp: String = "",// Nếu OTP chuyền otp = system-run thì k cần lấy gọi API Lấy OTP
    @SerializedName("order_id")
    val orderId: String = "",
)

data class OTPOnOffAutoChargingCloudRequest(
    @SerializedName("type_otp")
    val typeOtp: String = "on-off-payment",
    @SerializedName("serial")
    val serial: String = "",

)

data class ChangeLevelPayLink(
    @SerializedName("daily_amount_limit")
    val daily_amount_limit: Int,
    @SerializedName("trans_amount_limit")
    val trans_amount_limit: Int,
)

data class ResponseSpreadCheck(
    val code: Int? = 0,
    val data: List<DataSpreadCheck>? = listOf(),
    val message: String? = "",
    var mesSpreadCamera: String? = "",
    var titleSpreadCamera: String? = "",
)

data class DataSpreadCheck(
    val score: Int? = 0,
    val phone: String? = "",
)