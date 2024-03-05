package com.viettel.vht.sdk.funtionsdk

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import com.vht.sdkcore.pref.RxPreferences
import com.vht.sdkcore.utils.AppLog
import com.vht.sdkcore.utils.Constants
import com.vht.sdkcore.utils.Define
import com.viettel.vht.sdk.model.DeviceDataResponse
import com.viettel.vht.sdk.network.ApiInterface
import com.viettel.vht.sdk.network.AuthApiInterface
import com.viettel.vht.sdk.network.NetworkEvent
import com.viettel.vht.sdk.network.NetworkState
import com.viettel.vht.sdk.ui.main.SDKVHomeMainActivity
import com.viettel.vht.sdk.utils.Config
import com.viettel.vht.sdk.utils.DebugConfig
import com.viettel.vht.sdk.utils.ImageUtils
import com.viettel.vht.sdk.utils.ImageUtils.getBitmapFromFilePath
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import timber.log.Timber
import javax.inject.Singleton

interface VHomeSDKManager {

    var vHomeSDKAddCameraJFListener: VHomeSDKAddCameraJFListener?

    var vHomeDetailCameraJFSDKListener: VHomeDetailCameraJFSDKListener?

    fun loginAccountVHome(phone: String?, password: String?, listener: VHomeSDKLoginListener?)

    /**
     *  Mở màn hình  add camera JF
     */
    fun openAddCameraJF(context: Context, listener: VHomeSDKAddCameraJFListener?)

    /**
     *  Mở màn hình LiveView camera JF
     */
    fun openDetailCameraJF(context: Context,listener: VHomeDetailCameraJFSDKListener?)

    /**
     *  Hiển thị logcat trong sdk VHome
     */
    fun setLogcat(boolean: Boolean)

    /**
     *   cài đặt Base URL cho sdk VHome
     */
    fun setUrlSDK(sdkAppName: String?,sdkAppKey: String?,sdkAppSecret: String?, sdkBaseUrl: String?, sdkBaseURlCameraJF: String?)

    /**
     *  lấy đường dẫn hình ảnh camera khi vào xem liveView ( đường dẫn được lưu trong local app)
     */
    fun getPathImageScreenshotCameraJF(serialCamera: String): String

    /**
     *  lấy đường dẫn (Bitmap) hình ảnh camera khi vào xem liveView ( đường dẫn được lưu trong local app)
     */
    fun getBitmapImageScreenshotCameraJF(serialCamera: String): Bitmap?

}

interface VHomeSDKAddCameraJFListener {
    fun onSuccess(data: DeviceDataResponse)

    fun onFailed(messageError: String)
}

interface VHomeDetailCameraJFSDKListener {
    fun onDeleteCameraJF(statusDelete: Boolean)
}

interface VHomeSDKLoginListener {
    fun onSuccess(token: String)

    fun onFailed(var1: Int)
}

