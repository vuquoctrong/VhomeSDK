package com.viettel.vht.sdk.network

import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.POST


interface ApiInterface {
    @POST("/api/devices/remove/list")
    suspend fun deleteListDevice(@Body requestBody: RequestBody)
}

