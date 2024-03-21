package com.viettel.vht.sdk.network

import android.content.Context
import com.google.gson.Gson
import com.utils.MacroUtils
import com.vht.sdkcore.pref.RxPreferences

import com.viettel.vht.sdk.model.login.LoginResponse

import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import timber.log.Timber
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
        val refreshUrl = URL("${MacroUtils.getValue(context,"SDK_VHOME_BASE_URL")}/api/vhome/refresh")
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
            val refreshTokenResult: LoginResponse?
            try {
                refreshTokenResult = gson.fromJson(response.toString(), LoginResponse::class.java)
            } catch (e: Exception) {
                e.printStackTrace()
                return false
            }

            if (refreshTokenResult?.code == 2019) {
                return false
            }
            if (refreshTokenResult != null) {
                Timber.tag("okhttp.OkHttpClient").e("refresh token success --- ${refreshTokenResult.token}")
                rxPreferences.setUserToken(refreshTokenResult.token)
                rxPreferences.setCameraAccessToken(refreshTokenResult.ezToken)
                rxPreferences.setRefreshToken(refreshTokenResult.refreshToken)
            }
            return true
        } else {
            return false
        }
    }

}