package com.viettel.vht.sdk.network

import com.vht.sdkcore.utils.Constants
import com.viettel.vht.main.model.ptz_jf.UpdateListPTZRequest
import com.viettel.vht.sdk.model.CameraDataResponse
import com.viettel.vht.sdk.model.CheckOwnerResponse
import com.viettel.vht.sdk.model.DeviceDataResponse
import com.viettel.vht.sdk.model.DeviceResponse
import com.viettel.vht.sdk.model.ResponseCheckDeviceSpread
import com.viettel.vht.sdk.model.camera_cloud.CloudStorageRegisteredResponse
import com.viettel.vht.sdk.model.camera_cloud.PricingCloudResponse
import com.viettel.vht.sdk.model.home.FeatureConfigResponse
import com.viettel.vht.sdk.model.home.GetAppLogUpLoadLinkResponse
import com.viettel.vht.sdk.model.home.RequestGetAppLogUpLoadLink
import com.viettel.vht.sdk.model.home.RequestUpLoadAppLog
import com.viettel.vht.sdk.model.home.UpLoadAppLogResponse
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query


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

    @GET("/api/pricing/payment/bill")
    suspend fun getListCloudStorageRegistered(
        @Query("start") startTime: Long = 1L,
        @Query("end") endTime: Long = System.currentTimeMillis(),
        @Query("serial") serial: String,
        @Query("limit") limit: Int = 100,
        @Query("offset") offset: Int = 0,
        @Query("order") order: Int = 0,
        @Query("skip_expired") skipExpired: Boolean = false,
    ): CameraDataResponse<CloudStorageRegisteredResponse>


    @POST("/api/pricing/payment/cloud/list")
    suspend fun getListPricingPaymentCloud(): PricingCloudResponse

    @GET("/api/devices/vhome")
    suspend fun getDevicesByOrg(
        @Query("org_id") orgId: String,
        @Query("expand") expand: Boolean = true
    ): DeviceResponse

    @PUT("/api/devices/ptz-parameters/list/update")
    suspend fun updateListPTZ(@Body request: UpdateListPTZRequest): Response<Any>

    @PUT("/api/vhome/camera/device/update/{deviceID}")
    suspend fun editNameDevice(
        @Path("deviceID") deviceId: String,
        @Body requestBody: RequestBody,
        @Header(Constants.SCREEN_ID) screenID: String,
        @Header(Constants.ACTION_ID) action: String,
        @Header(Constants.CAMERA_SERIAL) cameraSerial: String,
    ): Response<Unit>

    @DELETE("/api/devices/{deviceID}")
    suspend fun deleteDevice(@Path("deviceID") deviceId: String): Response<Unit>

    @GET("/api/users/mobile-app/config/list")
    suspend fun getConfigFeatureList(): FeatureConfigResponse

    @POST("/api/devices/spread/check")
    suspend fun deviceSpreadCheck(@Body requestBody: RequestBody): ResponseCheckDeviceSpread
}

