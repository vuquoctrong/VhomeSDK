package com.viettel.vht.sdk.network.repository


import com.viettel.vht.sdk.model.home.GetAppLogUpLoadLinkResponse
import com.viettel.vht.sdk.model.home.RequestGetAppLogUpLoadLink
import com.viettel.vht.sdk.model.home.RequestUpLoadAppLog
import com.viettel.vht.sdk.model.home.UpLoadAppLogResponse
import com.viettel.vht.sdk.network.ApiInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject


class HomeRepository @Inject constructor(
    private val smartHomeAPI: ApiInterface,
    ) {

    fun getAppLogAppLoadLInk(request: RequestGetAppLogUpLoadLink): Flow<com.vht.sdkcore.network.Result<GetAppLogUpLoadLinkResponse>> =
        flow {
            try {
                emit(com.vht.sdkcore.network.Result.Loading())
                val response = smartHomeAPI.getAppLogUpLoadLink(request)
                emit(com.vht.sdkcore.network.Result.Success(response))
            } catch (e: Exception) {
                emit(com.vht.sdkcore.network.Result.Error(e.toString()))
                e.printStackTrace()
            }
        }.flowOn(Dispatchers.IO)

    fun getAppLogStatus(request: RequestUpLoadAppLog): Flow<com.vht.sdkcore.network.Result<UpLoadAppLogResponse>> =
        flow {
            try {
                emit(com.vht.sdkcore.network.Result.Loading())
                val response = smartHomeAPI.upLoadAppLogStatus(request)
                emit(com.vht.sdkcore.network.Result.Success(response))
            } catch (e: Exception) {
                emit(com.vht.sdkcore.network.Result.Error(e.toString()))
                e.printStackTrace()
            }
        }.flowOn(Dispatchers.IO)


}

