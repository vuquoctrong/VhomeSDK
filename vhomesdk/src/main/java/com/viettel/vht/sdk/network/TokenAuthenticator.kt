package com.viettel.vht.sdk.network

import android.content.Context
import com.google.gson.Gson
import com.utils.MacroUtils
import com.vht.sdkcore.pref.RxPreferences
import com.viettel.vht.sdk.model.login.RefreshTokenResponse
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject

class TokenAuthenticator @Inject constructor(
    private val rxPreferences: RxPreferences,
    @ApplicationContext private val context: Context,
    private val gson: Gson,
    private val networkEvent: NetworkEvent
) : Authenticator {

    private var isLoadingRefreshToken = true

    override fun authenticate(route: Route?, response: Response): Request? {
        if(isLoadingRefreshToken){
            isLoadingRefreshToken = false
            if (!refreshToken(context = context)) {
                networkEvent.publish(NetworkState.UNAUTHORIZED)
            }
            isLoadingRefreshToken = true
        }
        return response.request.newBuilder()
            .header("Authorization", rxPreferences.getUserToken() ?: "")
            .build()
    }

    private fun refreshToken(context: Context): Boolean {
        val refreshUrl = URL("${MacroUtils.getValue(context,"SDK_VHOME_BASE_URL")}/api/vhome/refresh/partner")
        val urlConnection = refreshUrl.openConnection() as HttpURLConnection
        urlConnection.apply {
            doInput = true
            setRequestProperty("RefreshToken", rxPreferences.getRefreshToken())
            requestMethod = "GET"
            useCaches = false
        }
        val responseCode = urlConnection.responseCode
        if (responseCode == 200) {
            val input = BufferedReader(InputStreamReader(urlConnection.inputStream))
            val response = StringBuffer()
            while (true) {
                val inputLine: String = input.readLine() ?: break
                response.append(inputLine)
            }
            input.close()
            val refreshTokenResult: RefreshTokenResponse?
            try {
                refreshTokenResult = gson.fromJson(response.toString(), RefreshTokenResponse::class.java)
            } catch (e: Exception) {
                return false
            }

            if (refreshTokenResult?.code != -1) {
                return false
            }
            refreshTokenResult?.let {
                rxPreferences.setUserToken(it.token)
                rxPreferences.setTokenJFTech(it.jfAuth)
                rxPreferences.setUserJF(it.jfUser)
                rxPreferences.setUserPhoneNumber(it.phone)
                rxPreferences.setUserId(it.userId)
                rxPreferences.setOrgIDAccount(it.orgId)
            }
            return true
        } else
            return false
    }

}