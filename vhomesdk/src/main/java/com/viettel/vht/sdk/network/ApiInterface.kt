package com.viettel.vht.sdk.network

import com.viettel.vht.sdk.model.CheckOwnerResponse
import com.viettel.vht.sdk.model.DeviceDataResponse
import com.viettel.vht.sdk.model.home.GetAppLogUpLoadLinkResponse
import com.viettel.vht.sdk.model.home.RequestGetAppLogUpLoadLink
import com.viettel.vht.sdk.model.home.RequestUpLoadAppLog
import com.viettel.vht.sdk.model.home.UpLoadAppLogResponse
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.POST


interface ApiInterface {
    @POST("/api/devices/remove/list")
    suspend fun deleteListDevice(@Body requestBody: RequestBody)

    @POST("/api/vhome/smart-home/check-bind")
    suspend fun checkOwnerCameraIPC(@Body requestBody: RequestBody): CheckOwnerResponse

    @POST("/api/devices")
    suspend fun addDevice(@Body requestBody: RequestBody): DeviceDataResponse

    @POST("/api/vhome/camera/forward")
    suspend fun getAppLogUpLoadLink(@Body request: RequestGetAppLogUpLoadLink): GetAppLogUpLoadLinkResponse

    @POST("/api/vhome/camera/forward")
    suspend fun upLoadAppLogStatus(@Body request: RequestUpLoadAppLog): UpLoadAppLogResponse

}

