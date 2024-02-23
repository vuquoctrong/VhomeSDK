package com.viettel.vht.sdk.ui.main

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.viewModelScope
import com.vht.sdkcore.base.BaseViewModel
import com.vht.sdkcore.pref.RxPreferences
import com.vht.sdkcore.utils.SingleLiveEvent
import com.viettel.vht.sdk.model.home.GetAppLogUpLoadLinkResponse
import com.viettel.vht.sdk.model.home.RequestGetAppLogUpLoadLink
import com.viettel.vht.sdk.model.home.RequestUpLoadAppLog
import com.viettel.vht.sdk.model.home.UpLoadAppLogResponse
import com.viettel.vht.sdk.network.repository.HomeRepository

import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject

@HiltViewModel
@SuppressLint("StaticFieldLeak")
class MainViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val homeRepository: HomeRepository,
    private val rxPreferences: RxPreferences
) : BaseViewModel() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)


    private val _appLogUpLoadLinkResponse =
        SingleLiveEvent<com.vht.sdkcore.network.Result<GetAppLogUpLoadLinkResponse>>()
    val appLogUpLoadLinkResponse: SingleLiveEvent<com.vht.sdkcore.network.Result<GetAppLogUpLoadLinkResponse>> get() = _appLogUpLoadLinkResponse

    private val _appLogStatus =
        SingleLiveEvent<com.vht.sdkcore.network.Result<UpLoadAppLogResponse>>()
    val appLogStatus: SingleLiveEvent<com.vht.sdkcore.network.Result<UpLoadAppLogResponse>> get() = _appLogStatus

    private val _uploadFileToServerStatus =
        SingleLiveEvent<Boolean>()
    val uploadFileToServerStatus: SingleLiveEvent<Boolean> get() = _uploadFileToServerStatus


    fun getAppLogUpLoadLink(request: RequestGetAppLogUpLoadLink) {
        viewModelScope.launch {
            homeRepository.getAppLogAppLoadLInk(request).collect { result ->
                _appLogUpLoadLinkResponse.value = result
            }
        }
    }

    fun getAppLogStatus(request: RequestUpLoadAppLog) {
        viewModelScope.launch {
            homeRepository.getAppLogStatus(request).collect { result ->
                _appLogStatus.value = result
            }
        }
    }

    fun uploadFileToServer(file: File, serverUrl: String) {
        applicationScope.launch {
            try {
                val connection = URL(serverUrl).openConnection() as HttpURLConnection
                connection.doOutput = true
                connection.requestMethod = "PUT"
                connection.setRequestProperty("Content-Type", "text/plain")

                val fileInputStream = FileInputStream(file)
                val outputStream = connection.outputStream
                fileInputStream.copyTo(outputStream)
                outputStream.close()
                fileInputStream.close()

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    file.delete()
                    viewModelScope.launch(Dispatchers.Main) {
                        _uploadFileToServerStatus.value = true
                    }
                    // File uploaded successfully
                    // Handle response
                } else {
                    viewModelScope.launch(Dispatchers.Main) {
                        _uploadFileToServerStatus.value = false
                    }
                    // Handle error
                }

                connection.disconnect()
            } catch (e: Exception) {

            }
        }
    }

}