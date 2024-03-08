package com.viettel.vht.sdk.network.repository


import com.viettel.vht.sdk.model.camera_cloud.BuyCloudVeRequest
import com.viettel.vht.sdk.model.camera_cloud.OTPOnOffAutoChargingCloudRequest
import com.viettel.vht.sdk.model.camera_cloud.OnOffAutoChargingCloud
import com.viettel.vht.sdk.model.camera_cloud.PaymentCodeResponse
import com.viettel.vht.sdk.model.camera_cloud.PaymentPayLinkRequest
import com.viettel.vht.sdk.model.camera_cloud.PaymentRequest
import com.viettel.vht.sdk.model.camera_cloud.PaymentResponse
import com.viettel.vht.sdk.network.ApiInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class PaymentRepository @Inject constructor(
    private val apiInterface: ApiInterface,
) {

    fun paymentVT(bodyJson: PaymentRequest): Flow<com.vht.sdkcore.network.Result<PaymentCodeResponse>> = flow {
        try {
            emit(com.vht.sdkcore.network.Result.Loading())
            val response = apiInterface.paymentCamera(bodyJson)
            emit(com.vht.sdkcore.network.Result.Success(response))
        } catch (e: Exception) {
            emit(com.vht.sdkcore.network.Result.Error(e.toString()))
        }
    }.flowOn(Dispatchers.IO)

    // lấy mã OTP để bật tắt gia hạn
    fun getOTPOnOffAutoChargingCloud(bodyJson: OTPOnOffAutoChargingCloudRequest): Flow<com.vht.sdkcore.network.Result<PaymentCodeResponse>> =
        flow {
            try {
                emit(com.vht.sdkcore.network.Result.Loading())
                val response = apiInterface.getOTPOnOffAutoChargingCloud(bodyJson)
                emit(com.vht.sdkcore.network.Result.Success(response))
            } catch (e: Exception) {
                emit(com.vht.sdkcore.network.Result.Error(e.toString()))
            }
        }.flowOn(Dispatchers.IO)

    fun getLinkBuyCloudV2(bodyJson: BuyCloudVeRequest): Flow<com.vht.sdkcore.network.Result<PaymentCodeResponse>> = flow {
        try {
            emit(com.vht.sdkcore.network.Result.Loading())
            val response = apiInterface.buyCloudV2(bodyJson)
            emit(com.vht.sdkcore.network.Result.Success(response))
        } catch (e: Exception) {
            emit(com.vht.sdkcore.network.Result.Error(e.toString()))
        }
    }.flowOn(Dispatchers.IO)

    fun getPaymentLinkPayWhenRegisterCloud(bodyJson: PaymentPayLinkRequest): Flow<com.vht.sdkcore.network.Result<PaymentCodeResponse>> = flow {
        try {
            emit(com.vht.sdkcore.network.Result.Loading())
            val response = apiInterface.paymentOTPPayLinkCloud(bodyJson)
            emit(com.vht.sdkcore.network.Result.Success(response))
        } catch (e: Exception) {
            emit(com.vht.sdkcore.network.Result.Error(e.toString()))
        }
    }.flowOn(Dispatchers.IO)

    fun setOnOffAutoChargingCloud(bodyJson: OnOffAutoChargingCloud): Flow<com.vht.sdkcore.network.Result<PaymentCodeResponse>> = flow {
        try {
            emit(com.vht.sdkcore.network.Result.Loading())
            val response = apiInterface.setOnOffAutoChargingCloud(bodyJson)
            emit(com.vht.sdkcore.network.Result.Success(response))
        } catch (e: Exception) {
            emit(com.vht.sdkcore.network.Result.Error(e.toString()))
        }
    }.flowOn(Dispatchers.IO)

}