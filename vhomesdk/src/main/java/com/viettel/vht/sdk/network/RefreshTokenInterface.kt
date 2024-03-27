package com.viettel.vht.sdk.network

import com.viettel.vht.sdk.model.login.RefreshTokenResponse
import retrofit2.http.GET
import retrofit2.http.POST

interface RefreshTokenInterface {
    @GET("/api/vhome/refresh/partner")
    suspend fun refreshToken(
    ): RefreshTokenResponse
}