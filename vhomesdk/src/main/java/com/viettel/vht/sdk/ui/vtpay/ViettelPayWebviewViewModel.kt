package com.viettel.vht.sdk.ui.vtpay

import androidx.lifecycle.viewModelScope
import com.vht.sdkcore.base.BaseViewModel
import com.vht.sdkcore.pref.RxPreferences
import com.vht.sdkcore.utils.SingleLiveEvent
import com.viettel.vht.sdk.model.camera_cloud.BuyCloudVeRequest
import com.viettel.vht.sdk.model.camera_cloud.OnOffAutoChargingCloud
import com.viettel.vht.sdk.model.camera_cloud.PaymentCodeResponse
import com.viettel.vht.sdk.model.camera_cloud.PaymentRequest
import com.viettel.vht.sdk.network.repository.PaymentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class ViettelPayWebviewViewModel @Inject constructor(
    private val paymentRepository: PaymentRepository,
    private val rxPreferences: RxPreferences
) : BaseViewModel() {

    val paymentResult = SingleLiveEvent<PaymentCodeResponse?>()
    fun payment(request: PaymentRequest) {
        viewModelScope.launch {
            paymentRepository.paymentVT(request).collect { result ->
                when (result) {
                    is com.vht.sdkcore.network.Result.Success -> {
                        isLoading.value = false
                        paymentResult.value = result.data
                    }

                    is com.vht.sdkcore.network.Result.Loading -> {
                        isLoading.value = true
                    }

                    is com.vht.sdkcore.network.Result.Error -> {
                        isLoading.value = false
                        paymentResult.value = null
                    }
                }
            }
        }
    }

    private val _getLinkBuyCloudURl =
        SingleLiveEvent<com.vht.sdkcore.network.Result<PaymentCodeResponse>>()
    val responseGetLinkBuyCloudURl: SingleLiveEvent<com.vht.sdkcore.network.Result<PaymentCodeResponse>> get() = _getLinkBuyCloudURl

    fun getLinkBuyCloudURl(bodyJson: BuyCloudVeRequest) {
        viewModelScope.launch {
            paymentRepository.getLinkBuyCloudV2(bodyJson).collect { result ->
                _getLinkBuyCloudURl.value = result
            }
        }
    }

    private val _setOnOffAutoChargingCloud =
        SingleLiveEvent<com.vht.sdkcore.network.Result<PaymentCodeResponse>>()
    val responseOnOffAutoChargingCloud: SingleLiveEvent<com.vht.sdkcore.network.Result<PaymentCodeResponse>> get() = _setOnOffAutoChargingCloud


    fun setOnOffAutoChargingCloud(bodyJson: OnOffAutoChargingCloud) {
        viewModelScope.launch {
            paymentRepository.setOnOffAutoChargingCloud(bodyJson).collect { result ->
                _setOnOffAutoChargingCloud.value = result
            }
        }
    }


}