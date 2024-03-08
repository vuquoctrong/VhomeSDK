package com.viettel.vht.sdk.model.camera_cloud

import com.google.gson.annotations.SerializedName


data class ListPaymentLinkResponse(
    @SerializedName("code")
    val code: Int? = null,
    @SerializedName("msg")
    val msg: String? = null,
    @SerializedName("data")
    val data: List<PaymentResponse>? = listOf()
)

data class PaymentResponse(
    @SerializedName("order_id")
    val orderId: String? = null,
    @SerializedName("phone")
    val phone: String? = null,
    @SerializedName("vt_transaction_id")
    val vtTransactionId: String? = null,
    @SerializedName("daily_amount_limit")
    val dailyAmountLimit: String? = null,
    @SerializedName("trans_amount_limit")
    val transAmountLimit: String? = null,
    @SerializedName("created_time")
    val createdTime: Long = 0L,
    @SerializedName("type_request")
    val typeRequest: String? = null,
    @SerializedName("account_id")
    val accountId: String? = null,
)

data class RegisterPayLinkRequest(
    @SerializedName("phone_payment")
    val phonePayment: String? = null,
    @SerializedName("daily_amount_limit")
    val dailyAmountLimit: Int? = 1000000,
    @SerializedName("trans_amount_limit")
    val trans_amount_limit: Int? = 25000,
)

data class URLPayLinkResponse(
    @SerializedName("code")
    val code: Int? = null,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("data")
    val data: String? = null,
)


