package com.viettel.vht.sdk.network.repository


import android.util.Log
import com.utils.SignatureUtil
import com.utils.TimeMillisUtil
import com.vht.sdkcore.utils.executeWithRetry
import com.viettel.vht.main.model.ptz_jf.UpdateListPTZRequest
import com.viettel.vht.sdk.jfmanager.JFApiManager
import com.viettel.vht.sdk.model.DeviceResponse
import com.viettel.vht.sdk.model.camera_cloud.RegisterPayLinkRequest
import com.viettel.vht.sdk.model.camera_cloud.RequestRegisterPromotionFree
import com.viettel.vht.sdk.model.home.GetAppLogUpLoadLinkResponse
import com.viettel.vht.sdk.model.home.RequestGetAppLogUpLoadLink
import com.viettel.vht.sdk.model.home.RequestUpLoadAppLog
import com.viettel.vht.sdk.model.home.UpLoadAppLogResponse
import com.viettel.vht.sdk.network.ApiInterface
import com.viettel.vht.sdk.network.jf.JFApiCallInterface
import com.viettel.vht.sdk.network.jf.RetrofitHelperJF
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import retrofit2.Response
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

    fun getListPricingPaymentCloud() = flow {

        try {
            emit(com.vht.sdkcore.network.Result.Loading())
            val response = smartHomeAPI.getListPricingPaymentCloud()
            emit(com.vht.sdkcore.network.Result.Success(response))

        } catch (e: Exception) {
            emit(com.vht.sdkcore.network.Result.Error(e.message.toString()))

        }
    }.flowOn(Dispatchers.IO)

    fun getDeviceByOrg(orgId: String): Flow<com.vht.sdkcore.network.Result<DeviceResponse>> = flow {
        try {
            emit(com.vht.sdkcore.network.Result.Loading())
            val response = smartHomeAPI.getDevicesByOrg(orgId)
            val dataResponse = updateDeviceResponse(response)
            emit(com.vht.sdkcore.network.Result.Success(dataResponse))
        } catch (e: Exception) {
            emit(com.vht.sdkcore.network.Result.Error(e.toString()))
            e.printStackTrace()
        }
    }.flowOn(Dispatchers.IO)
    private fun updateDeviceResponse(deviceRes: DeviceResponse): DeviceResponse {
        deviceRes.devices.forEach {
            val gatewayId = it.gatewayId()
            val deviceGateway = deviceRes.devices.find { it.id == gatewayId }
            if (deviceGateway != null) it.status = deviceGateway.status
        }
        return deviceRes
    }


    suspend fun updateListPTZ(ptzRequest: UpdateListPTZRequest): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val response = smartHomeAPI.updateListPTZ(ptzRequest)
                Log.d("TAG", "updatePTZ: response  = $response")
                response.isSuccessful
            } catch (e: Exception) {
                Log.d("TAG", "updatePTZ: Error = $e")
                false
            }
        }
    }
    private val jFApiCallInterface = RetrofitHelperJF.getInstance().create(JFApiCallInterface::class.java)
    fun postPassword(body: String) = flow {
        try {
            emit(com.vht.sdkcore.network.Result.Loading())
            val timeMillis: String = TimeMillisUtil.getTimMillis()

            val secret: String = SignatureUtil.getEncryptStr(
                JFApiManager.uuid,
                JFApiManager.appKey,
                JFApiManager.appSecret, timeMillis, JFApiManager.movedCard
            )

            val response = jFApiCallInterface.postPassword(
                JFApiManager.uuid, JFApiManager.appKey, secret, timeMillis, body
            )
            emit(com.vht.sdkcore.network.Result.Success(response))
        } catch (e: Exception) {
            emit(com.vht.sdkcore.network.Result.Error(e.message.toString()))
            Log.d(
                "postPassword",
                "error exception " + e.message.toString() + "|| cause = " + e.cause?.message
            )

        }
    }

    fun editNameDevice(deviceId: String, nameEdit: String): Flow<com.vht.sdkcore.network.Result<Response<Unit>>> = flow {
        try {
            emit(com.vht.sdkcore.network.Result.Loading())
            val jsonObject = JSONObject()
            jsonObject.put("name", nameEdit)
            val body: RequestBody =
                RequestBody.create(
                    "application/json".toMediaTypeOrNull(),
                    jsonObject.toString()
                )

            val response = smartHomeAPI.editNameDevice(
                deviceId,
                body,
                "EDIT_DEVICE_NAME",
                "EDIT_NAME",
                deviceId
            )
            response.let {
                emit(com.vht.sdkcore.network.Result.Success(it))
            }
        } catch (e: Exception) {
            emit(com.vht.sdkcore.network.Result.Error(e.toString()))
            e.printStackTrace()
        }
    }.flowOn(Dispatchers.IO)

    fun deleteDevice(deviceId: String): Flow<com.vht.sdkcore.network.Result<Response<Unit>>> = flow {
        emit(com.vht.sdkcore.network.Result.Loading())
        var isError = true
        executeWithRetry {
            val response = smartHomeAPI.deleteDevice(deviceId)
            if (response.isSuccessful) {
                emit(com.vht.sdkcore.network.Result.Success(response))
                isError = false
                return@executeWithRetry false
            } else {
                return@executeWithRetry true
            }
        }
        if (isError) {
            emit(com.vht.sdkcore.network.Result.Error("Lỗi đồng bộ thiết bị"))
            //TODO: Bắn log lỗi đồng bộ thiết bị về server VHT để người vận hành xử lý
        }
    }.flowOn(Dispatchers.IO)


    fun getConfigFeatureList() = flow {
        try {
            emit(com.vht.sdkcore.network.Result.Loading())
            val response = smartHomeAPI.getConfigFeatureList()
            emit(com.vht.sdkcore.network.Result.Success(response))
        } catch (e: Exception) {
            emit(com.vht.sdkcore.network.Result.Error(e.message.toString()))

        }
    }.flowOn(Dispatchers.IO)
    fun checkDeviceSpread(idDevice: String) = flow {
        try {
            val request = JSONObject().apply {
                put("device_id", idDevice)
            }.toString().toRequestBody()
            emit(com.vht.sdkcore.network.Result.Loading())
            val response = smartHomeAPI.deviceSpreadCheck(request)
            emit(com.vht.sdkcore.network.Result.Success(response))
        } catch (e: Exception) {
            emit(com.vht.sdkcore.network.Result.Error(e.message.toString()))

        }
    }.flowOn(Dispatchers.IO)

    fun getInformationCloudRelatives(serial: String, phone: String) = flow {
        try {
            val request = JSONObject().apply {
                put("serial", serial)
                put("phone", phone)
            }.toString().toRequestBody()
            emit(com.vht.sdkcore.network.Result.Loading())
            val response = smartHomeAPI.getInformationCloudRelatives(request)
            emit(com.vht.sdkcore.network.Result.Success(response))
        } catch (e: Exception) {
            emit(com.vht.sdkcore.network.Result.Error(e.message.toString()))

        }
    }.flowOn(Dispatchers.IO)


    fun setListCameraPricingPaymentCloud(requestBody: RequestRegisterPromotionFree) = flow {

        try {
            emit(com.vht.sdkcore.network.Result.Loading())
            val response = smartHomeAPI.setPricingPaymentCloud(requestBody)
            emit(com.vht.sdkcore.network.Result.Success(response))

        } catch (e: Exception) {
            emit(com.vht.sdkcore.network.Result.Error(e.message.toString()))

        }
    }.flowOn(Dispatchers.IO)


    fun getListPaymentGatewayLink() = flow {
        try {
            emit(com.vht.sdkcore.network.Result.Loading())
            val response = smartHomeAPI.getListPaymentGatewayLink()
            if (response.code == 200) {
                emit(com.vht.sdkcore.network.Result.Success(response))
            } else {
                emit(com.vht.sdkcore.network.Result.Error(response.msg.toString()))
            }

        } catch (e: Exception) {
            emit(com.vht.sdkcore.network.Result.Error(e.message.toString()))

        }
    }.flowOn(Dispatchers.IO)

    fun registerPaymentGatewayLink(request: RegisterPayLinkRequest) = flow {
        try {
            emit(com.vht.sdkcore.network.Result.Loading())
            val response = smartHomeAPI.registerPaymentGatewayLink(request)
            emit(com.vht.sdkcore.network.Result.Success(response))

        } catch (e: Exception) {
            emit(com.vht.sdkcore.network.Result.Error(e.message.toString()))

        }
    }.flowOn(Dispatchers.IO)

    fun getDataCheckCloudReferCloud(code: String) = flow {
        try {
            emit(com.vht.sdkcore.network.Result.Loading())
            val response = smartHomeAPI.getDataCheckCloudReferCloud(code)
            emit(com.vht.sdkcore.network.Result.Success(response))
        } catch (e: Exception) {
            emit(com.vht.sdkcore.network.Result.Error( e.message?:"".toString()))
        }
    }.flowOn(Dispatchers.IO)
    fun getInformationAccountByUseId(useId: String) = flow {
        try {
            emit(com.vht.sdkcore.network.Result.Loading())
            val response = smartHomeAPI.getInformationAccountByUseId(useId)
            emit(com.vht.sdkcore.network.Result.Success(response))
        } catch (e: Exception) {
            emit(com.vht.sdkcore.network.Result.Error(e.toString()))
            e.printStackTrace()
        }
    }.flowOn(Dispatchers.IO)

    fun setDeviceSpread(idDevice: String, phone: String) = flow {
        try {
            val request = JSONObject().apply {
                put("device_id", idDevice)
                put("phone", phone)
            }.toString().toRequestBody()
            emit(com.vht.sdkcore.network.Result.Loading())
            val response = smartHomeAPI.setDeviceSpread(request)
            emit(com.vht.sdkcore.network.Result.Success(response))
        } catch (e: Exception) {
            emit(com.vht.sdkcore.network.Result.Error(e.message.toString()))

        }
    }.flowOn(Dispatchers.IO)



}

