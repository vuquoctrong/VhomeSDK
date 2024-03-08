package com.viettel.vht.sdk.ui.jfcameradetail.cloud.promotion

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.vht.sdkcore.base.BaseViewModel
import com.vht.sdkcore.network.Result
import com.vht.sdkcore.utils.SingleLiveEvent
import com.viettel.vht.sdk.model.camera_cloud.CloudReferCodeCloudResponse
import com.viettel.vht.sdk.model.camera_cloud.DataFreePricing
import com.viettel.vht.sdk.model.camera_cloud.ListPaymentLinkResponse
import com.viettel.vht.sdk.model.camera_cloud.OTPOnOffAutoChargingCloudRequest
import com.viettel.vht.sdk.model.camera_cloud.PaymentCodeResponse
import com.viettel.vht.sdk.model.camera_cloud.PaymentResponse
import com.viettel.vht.sdk.model.camera_cloud.PricingCloudResponse
import com.viettel.vht.sdk.model.camera_cloud.RegisterPayLinkRequest
import com.viettel.vht.sdk.model.camera_cloud.RequestRegisterPromotionFree
import com.viettel.vht.sdk.model.camera_cloud.URLPayLinkResponse
import com.viettel.vht.sdk.model.home.FeatureConfigResponse
import com.viettel.vht.sdk.network.repository.HomeRepository
import com.viettel.vht.sdk.network.repository.PaymentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailPromotionViewModel @Inject constructor(
    private val homeRepository: HomeRepository,
    private val paymentRepository: PaymentRepository

) : BaseViewModel() {
    private val _listPricingCloud = SingleLiveEvent<Result<PricingCloudResponse>>()
    val dataListPricingCloud: SingleLiveEvent<Result<PricingCloudResponse>> get() = _listPricingCloud

    fun getListPricingPaymentCloud() {
        viewModelScope.launch {
            homeRepository.getListPricingPaymentCloud().collect {
                _listPricingCloud.value = it
            }
        }
    }

    private val _setCamerasPricingCloud = SingleLiveEvent<Result<PaymentCodeResponse>>()
    val setCamerasPricingCloudResponse: SingleLiveEvent<Result<PaymentCodeResponse>> get() = _setCamerasPricingCloud

    fun setListCameraPricingPaymentCloud(requestBody: RequestRegisterPromotionFree) {
        viewModelScope.launch {
            homeRepository.setListCameraPricingPaymentCloud(requestBody).collect {
                _setCamerasPricingCloud.value = it
            }
        }
    }

    private val _listPaymentLink =
        SingleLiveEvent<com.vht.sdkcore.network.Result<ListPaymentLinkResponse>>()
    val listPaymentLink: SingleLiveEvent<com.vht.sdkcore.network.Result<ListPaymentLinkResponse>> get() = _listPaymentLink

    fun getListPaymentLink() {
        viewModelScope.launch {
            homeRepository.getListPaymentGatewayLink().collect { it ->
                _listPaymentLink.value = it
            }
        }
    }

    private val _urlRegisterPayLink =
        SingleLiveEvent<com.vht.sdkcore.network.Result<URLPayLinkResponse>>()
    val urlRegisterPayLink: SingleLiveEvent<com.vht.sdkcore.network.Result<URLPayLinkResponse>> get() = _urlRegisterPayLink

    fun getURLRegisterPaymentLink(phone: String) {
        viewModelScope.launch {
            homeRepository.registerPaymentGatewayLink(RegisterPayLinkRequest(phonePayment = phone))
                .collect { it ->
                    _urlRegisterPayLink.value = it
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

    private val _dataOTPOnOffPayLink =
        SingleLiveEvent<com.vht.sdkcore.network.Result<PaymentCodeResponse>>()
    val dataOTPOnOffPayLink: SingleLiveEvent<com.vht.sdkcore.network.Result<PaymentCodeResponse>> get() = _dataOTPOnOffPayLink

    // chuyền serial = "" để lấy gói cước free OTP
    fun getOTPOnOffAutoChargingCloud(serial: String) {
        viewModelScope.launch {
            paymentRepository.getOTPOnOffAutoChargingCloud(
                OTPOnOffAutoChargingCloudRequest(
                    serial = serial,
                    typeOtp = "free-cloud-payment"
                )
            ).collect { it ->
                _dataOTPOnOffPayLink.value = it
            }
        }
    }


    val responseReferCodeCloud = SingleLiveEvent<CloudReferCodeCloudResponse?>()

    val referCodeCloud = MutableLiveData<String>()

    fun getDataCheckCloudReferCloud(code: String) {
        viewModelScope.launch {
            homeRepository.getDataCheckCloudReferCloud(code).collect { result ->
                when (result) {
                    is com.vht.sdkcore.network.Result.Loading -> {
                        isLoading.postValue(true)
                    }

                    is com.vht.sdkcore.network.Result.Error -> {
                        isLoading.postValue(false)

                    }

                    is com.vht.sdkcore.network.Result.Success -> {
                        isLoading.postValue(false)
                        responseReferCodeCloud.value = result.data
                    }
                }
            }
        }
    }

    private val _dataCheckInvitationCode =
        SingleLiveEvent<com.vht.sdkcore.network.Result<FeatureConfigResponse>>()
    val dataCheckInvitationCode: SingleLiveEvent<com.vht.sdkcore.network.Result<FeatureConfigResponse>> get() = _dataCheckInvitationCode

    fun getCheckInvitationCode() {
        viewModelScope.launch {
            homeRepository.getConfigFeatureList().collect { result ->
                _dataCheckInvitationCode.value = result
            }
        }
    }

    val cbAllDeviceLiveData = MutableLiveData<Boolean>(false)
    val cbExtendLiveData = MutableLiveData<Boolean>(false)
    val listCamera = ArrayList<DataFreePricing>()


}