package com.viettel.vht.sdk.ui.vtpay

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.vht.sdkcore.base.BaseViewModel
import com.vht.sdkcore.utils.SingleLiveEvent
import com.viettel.vht.sdk.model.camera_cloud.OTPOnOffAutoChargingCloudRequest
import com.viettel.vht.sdk.model.camera_cloud.OnOffAutoChargingCloud
import com.viettel.vht.sdk.model.camera_cloud.PaymentCodeResponse
import com.viettel.vht.sdk.network.repository.HomeRepository
import com.viettel.vht.sdk.network.repository.PaymentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TabSettingViewModel @Inject constructor(private val homeRepository: HomeRepository, private val paymentRepository: PaymentRepository,) :
    BaseViewModel() {


    private val _dataOTPOnOffPayLink = SingleLiveEvent<com.vht.sdkcore.network.Result<PaymentCodeResponse>>()
    val dataOTPOnOffPayLink: SingleLiveEvent<com.vht.sdkcore.network.Result<PaymentCodeResponse>> get() = _dataOTPOnOffPayLink

    fun getOTPOnOffAutoChargingCloud(serial: String){
        viewModelScope.launch {
            paymentRepository.getOTPOnOffAutoChargingCloud(OTPOnOffAutoChargingCloudRequest(serial = serial)).collect { it ->
                _dataOTPOnOffPayLink.value = it
            }
        }
    }

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