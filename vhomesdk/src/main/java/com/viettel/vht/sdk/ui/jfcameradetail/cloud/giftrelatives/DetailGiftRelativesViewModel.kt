package com.viettel.vht.sdk.ui.jfcameradetail.cloud.giftrelatives

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.vht.sdkcore.base.BaseViewModel
import com.vht.sdkcore.network.Status
import com.vht.sdkcore.utils.SingleLiveEvent
import com.viettel.vht.sdk.model.camera_cloud.DataInformationRelatives
import com.viettel.vht.sdk.network.repository.HomeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailGiftRelativesViewModel @Inject constructor(
    private val homeRepository: HomeRepository,

    ) : BaseViewModel() {

    val mActionState = SingleLiveEvent<GiftRelativesActionState>()
    val validatePhoneLiveData = MutableLiveData<Boolean>(false)
    val validateSerialLiveData = MutableLiveData<Boolean>(false)
    val validateSuccessLiveData = MediatorLiveData<Boolean>()

    init {
        validateSuccessLiveData.addSource(validatePhoneLiveData) { validateName ->
            checkAndSetCombinedLiveDataValue()
        }
        validateSuccessLiveData.addSource(validateSerialLiveData) { validatePhone ->
            checkAndSetCombinedLiveDataValue()
        }

    }

    val edtSerialCameraLiveData = MutableLiveData<String>()
    val edtPhoneLiveData = MutableLiveData<String>()

    private fun checkAndSetCombinedLiveDataValue() {
        val isAllValid =
            validatePhoneLiveData.value == true &&
                    validateSerialLiveData.value == true

        validateSuccessLiveData.value = isAllValid
    }




    fun checkInformationCloudRelative(phoneRelative: String, serialRelative: String) {
        viewModelScope.launch {
            homeRepository.getInformationCloudRelatives(serial = serialRelative, phone = phoneRelative).collect { result ->
                when (result.status) {
                    Status.LOADING -> isLoading.postValue(true)

                    Status.ERROR -> isLoading.postValue(false)

                    Status.SUCCESS -> {
                        isLoading.postValue(false)
                        if (result.data?.code == 200) {
                            mActionState.value = GiftRelativesActionState.InformationCloudSuccess(
                                true,
                                data = result.data?.data
                            )
                        } else {
                            mActionState.value =
                                GiftRelativesActionState.InformationCloudSuccess(
                                    false,
                                    error = result.data?.message
                                )
                        }
                    }
                }
            }
        }
    }


}

sealed class GiftRelativesActionState {
    class InformationCloudSuccess(val isSuccess: Boolean,val error: String? = null,val data: DataInformationRelatives? = null) : GiftRelativesActionState()
}