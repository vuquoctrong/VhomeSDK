package com.viettel.vht.sdk.network.repository

import com.google.gson.Gson
import com.vht.sdkcore.pref.RxPreferences
import com.vht.sdkcore.utils.Define
import com.viettel.vht.sdk.model.CheckOwnerResponse
import com.viettel.vht.sdk.model.home.AttributeRequest
import com.viettel.vht.sdk.model.home.AttributeResponse
import com.viettel.vht.sdk.model.home.DataAddRequest
import com.viettel.vht.sdk.network.ApiInterface
import com.viettel.vht.sdk.utils.AttributeDataManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.retry
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import timber.log.Timber
import javax.inject.Inject

class AddDeviceRepository @Inject constructor(
    private val rxPreferences: RxPreferences,
    private val apiInterface: ApiInterface,
) {


    fun createCameraJF(cameraName: String, serialNumber: String, orgId: String, isInDoor: Boolean) = flow {
        val deviceRequest: DataAddRequest
        val listAttributes: MutableList<AttributeRequest> = arrayListOf()
        listAttributes.add(
            AttributeRequest(
                attributeKey = AttributeDataManager.AttributeKey.TYPE,
                value = Define.TYPE_DEVICE.CAMERA_JF,
                valueType = AttributeDataManager.ValueType.STR
            )
        )
        listAttributes.add(
            AttributeRequest(
                attributeKey = AttributeDataManager.AttributeKey.DEVICE_SERIAL,
                value = serialNumber,
                valueType = AttributeDataManager.ValueType.STR
            )
        )
        listAttributes.add(
            AttributeRequest(
                attributeKey = AttributeDataManager.AttributeKey.MODEL_CAMERA,
                value = if(isInDoor) Define.CAMERA_MODEL.CAMERA_JF_INDOOR else Define.CAMERA_MODEL.CAMERA_JF_OUTDOOR,
                valueType = AttributeDataManager.ValueType.STR
            )
        )
        listAttributes.add(
            AttributeRequest(
                attributeKey = AttributeDataManager.AttributeKey.USER_PHONE,
                value = rxPreferences.getUserPhoneNumber(),
                valueType = AttributeDataManager.ValueType.STR
            )
        )

        deviceRequest = DataAddRequest(name = cameraName, orgId = orgId, attributes = listAttributes, key = serialNumber, type = Define.TYPE_DEVICE.CAMERA_JF)
        val dataRequest = Gson().toJson(deviceRequest)
        Timber.d("deviceRequest: $dataRequest")
        val response = apiInterface.addDevice(dataRequest.toRequestBody())
        Timber.d("responseAddDevice: $response")
        if (response.attributes == null || response.attributes?.isEmpty() == true) {
            val listAttributeRes = response.attributes?.toMutableList() ?: mutableListOf()
            listAttributeRes.add(
                AttributeResponse(
                attributeKey = AttributeDataManager.AttributeKey.TYPE,
                value = Define.TYPE_DEVICE.CAMERA_JF,
                valueAsString = Define.TYPE_DEVICE.CAMERA_JF
            )
            )
            listAttributeRes.add(AttributeResponse(
                attributeKey = AttributeDataManager.AttributeKey.DEVICE_SERIAL,
                value = serialNumber,
                valueAsString = serialNumber
            ))
            listAttributeRes.add(AttributeResponse(
                attributeKey = AttributeDataManager.AttributeKey.MODEL_CAMERA,
                value = if(isInDoor) Define.CAMERA_MODEL.CAMERA_JF_INDOOR else Define.CAMERA_MODEL.CAMERA_JF_OUTDOOR,
                valueAsString = if(isInDoor) Define.CAMERA_MODEL.CAMERA_JF_INDOOR else Define.CAMERA_MODEL.CAMERA_JF_OUTDOOR,
            ))
            response.attributes = listAttributeRes
        }
        emit(Result.success(response))
    }.retry(2) { e ->
        e.message?.contains("409") == false
    }.catch { e -> emit(kotlin.Result.failure(e)) }.flowOn(Dispatchers.IO)
    suspend fun checkOwnerCameraJF(deviceSerial: String) : CheckOwnerResponse? {
        try {
//            emit(Result.Loading())
            val body = JSONObject().apply {
                put("device_serial", deviceSerial)
            }
            val requestBody = RequestBody.create(
                "application/json".toMediaTypeOrNull(),
                body.toString()
            )
            return apiInterface.checkOwnerCameraIPC(requestBody)
//            continuation.resume(response)
//            emit(Result.Success(response))
        } catch (e: Exception) {
//            emit(Result.Error(e.message ?: ""))
            Timber.e("error_checkOwnerCameraJF: " + e.message)
            return null
        }
    }
}