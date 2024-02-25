package com.viettel.vht.sdk.model.camera_cloud

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize


data class CloudStorageRegisteredResponse(
    val total: Int?,
    val limit: Int?,
    val offset: Int?,
    val data: List<CloudStorageRegistered>?,
)

@Parcelize
data class CloudStorageRegistered(
    @SerializedName("order_id") val orderId: String?,
    @SerializedName("transaction_id") val transactionId: String?,
    @SerializedName("account_id") val accountId: String?,
    @SerializedName("phone_number") val phoneNumber: String?,
    @SerializedName("purchase_date_time") val purchaseDateTime: Long?,
    @SerializedName("start_date_time") val startDateTime: Long?,
    @SerializedName("end_date_time") val endDateTime: Long?,
    @SerializedName("payment_method") val paymentMethod: String?,
    @SerializedName("price") val price: Long?,
    @SerializedName("package_type") val packageType: String?,
    @SerializedName("package_id") val packageId: String?,
    @SerializedName("camera_sn") val cameraSN: String?,
    @SerializedName("service") val service: String?,
    @SerializedName("invitation_code") val invitationCode: String?,
    @SerializedName("money_source") val moneySource: String?,// phương thức thanh toán
    @SerializedName("info_service") val infoService: Descriptions?,
    @SerializedName("service_status") val serviceStatus: Int?, //0 - hết hạn 1 - Đang sử dụng 2 -Chưa kích hoạt
    @SerializedName("user_id") val userId: String?,// ID của người đăng ký cloud
    @SerializedName("user_use") val userUse: String?,// ID của người sử dụng gói cloud
)   : Parcelable {
    fun numberSort(): Int {
        return when (serviceStatus) {
            1 -> 2
            2 -> 1
            0 -> 0
            else -> 0
        }
    }
    // gói đó là trả sau  = true
    fun isPostpaid():Boolean{
        return  paymentMethod == "BCCS"
    }
    fun isGiftRelatives():Boolean{
        return userId != userUse
    }
    /*
          Kiểm tra gói có cloud có phải được nhận từ người khác tặng k.
     */
    fun isGiftReceive(userIdAccount: String?):Boolean{
        return  isGiftRelatives() && userIdAccount == userUse
    }
}
@Parcelize
data class Descriptions(
    val en: String = "",
    val noteEn: String = "",
    val vi: String = "",
    val noteVi: String = "",
    val discountPercentage: Int = 0,
    val originalPrice: Long = 0L,
    val promoted: Boolean = false,
) : Parcelable

data class PaymentGatewayAutoResponse(
    @SerializedName("code")
    val code: Int? = 0,
    @SerializedName("data")
    val data: ListPaymentGatewayAuto?,
)

data class ListPaymentGatewayAuto(
    val total: Int?,
    val limit: Int?,
    val offset: Int?,
    @SerializedName("data")
    val data: List<PaymentGatewayAuto>?,
)

data class PaymentGatewayAuto(
    @SerializedName("account_id")
    val accountId: String?,
    @SerializedName("serial")
    val serial: String?,
    @SerializedName("status")
    val status: String?,
    @SerializedName("method")
    val method: String?,
    @SerializedName("update_time")
    val updateTime: Long?,
)

data class DataManagerAutoChargingResponse(
    @SerializedName("code")
    val code: Int?,
    @SerializedName("data")
    val data: List<AutoChargingData>?,
)

data class AutoChargingData(
    @SerializedName("account_id")
    val accountId: String?,

    @SerializedName("serial")
    val serial: String?,

    @SerializedName("device_name")
    val deviceName: String?,

    @SerializedName("status")
    val status: String?,

    @SerializedName("method")
    val method: String?,

    @SerializedName("update_time")
    val updateTime: Long?,

    @SerializedName("package_code")
    val packageCode: String?,

    @SerializedName("end_date_time")
    val endDateTime: Long?,

    @SerializedName("package_name")
    val packageName: String?,
)

data class LinkContactResponse(
    @SerializedName("facebook")
    val facebook: String?,

    @SerializedName("telegram")
    val telegram: String?,
)


data class ListStatusCloudCameraResponse(
    @SerializedName("code") val code: Int,
    @SerializedName("msg") val msg: String,
    @SerializedName("data") val data: DataStatusCloudCameraResponse? = null
)

data class DataStatusCloudCameraResponse(
    val total: Int?,
    val limit: Int?,
    val offset: Int?,
    val data: List<DataStatusCloud>?,
)
data class DataStatusCloud(
    @SerializedName("cam") val camera: CameraStatusCloud,
    @SerializedName("bill") val bill: BillCloud,
)
data class CameraStatusCloud(
    val id: String? ="",
    val name: String? ="",
    val owner: String? ="",
    val model: String? ="",
    val orgid: String? ="",
    val createby: String? ="",
    val serial: String? ="",
)

data class BillCloud(
    val order_id: String? ="",
    val payment_method: String? ="",
    @SerializedName("info_service") val infoService: Descriptions?,
    @SerializedName("service_status") val serviceStatus: Int?, //0 - hết hạn 1 - Đang sử dụng 2 -Chưa kích hoạt
)

data class CloudReferCodeCloudResponse(
    @SerializedName("code") val code: Int,
    @SerializedName("msg") val msg: String,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: CloudReferCodeCloud?,
)

data class CloudReferCodeCloud (
    @SerializedName("staff_code") val staffCode: String,
    @SerializedName("staff_id") val staffId: String,
    @SerializedName("shop_code") val shopCode: String,
    @SerializedName("status") val status: String,
    @SerializedName("name") val name: String,
    @SerializedName("phone") val phone: String,
)

