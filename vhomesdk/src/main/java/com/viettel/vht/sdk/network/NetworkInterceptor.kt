package com.viettel.vht.sdk.network

import android.content.Context
import com.google.gson.Gson
import com.vht.sdkcore.file.AppLogFileManager
import com.vht.sdkcore.network.NetworkException
import com.vht.sdkcore.pref.RxPreferences
import com.vht.sdkcore.utils.isNetworkAvailable
import com.viettel.vht.sdk.BuildConfig
import com.viettel.vht.sdk.model.login.LoginResponse
import com.viettel.vht.sdk.model.ApiException
import com.viettel.vht.sdk.utils.Config
import okhttp3.Interceptor
import okhttp3.Response
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.ConnectException
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.Charset
import javax.inject.Inject

class NetworkInterceptor @Inject constructor(
    private val networkEvent: NetworkEvent,
    private val context: Context,
    private val gson: Gson,
    private val rxPreferences: RxPreferences,
    private val appLogFileManager: AppLogFileManager,
    private val listRequest: ListRequest
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        if (!context.isNetworkAvailable()) {
            throw NetworkException()
        } else {
            try {
                listRequest.saveLogRequest(request)
                val response = chain.proceed(request)
                val responseBody = response.body
                listRequest.saveHTTPLogResponse(response)
                val source = responseBody?.source()
                source?.request(Long.MAX_VALUE)
                val buffer = source?.buffer
                val responseBodyString = buffer?.clone()?.readString(Charset.forName("UTF-8"))
                val errorResponse = gson.fromJson(responseBodyString, ApiException::class.java)
                when (response.code) {
                    in 200..299 -> {

                    }
                    400, 402, 403, 404, 405, 409 -> {
                        if (errorResponse.code == 2003) {
                            if (request.url.toString().endsWith("/login")) {
                                networkEvent.publish(NetworkState.GENERIC(errorResponse))
                            }
                        } else {
                            networkEvent.publish(NetworkState.GENERIC(errorResponse))
                        }
                    }
                    else -> {
                        when (errorResponse.code) {
                            2003 -> {}

                            else -> {
                                networkEvent.publish(NetworkState.GENERIC(errorResponse))
                            }
                        }
                    }

                }

                when (errorResponse.errorCode) {
                    800033 -> {
                        networkEvent.publish(NetworkState.GENERIC(errorResponse))
                    }
                }

                when (errorResponse.code) {
                    10001 -> {
                        if (!rxPreferences.getCameraAccessToken().isNullOrBlank()) {
                            if (refreshToken()) {
                                return chain.proceed(request)
                            } else {
                                networkEvent.publish(NetworkState.UNAUTHORIZED)
                            }
                        }
                    }
                }
                return response
            } catch (e: ConnectException) {
            } catch (e: Exception) {
                if ("Canceled" != e.message) {
                    networkEvent.publish(NetworkState.ERROR)
                }
            }
            return chain.proceed(request)
        }
    }

    private fun refreshToken(): Boolean {
        val refreshUrl = URL("${Config.sdkBASE_URL}/api/vhome/refresh")
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
                return false
            }

            if (refreshTokenResult?.code == 2019) {
                return false
            }

            if (refreshTokenResult != null) {
                rxPreferences.setUserToken(refreshTokenResult.token)
                rxPreferences.setCameraAccessToken(refreshTokenResult.ezToken)
                rxPreferences.setRefreshToken(refreshTokenResult.refreshToken)
            }
            return true
        } else
            return false
    }
}

data class NetworkRequest(
    val screenID: String,
    val actionID: String,
    val httpStartTime: String,
    val traceParent: String,
    val serialCamera: String,
    val httpMethod: String,
    var httpURL: String
)