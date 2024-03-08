package com.viettel.vht.sdk.ui.jfcameradetail.spread_camera

import androidx.lifecycle.viewModelScope
import com.vht.sdkcore.base.BaseViewModel
import com.vht.sdkcore.utils.SingleLiveEvent
import com.viettel.vht.sdk.model.ResponseCheckDeviceSpread
import com.viettel.vht.sdk.network.repository.HomeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SpreadCameraViewModel @Inject constructor(
    private val homeRepository: HomeRepository,

    ) :
    BaseViewModel() {
    private val _responseSetDeviceSpread= SingleLiveEvent<com.vht.sdkcore.network.Result<ResponseCheckDeviceSpread>>()
    val dataResponseSetSpreadCamera: SingleLiveEvent<com.vht.sdkcore.network.Result<ResponseCheckDeviceSpread>> get() = _responseSetDeviceSpread

    fun setSpreadCamera(deviceId: String,phone: String) {
        viewModelScope.launch {
            homeRepository.setDeviceSpread(
                deviceId,phone
            ).collect {
                _responseSetDeviceSpread.value = it
            }
        }
    }

}
