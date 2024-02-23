package com.viettel.vht.sdk.ui.splash

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.vht.sdkcore.base.BaseViewModel
import com.vht.sdkcore.pref.RxPreferences
import com.vht.sdkcore.utils.SingleLiveEvent
import com.viettel.vht.sdk.model.login.LoginResponse
import com.viettel.vht.sdk.network.repository.LoginRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SDKVHomeLoadingViewModel @Inject constructor(
    private val application: Application,
    private val rxPreferences: RxPreferences,
    private val loginRepository: LoginRepository,
) : BaseViewModel() {
    private val _loginResponse = SingleLiveEvent<com.vht.sdkcore.network.Result<LoginResponse>>()
    val loginResponse: LiveData<com.vht.sdkcore.network.Result<LoginResponse>> get() = _loginResponse
    fun login(body: String) {
        viewModelScope.launch {
            loginRepository.login(body).collect { loginResponse ->
                _loginResponse.value = loginResponse
            }
        }
        GlobalScope.launch{

        }
    }

}