@Singleton
class VHomeSDKManagerImpl constructor(
    private val context: Context,
    private val coroutineScope: CoroutineScope,
    private val rxPreferences: RxPreferences,
    private val authApiInterface: AuthApiInterface,
    private val apiInterface: ApiInterface,
    private val networkEvent: NetworkEvent,

    ) : VHomeSDKManager {

    private var errorCode: Int? = 0

    init {
        coroutineScope.launch {
            networkEvent.observableNetworkState
                .flowOn(Dispatchers.Main)
                .collectLatest { status ->
                    when (status) {
                        is NetworkState.UNAUTHORIZED -> {
                            DebugConfig.logd(message = "Main_NetworkState.UNAUTHORIZED")

                            networkEvent.publish(NetworkState.INITIALIZE)
                        }

                        is NetworkState.GENERIC -> {
                            errorCode = status.exception.code
                            networkEvent.publish(NetworkState.INITIALIZE)
                            DebugConfig.logd(message = "networkEvent: ${errorCode}")
                        }

                        else -> {}
                    }
                }
        }
    }

    override var vHomeSDKAddCameraJFListener: VHomeSDKAddCameraJFListener? = null

    override var vHomeDetailCameraJFSDKListener: VHomeDetailCameraJFSDKListener? = null

    override fun loginAccountVHome(
        phone: String?,
        password: String?,
        listener: VHomeSDKLoginListener?
    ) {
        val jsonObject = JSONObject().apply {
            put(Define.BUNDLE_KEY.PARAM_IDENTIFIER, phone)
            put(Define.BUNDLE_KEY.PARAM_PASSWORD, password)
        }
        coroutineScope.launch(Dispatchers.IO) {
            try {
                DebugConfig.logd(message = "loginAccountVHome:start")
                val response = authApiInterface.login(
                    jsonObject.toString().toRequestBody(),
                    AppLog.LogLogin.LOGIN.screenID,
                    AppLog.LogLogin.LOGIN.actionID
                )
                DebugConfig.logd(message = "loginAccountVHome: $response")
                withContext(Dispatchers.Main) {
                    response.let {
                        rxPreferences.setUserToken(it.token, it.deviceToken)
                        rxPreferences.setCameraAccessToken(it.ezToken)
                        rxPreferences.setRefreshToken(it.refreshToken)
                        rxPreferences.setUserId(it.userId)
                        rxPreferences.setUserPhoneNumber(it.phone ?: Constants.EMPTY)
                        rxPreferences.setOrgIDAccount(it.orgId ?: Constants.EMPTY)
                        rxPreferences.setUserName(it.name ?: Constants.EMPTY)

                    }
                    listener?.onSuccess(rxPreferences.getUserToken() ?: "")
                }
            } catch (e: Exception) {
                listener?.onFailed(errorCode ?: 0)
                e.printStackTrace()
            }
        }

    }

    override fun openAddCameraJF(context: Context, listener: VHomeSDKAddCameraJFListener?) {
        vHomeSDKAddCameraJFListener = listener
        val intent = Intent(this.context, SDKVHomeMainActivity::class.java)
        intent.putExtra(Config.SDK_DATA_FUNCTION_VHOME, Config.SDK_FUNCTION_OPEN_ADD_CAMERA_JF)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        this.context.startActivity(intent)
    }


    override fun openDetailCameraJF(context: Context,listener: VHomeDetailCameraJFSDKListener?) {
        vHomeDetailCameraJFSDKListener = listener
        val intent = Intent(this.context, SDKVHomeMainActivity::class.java)
        intent.putExtra(Config.SDK_DATA_FUNCTION_VHOME, Config.SDK_FUNCTION_OPEN_DETAIL_CAMERA_JF)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        this.context.startActivity(intent)
    }

    override fun setLogcat(boolean: Boolean) {
        DebugConfig.SHOW_DEBUG_LOG = boolean
        if(DebugConfig.SHOW_DEBUG_LOG){
            Timber.plant(Timber.DebugTree())
        }
    }

    override fun setUrlSDK(
        sdkAppName: String?,
        sdkAppKey: String?,
        sdkAppSecret: String?,
        sdkBaseUrl: String?,
        sdkBaseURlCameraJF: String?
    ) {
        sdkAppName?.let {
            Config.sdkAppName
        }
        sdkAppKey?.let {
            Config.sdkAPP_KEY = it
        }
        sdkAppSecret?.let {
            Config.sdkAPP_SECRET = it
        }
        sdkBaseUrl?.let {
            Config.sdkBASE_URL = it
        }
        sdkBaseURlCameraJF?.let {
            Config.sdkBASE_URL_CAMERA_JF = it
        }
    }

    override fun getPathImageScreenshotCameraJF(serialCamera: String): String {
      return ImageUtils.getScreenshotFile(serialCamera, context).absolutePath?:""
    }

    override fun getBitmapImageScreenshotCameraJF(serialCamera: String): Bitmap? {
        return ImageUtils.getScreenshotFile(serialCamera, context).absolutePath?.getBitmapFromFilePath()
    }
}