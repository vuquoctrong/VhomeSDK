package com.viettel.vht.sdk.network.repository

import com.vht.sdkcore.pref.RxPreferences
import com.viettel.vht.sdk.network.ApiInterface
import com.viettel.vht.sdk.network.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CloudRepository @Inject constructor(
    private val apiInterface: ApiInterface,
    private val rxPreferences: RxPreferences,
) {


    suspend fun checkRegistedCloud(serial: String) = withContext(Dispatchers.IO) {
        try {
            val response = apiInterface.getListCloudStorageRegistered(serial = serial)
            val isRegisted = response.data?.data?.isNotEmpty()
            isRegisted
        } catch (e: Exception) {
            false
        }
    }
    fun getListCloudStorageRegistered(serial: String) = flow {
        try {
            emit(Result.Loading)
            val response = apiInterface.getListCloudStorageRegistered(serial = serial)
            if (response.data != null) {
                emit(Result.Success(response.data))
            } else throw Exception()
        } catch (e: Exception) {
            emit(Result.Error())
        }
    }.flowOn(Dispatchers.IO)


}