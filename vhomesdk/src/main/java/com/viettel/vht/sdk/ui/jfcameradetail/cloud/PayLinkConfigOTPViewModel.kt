package com.viettel.vht.sdk.ui.jfcameradetail.cloud

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.vht.sdkcore.base.BaseViewModel
import com.vht.sdkcore.utils.SingleLiveEvent
import com.viettel.vht.sdk.model.camera_cloud.BuyCloudVeRequest
import com.viettel.vht.sdk.model.camera_cloud.OnOffAutoChargingCloud
import com.viettel.vht.sdk.model.camera_cloud.PaymentCodeResponse
import com.viettel.vht.sdk.model.camera_cloud.PaymentPayLinkRequest
import com.viettel.vht.sdk.model.camera_cloud.PaymentResponse
import com.viettel.vht.sdk.network.repository.PaymentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PayLinkConfigOTPViewModel @Inject constructor(
    private val paymentRepository: PaymentRepository,

    ) : BaseViewModel() {
    var deviceId = ""
    var deviceSerial = ""
    var deviceName = ""


    val countdownTimer = MutableLiveData(120)
    private var countdownJob: Job? = null

    fun startCountdown() {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch(Dispatchers.IO) {
            for (second in 120 downTo 0 step 1) {
                countdownTimer.postValue(second)
                delay(1000)
            }
        }
    }

    fun stopCountdown() {
        countdownJob?.cancel()
        countdownTimer.postValue(120)
    }

    private val _getRegisterPaymentPackageV2 =
        SingleLiveEvent<com.vht.sdkcore.network.Result<PaymentCodeResponse>>()
    val getRegisterPaymentPackageV2: SingleLiveEvent<com.vht.sdkcore.network.Result<PaymentCodeResponse>> get() = _getRegisterPaymentPackageV2


    fun getLinkBuyCloudURl(bodyJson: BuyCloudVeRequest) {
        viewModelScope.launch {
            paymentRepository.getLinkBuyCloudV2(bodyJson).collect { result ->
                _getRegisterPaymentPackageV2.value = result
            }
        }
    }


    private val _getPaymentLinkPayWhenRegisterCloud =
        SingleLiveEvent<com.vht.sdkcore.network.Result<PaymentCodeResponse>>()
    val paymentLinkPayWhenRegisterCloudResponse: SingleLiveEvent<com.vht.sdkcore.network.Result<PaymentCodeResponse>> get() = _getPaymentLinkPayWhenRegisterCloud


    fun getPaymentLinkPayWhenRegisterCloud(bodyJson: PaymentPayLinkRequest) {
        viewModelScope.launch {
            paymentRepository.getPaymentLinkPayWhenRegisterCloud(bodyJson).collect { result ->
                _getPaymentLinkPayWhenRegisterCloud.value = result
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