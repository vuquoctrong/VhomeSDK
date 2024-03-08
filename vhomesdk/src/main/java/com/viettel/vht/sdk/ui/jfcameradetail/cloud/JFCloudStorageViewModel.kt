package com.viettel.vht.sdk.ui.jfcameradetail.cloud

import androidx.lifecycle.*
import androidx.paging.toLiveData
import androidx.paging.*
import com.vht.sdkcore.base.BaseViewModel
import com.vht.sdkcore.utils.SingleLiveEvent
import com.viettel.vht.sdk.model.camera_cloud.BuyCloudVeRequest
import com.viettel.vht.sdk.model.camera_cloud.CloudReferCodeCloudResponse
import com.viettel.vht.sdk.model.camera_cloud.CloudStoragePackage
import com.viettel.vht.sdk.model.camera_cloud.CloudStorageRegistered
import com.viettel.vht.sdk.model.camera_cloud.CloudStorageRegisteredResponse
import com.viettel.vht.sdk.model.camera_cloud.ListPaymentGatewayAuto
import com.viettel.vht.sdk.model.camera_cloud.ListPaymentLinkResponse
import com.viettel.vht.sdk.model.camera_cloud.OTPOnOffAutoChargingCloudRequest
import com.viettel.vht.sdk.model.camera_cloud.PaymentCodeResponse
import com.viettel.vht.sdk.model.camera_cloud.RegisterPayLinkRequest
import com.viettel.vht.sdk.model.camera_cloud.SearchCloudRequest
import com.viettel.vht.sdk.model.camera_cloud.URLPayLinkResponse
import com.viettel.vht.sdk.model.home.FeatureConfigResponse
import com.viettel.vht.sdk.network.repository.CloudRepository
import com.viettel.vht.sdk.network.repository.HomeRepository
import com.viettel.vht.sdk.network.repository.PaymentRepository

import com.viettel.vht.sdk.ui.jfcameradetail.cloud.repository.CloudPagingSource
import com.viettel.vht.sdk.ui.jfcameradetail.cloud.repository.JFCloudEventDataSourceFactory
import com.viettel.vht.sdk.ui.jfcameradetail.cloud.repository.JFCloudEventPageKeyedDataSource
import com.viettel.vht.sdk.ui.jfcameradetail.cloud.repository.NetworkListing
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class JFCloudStorageViewModel @Inject constructor(
    private val cloudRepository: CloudRepository,
    private val homeRepository: HomeRepository,
    private val paymentRepository: PaymentRepository,
) : BaseViewModel() {

    var deviceId = ""
    var deviceName = ""
    var deviceSerial = ""
        set(value) {
            field = value
            serialLiveData.postValue(value)
        }
    var isShowExtendAutoCloud = true
    var deviceType = ""
    var cloudPackages = MutableLiveData<List<CloudStoragePackage>?>()
    var cloudRegistered = MutableLiveData<List<CloudStorageRegistered>?>()

    fun getListCloudStoragePackage(vendor: String) {
        viewModelScope.launch(Dispatchers.IO) {
            cloudRepository.getListCloudStoragePackage(vendor).collect {
                if (it is com.viettel.vht.sdk.network.Result.Success) {
                    cloudPackages.postValue(it.data?.data)
                }
            }
        }
    }

    fun getListCloudStorageRegistered(serial: String) {
        viewModelScope.launch(Dispatchers.IO) {
            cloudRepository.getListCloudStorageRegistered(serial).collect {
                when (it) {
                    is com.viettel.vht.sdk.network.Result.Loading -> {
                        isLoading.postValue(true)
                    }
                    is com.viettel.vht.sdk.network.Result.Error -> {
                        isLoading.postValue(false)
                        Timber.e("${it.error}")
                    }
                    is com.viettel.vht.sdk.network.Result.Success -> {
                        isLoading.postValue(false)
                        cloudRegistered.postValue(it.data?.data)
                    }
                }
            }
        }
    }

    val serialLiveData = MutableLiveData("")

    val eventSearchLiveData = MediatorLiveData<String>().also {
        it.addSource(serialLiveData) { text ->
            it.value = text
        }
    }

    var sourceData = MutableLiveData<JFCloudEventPageKeyedDataSource>()
    val eventNetworkListing = eventSearchLiveData.map { serial ->
        val factory =
            JFCloudEventDataSourceFactory(
                SearchCloudRequest(serial = serial),
                viewModelScope,
                cloudRepository
            )
        sourceData = factory.source
        val requestPage = factory.source.switchMap { it.requestPageLiveData }
        val networkState = factory.source.switchMap { it.networkStateLiveData }
        val liveData = factory.toLiveData(
            Config(
                pageSize = 10,
                enablePlaceholders = false,
                initialLoadSizeHint = 10
            )
        )

        return@map NetworkListing(
            pagedList = liveData,
            requestPage = requestPage,
            networkState = networkState
        )
    }
    private val _listCloudLiveData =
        SingleLiveEvent<com.viettel.vht.sdk.network.Result<CloudStorageRegisteredResponse?>>()

    val listCloudLiveData: SingleLiveEvent<com.viettel.vht.sdk.network.Result<CloudStorageRegisteredResponse?>> get() = _listCloudLiveData

    fun getListCloudStorage(serial: String) {
        viewModelScope.launch(Dispatchers.IO) {
            cloudRepository.getListCloudStorageRegistered(serial).collect {
                _listCloudLiveData.postValue(it)
            }
        }
    }

    private val _statusAutoChargingCloud = SingleLiveEvent<com.viettel.vht.sdk.network.Result<ListPaymentGatewayAuto?>>()
    val statusAutoChargingCloud: SingleLiveEvent<com.viettel.vht.sdk.network.Result<ListPaymentGatewayAuto?>> get() = _statusAutoChargingCloud

    fun getServerStatusAuto(serial: String) {
        viewModelScope.launch(Dispatchers.IO) {
            cloudRepository.getStatusAutoChargingCamera(serial).collect {
                _statusAutoChargingCloud.postValue(it)
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

    private val _dataOTPOnOffPayLink =
        SingleLiveEvent<com.vht.sdkcore.network.Result<PaymentCodeResponse>>()
    val dataOTPOnOffPayLink: SingleLiveEvent<com.vht.sdkcore.network.Result<PaymentCodeResponse>> get() = _dataOTPOnOffPayLink

    fun getOTPOnOffAutoChargingCloud(serial: String) {
        viewModelScope.launch {
            paymentRepository.getOTPOnOffAutoChargingCloud(OTPOnOffAutoChargingCloudRequest(serial = serial))
                .collect { it ->
                    _dataOTPOnOffPayLink.value = it
                }
        }
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

    fun getCheckInvitationCode(){
        viewModelScope.launch {
            homeRepository.getConfigFeatureList().collect{ result ->
                _dataCheckInvitationCode.value = result
            }
        }
    }

    val pagedListCloudHistory: Flow<PagingData<CloudStorageRegistered>> =
        Pager(config = PagingConfig(pageSize = 10)) {
            CloudPagingSource(cloudRepository, deviceSerial)
        }.flow.cachedIn(viewModelScope)

    val eventListCloudLiveData = eventNetworkListing.switchMap { it.pagedList }
    val requestPageCloudLiveData = eventNetworkListing.switchMap { it.requestPage }

    sealed class UIState {
        class Error(val message: String) : UIState()
        object RegisterSuccess : UIState()
    }
}