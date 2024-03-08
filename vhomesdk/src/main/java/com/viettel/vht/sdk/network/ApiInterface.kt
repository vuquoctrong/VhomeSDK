package com.viettel.vht.sdk.network

import com.vht.sdkcore.utils.Constants
import com.viettel.vht.main.model.ptz_jf.UpdateListPTZRequest
import com.viettel.vht.sdk.model.AccountUseIdResponse
import com.viettel.vht.sdk.model.CameraDataResponse
import com.viettel.vht.sdk.model.CheckOwnerResponse
import com.viettel.vht.sdk.model.DeviceDataResponse
import com.viettel.vht.sdk.model.DeviceResponse
import com.viettel.vht.sdk.model.ResponseCheckDeviceSpread
import com.viettel.vht.sdk.model.camera_cloud.BuyCloudVeRequest
import com.viettel.vht.sdk.model.camera_cloud.CloudReferCodeCloudResponse
import com.viettel.vht.sdk.model.camera_cloud.CloudStoragePackageResponse
import com.viettel.vht.sdk.model.camera_cloud.CloudStorageRegisteredResponse
import com.viettel.vht.sdk.model.camera_cloud.ListPaymentLinkResponse
import com.viettel.vht.sdk.model.camera_cloud.ListStatusCloudCameraResponse
import com.viettel.vht.sdk.model.camera_cloud.OTPOnOffAutoChargingCloudRequest
import com.viettel.vht.sdk.model.camera_cloud.OnOffAutoChargingCloud
import com.viettel.vht.sdk.model.camera_cloud.PaymentCodeResponse
import com.viettel.vht.sdk.model.camera_cloud.PaymentGatewayAutoResponse
import com.viettel.vht.sdk.model.camera_cloud.PaymentPayLinkRequest
import com.viettel.vht.sdk.model.camera_cloud.PaymentRequest
import com.viettel.vht.sdk.model.camera_cloud.PaymentResponse
import com.viettel.vht.sdk.model.camera_cloud.PricingCloudResponse
import com.viettel.vht.sdk.model.camera_cloud.RegisterPayLinkRequest
import com.viettel.vht.sdk.model.camera_cloud.RequestRegisterPromotionFree
import com.viettel.vht.sdk.model.camera_cloud.ResponseInformationRelatives
import com.viettel.vht.sdk.model.camera_cloud.URLPayLinkResponse
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

    @POST("/api/pricing/payment/precheck")
    suspend fun getInformationCloudRelatives(
        @Body requestBody: RequestBody
    ): ResponseInformationRelatives

    @POST("/api/pricing/payment/cloud/free")
    suspend fun setPricingPaymentCloud(@Body requestBody: RequestRegisterPromotionFree): PaymentCodeResponse

    @GET("/api/pricing/PaymentGateway/auto-payment/info")
    suspend fun getListPaymentGatewayLink(
        @Query("offset") offset: Int = 0,
        @Query("limit") limit: Int = 100
    ): ListPaymentLinkResponse


    @POST("/api/pricing/PaymentGateway/auto-payment/register")
    suspend fun registerPaymentGatewayLink(@Body requestBody: RegisterPayLinkRequest): URLPayLinkResponse

    @GET("/api/pricing/cloud/refer-code")
    suspend fun getDataCheckCloudReferCloud(@Query("code") code: String = ""): CloudReferCodeCloudResponse

    @POST("/api/pricing/PaymentGateway/auto-payment/otp")
    suspend fun getOTPOnOffAutoChargingCloud(@Body body: OTPOnOffAutoChargingCloudRequest): PaymentCodeResponse

    @GET("/api/pricing/payment/bill")
    suspend fun getListCloudStorageRegisteredAccount(
        @Query("limit") limit: Int = 100,
        @Query("offset") offset: Int = 0,
        @Query("order") order: Int = 0,
        @Query("user_id") userId: String = "",
    ): CameraDataResponse<CloudStorageRegisteredResponse>

    @GET("/api/pricing/payment/bill-phone")
    suspend fun getListStatusCloudCamera(): ListStatusCloudCameraResponse

    @GET("/api/users/v2")
    suspend fun getInformationAccountByUseId(
        @Query("user_id") userId: String = "",
    ): AccountUseIdResponse

    @GET("/api/pricing/payment/package")
    suspend fun getListCloudStoragePackage(
        @Query("vendor") attributes: String,
        @Query("method") method: Int = 0 /*0 - trả trước, 1 - trả sau*/
    ): CameraDataResponse<CloudStoragePackageResponse>

    @GET("/api/pricing/PaymentGateway/auto-charging/info")
    suspend fun getStatusAutoChargingCamera(
        @Query("limit") limit: Int = 10,
        @Query("offset") offset: Int = 0,
        @Query("serial") serial: String = "",
        @Query("status") status: String = "activate",
        @Query("method") method: String = "VTPAY-WALLET",//VTPAY or CTT

    ): PaymentGatewayAutoResponse


    @POST("/api/pricing/payment/package/v2")
    suspend fun buyCloudV2(@Body body: BuyCloudVeRequest): PaymentCodeResponse

    @POST("/api/pricing/payment/otp/do-payment")
    suspend fun paymentOTPPayLinkCloud(@Body body: PaymentPayLinkRequest): PaymentCodeResponse

    @POST("/api/pricing/PaymentGateway/auto-charging")
    suspend fun setOnOffAutoChargingCloud(@Body body: OnOffAutoChargingCloud): PaymentCodeResponse

    @POST("/api/devices/spread")
    suspend fun setDeviceSpread(@Body requestBody: RequestBody): ResponseCheckDeviceSpread

    @POST("/api/pricing/payment/camera")
    suspend fun paymentCamera(@Body body: PaymentRequest): PaymentCodeResponse
}

