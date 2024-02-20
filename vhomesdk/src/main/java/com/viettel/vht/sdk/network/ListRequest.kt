package com.viettel.vht.sdk.network

import android.app.Application
import android.content.Context
import com.vht.sdkcore.BuildConfig
import com.vht.sdkcore.file.AppLogFileManager
import com.vht.sdkcore.utils.Constants
import com.vht.sdkcore.utils.HTTPAppLogModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import okhttp3.Request
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ListRequest @Inject constructor(
    private val application: Application,
    @ApplicationContext val context: Context,
    private val appLogFileManager: AppLogFileManager
) {

    var listNetworkRequest: MutableList<NetworkRequest> = mutableListOf()
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var httpAppLogModel: HTTPAppLogModel? = null

    fun saveLogRequest(
        request: Request
    ) {
        applicationScope.launch {
            try {
                val screenId: String =
                    if (request.header(Constants.SCREEN_ID).toString().contains("null")) {
                        ""
                    } else {
                        request.header(Constants.SCREEN_ID).toString()
                    }
                val actionId: String =
                    if (request.header(Constants.ACTION_ID).toString().contains("null")) {
                        Constants.EMPTY
                    } else {
                        request.header(Constants.ACTION_ID).toString()
                    }
                val serialCamera: String =
                    if (request.header(Constants.SERIAL_CAMERA).toString().contains("null")) {
                        Constants.EMPTY
                    } else {
                        request.header(Constants.SERIAL_CAMERA).toString()
                    }
                val traceParent: String = request.header(Constants.TRACE_PARENT).toString()
                val isTraceParentNotNull: Boolean? =
                    request.header(Constants.TRACE_PARENT)?.isNotEmpty()
                if (isTraceParentNotNull == true) {
                    val currentTime = System.currentTimeMillis().toString()
                    listNetworkRequest.add(
                        NetworkRequest(
                            screenID = screenId,
                            actionID = actionId,
                            httpStartTime = currentTime,
                            traceParent = traceParent,
                            serialCamera = serialCamera,
                            httpMethod = request.method,
                            httpURL = request.url.toString()
                        )
                    )
                    httpAppLogModel = HTTPAppLogModel(
                        screenId = screenId,
                        timeStamp = currentTime,
                        actionId = actionId,
                        deviceId = serialCamera,
                        serverDomainIP = BuildConfig.BASE_URL,
                        httpMethod = request.method,
                        httpURL = request.url.toString().replace(BuildConfig.BASE_URL, ""),
                        httpVersion = Constants.LOG_DEFAULT,
                        httpResponseCode = Constants.NetworkRequestCode.REQUEST_CODE_200.toString(),
                        traceParent = traceParent
                    )
                    appLogFileManager.saveHTTPAppLogFile(httpAppLogModel ?: HTTPAppLogModel())
                }
            } catch (e: Exception) {
                val currentNetworkRequest = listNetworkRequest.find {
                    it.httpURL == request.url.toString()
                }
                listNetworkRequest.remove(currentNetworkRequest)
            }
        }
    }

    fun saveHTTPLogResponse(
        response: Response
    ) {
        applicationScope.launch {
            try {
                val traceID = response.header("traceid")
                if (traceID != null) {
                    val responseBodyBytes = response.body?.toString()?.toByteArray()
                    val contentLength = responseBodyBytes?.size ?: -1
                    val currentNetworkRequest = listNetworkRequest.find {
                        it.traceParent.contains(traceID)
                    }
                    if (currentNetworkRequest?.traceParent != Constants.EMPTY) {
                        httpAppLogModel = HTTPAppLogModel(
                            screenId = currentNetworkRequest?.screenID ?: Constants.LOG_DEFAULT,
                            actionId = currentNetworkRequest?.actionID ?: Constants.LOG_DEFAULT,
                            deviceId = currentNetworkRequest?.serialCamera ?: Constants.LOG_DEFAULT,
                            serverDomainIP = BuildConfig.BASE_URL,
                            httpMethod = currentNetworkRequest?.httpMethod ?: Constants.EMPTY,
                            httpURL = currentNetworkRequest?.httpURL?.replace(
                                BuildConfig.BASE_URL,
                                Constants.EMPTY
                            ) ?: Constants.EMPTY,
                            httpVersion = Constants.LOG_DEFAULT,
                            httpResponseCode = response.code.toString(),
                            httpResponseBodyLength = contentLength.toString(),
                            httpResponseTime = (System.currentTimeMillis() - currentNetworkRequest!!.httpStartTime.toLong()).toString(),
                            apiResponseCode = response.code.toString(),
                            traceParent = currentNetworkRequest.traceParent
                        )
                        appLogFileManager.saveHTTPAppLogFile(httpAppLogModel ?: HTTPAppLogModel())
                        listNetworkRequest.remove(currentNetworkRequest)
                    }
                }

            } catch (e: Exception) {

            }
        }
    }

}
