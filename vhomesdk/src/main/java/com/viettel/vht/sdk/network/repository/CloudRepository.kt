package com.viettel.vht.sdk.network.repository

import com.vht.sdkcore.pref.RxPreferences
import com.viettel.vht.sdk.model.camera_cloud.CloudStorageRegisteredResponse
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

    suspend fun getListCloudStorageRegisteredAccount(
        limit: Int = 100,
        offset: Int = 0,
    ): CloudStorageRegisteredResponse? = withContext(Dispatchers.IO) {
        try {
            val response = apiInterface.getListCloudStorageRegisteredAccount(
                limit = limit,
                offset = offset,
                order = 0,
                userId = rxPreferences.getUserId()
            )
            if (response.data != null) {
                return@withContext  response.data
            } else return@withContext null
        } catch (e: Exception) {
            return@withContext null
        }
    }

    suspend fun getListHistoryBuyCloudStorageRegistered(
        serial: String,
        limit: Int = 100,
        offset: Int = 0,
        skipExpired: Boolean = false
    ): CloudStorageRegisteredResponse? = withContext(Dispatchers.IO) {
        try {
            val response = apiInterface.getListCloudStorageRegistered(
                serial = serial,
                limit = limit,
                offset = offset,
                skipExpired = skipExpired
            )
            if (response.data != null) {
                return@withContext  response.data
            } else return@withContext null
        } catch (e: Exception) {
            return@withContext null
        }
    }

    suspend fun getCloudStorageRegistered(serial: String, offset: Int = 0, limit: Int = 100) =
        withContext(Dispatchers.IO) {
            val response = apiInterface.getListCloudStorageRegistered(
                serial = serial,
                limit = limit,
                offset = offset,
                order = 1,
                skipExpired = true
            )
            response
        }

    fun getListStatusCloudCamera() = flow {
        try {
            emit(Result.Loading)
            val response = apiInterface.getListStatusCloudCamera()
            if (response.code == 200) {
                emit(Result.Success(response.data))
            } else{
                emit(Result.Error())
            }
        } catch (e: Exception) {
            emit(Result.Error(error = e.message?:"".toString()))
        }
    }.flowOn(Dispatchers.IO)

    fun getListCloudStoragePackage(vendor: String) = flow {
        try {
            emit(Result.Loading)
            val response = apiInterface.getListCloudStoragePackage(vendor)
            if (response.data != null) {
                emit(Result.Success(response.data))
            } else throw Exception()
        } catch (e: Exception) {
            emit(Result.Error())
        }
    }.flowOn(Dispatchers.IO)

    fun getStatusAutoChargingCamera(serial: String, method: String ="VTPAY-WALLET") = flow {
        try {
            emit(Result.Loading)
            val response = apiInterface.getStatusAutoChargingCamera(serial = serial)
            if (response.code == 200) {
                emit(Result.Success(response.data))
            } else{
                emit(Result.Error())
            }
        } catch (e: Exception) {
            emit(Result.Error(error = e.message?:"".toString()))
        }
    }.flowOn(Dispatchers.IO)

}