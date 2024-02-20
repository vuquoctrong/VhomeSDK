package com.viettel.vht.sdk.network

import com.vht.sdkcore.utils.Constants
import com.viettel.vht.sdk.model.login.LoginResponse
import okhttp3.RequestBody
import retrofit2.http.*

interface AuthApiInterface {

    @POST("/api/app/vhome/v4/login")
    suspend fun login(
        @Body requestBody: RequestBody,
        @Header(Constants.SCREEN_ID) screenID: String,
        @Header(Constants.ACTION_ID) action: String
    ): LoginResponse
}