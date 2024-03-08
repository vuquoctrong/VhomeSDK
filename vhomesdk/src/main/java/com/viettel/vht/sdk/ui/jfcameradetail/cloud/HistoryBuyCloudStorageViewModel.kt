package com.viettel.vht.sdk.ui.jfcameradetail.cloud

import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.vht.sdkcore.base.BaseViewModel
import com.vht.sdkcore.utils.SingleLiveEvent
import com.viettel.vht.sdk.model.AccountUseIdResponse
import com.viettel.vht.sdk.model.camera_cloud.CloudStorageRegistered
import com.viettel.vht.sdk.model.camera_cloud.DataStatusCloudCameraResponse
import com.viettel.vht.sdk.network.Result
import com.viettel.vht.sdk.network.repository.CloudRepository
import com.viettel.vht.sdk.network.repository.HomeRepository
import com.viettel.vht.sdk.ui.jfcameradetail.cloud.repository.CloudAccountPagingSource
import com.viettel.vht.sdk.ui.jfcameradetail.cloud.repository.CloudPagingSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryBuyCloudStorageViewModel @Inject constructor(
    private val cloudRepository: CloudRepository,
    private val homeRepository: HomeRepository

) : BaseViewModel() {
    var deviceId = ""
    var deviceSerial = ""
    var deviceName = ""
    val pagedListCloudHistory: Flow<PagingData<CloudStorageRegistered>> = Pager(config = PagingConfig(pageSize = 10)) {
        CloudPagingSource(cloudRepository,deviceSerial)
    }.flow.cachedIn(viewModelScope)

    val pagedListCloudAccountHistory: Flow<PagingData<CloudStorageRegistered>> = Pager(config = PagingConfig(pageSize = 10)) {
        CloudAccountPagingSource(cloudRepository,deviceSerial)
    }.flow.cachedIn(viewModelScope)

    private val _dataListCameraCloudAccount = SingleLiveEvent<Result<DataStatusCloudCameraResponse?>>()
    val responseListCameraCloudAccount: SingleLiveEvent<Result<DataStatusCloudCameraResponse?>> get() = _dataListCameraCloudAccount

    fun getListCameraCloudAccount(){
        viewModelScope.launch {
            cloudRepository.getListStatusCloudCamera().collect { it ->
                _dataListCameraCloudAccount.value = it
            }
        }
    }

    private val _informationAccount = SingleLiveEvent<com.vht.sdkcore.network.Result<AccountUseIdResponse>>()
    val informationAccount: SingleLiveEvent<com.vht.sdkcore.network.Result<AccountUseIdResponse>> get() = _informationAccount

    fun getInformationAccount(useId: String){
        viewModelScope.launch {
            homeRepository.getInformationAccountByUseId(useId).collect{
                _informationAccount.value = it
            }
        }
    }
}