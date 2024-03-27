package com.viettel.vht.sdk.funtionsdk

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.core.content.ContextCompat.startActivity
import com.vht.sdkcore.pref.RxPreferences
import com.vht.sdkcore.utils.AppLog
import com.vht.sdkcore.utils.Constants
import com.vht.sdkcore.utils.Define
import com.viettel.vht.sdk.model.DeviceDataResponse
import com.viettel.vht.sdk.network.ApiInterface
import com.viettel.vht.sdk.network.AuthApiInterface
import com.viettel.vht.sdk.network.NetworkEvent
import com.viettel.vht.sdk.network.NetworkState
import com.viettel.vht.sdk.network.RefreshTokenInterface
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
    fun openDetailCameraJF(context: Context,idCamera: String,serialCamera: String,nameCamera: String,modelCamera: String,listener: VHomeDetailCameraJFSDKListener?)

    /**
     *  Hiển thị logcat trong sdk VHome
     */
    fun setLogcat(boolean: Boolean)

    /**
     *  lấy đường dẫn hình ảnh camera khi vào xem liveView ( đường dẫn được lưu trong local app)
     */
    fun getPathImageScreenshotCameraJF(serialCamera: String): String

    /**
     *  lấy đường dẫn (Bitmap) hình ảnh camera khi vào xem liveView ( đường dẫn được lưu trong local app)
     */
    fun getBitmapImageScreenshotCameraJF(serialCamera: String): Bitmap?

    fun setRefreshTokenSDKVHome(token: String, listener: VHomeSDKRefreshTokenListener)

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

interface VHomeSDKRefreshTokenListener{
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
    private val apiRefreshTokenInterface: RefreshTokenInterface,
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
                        it.thirdPartyInfo?.let { dataInfor ->
                            rxPreferences.setTokenJFTech(dataInfor.jfAuth)
                            rxPreferences.setUserJF(dataInfor.jfUser)
                        }

                    }
                    listener?.onSuccess(rxPreferences.getUserToken() ?: "")
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    listener?.onFailed(errorCode ?: 0)
                }
                e.printStackTrace()
            }
        }

    }

    override fun setRefreshTokenSDKVHome(token: String, listener: VHomeSDKRefreshTokenListener) {
        coroutineScope.launch(Dispatchers.IO) {
            try {
                rxPreferences.setRefreshToken(token)
                DebugConfig.logd(message = "setRefreshTokenSDKVHome:start")
                val response = apiRefreshTokenInterface.refreshToken(
                )
                DebugConfig.logd(message = "setRefreshTokenSDKVHome: $response")
                withContext(Dispatchers.Main) {
                   if(response.code == -1){
                       response.let {
                           rxPreferences.setUserToken(it.token, it.token)
                           rxPreferences.setUserId(it.userId)
                           rxPreferences.setUserPhoneNumber(it.phone ?: Constants.EMPTY)
                           rxPreferences.setOrgIDAccount(it.orgId ?: Constants.EMPTY)
                           rxPreferences.setTokenJFTech(it.jfAuth)
                           rxPreferences.setUserJF(it.jfUser)

                       }
                       listener?.onSuccess(rxPreferences.getUserToken() ?: "")
                   }else{
                       listener?.onFailed(errorCode ?: 0)
                   }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    listener?.onFailed(errorCode ?: 0)
                }
                e.printStackTrace()
            }
        }
    }

    override fun openAddCameraJF(context: Context, listener: VHomeSDKAddCameraJFListener?) {
        coroutineScope.launch(Dispatchers.Main){
            vHomeSDKAddCameraJFListener = listener
            val intent = Intent(context, SDKVHomeMainActivity::class.java)
            intent.putExtra(Config.SDK_DATA_FUNCTION_VHOME, Config.SDK_FUNCTION_OPEN_ADD_CAMERA_JF)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }

    }


    override fun openDetailCameraJF(context: Context,idCamera: String,serialCamera: String,nameCamera: String,modelCamera: String,listener: VHomeDetailCameraJFSDKListener?) {
        coroutineScope.launch(Dispatchers.Main){
            vHomeDetailCameraJFSDKListener = listener
            val intent = Intent(context, SDKVHomeMainActivity::class.java)
            intent.putExtra(Config.SDK_DATA_FUNCTION_VHOME, Config.SDK_FUNCTION_OPEN_DETAIL_CAMERA_JF)
            intent.putExtra(Config.SDKParamIntent.PARAM_ID_CAMERA,idCamera)
            intent.putExtra(Config.SDKParamIntent.PARAM_SERIAL_CAMERA,serialCamera)
            intent.putExtra(Config.SDKParamIntent.PARAM_NAME_CAMERA,nameCamera)
            intent.putExtra(Config.SDKParamIntent.PARAM_MODEL_CAMERA,modelCamera)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }

    override fun setLogcat(boolean: Boolean) {
        DebugConfig.SHOW_DEBUG_LOG = boolean
        if(DebugConfig.SHOW_DEBUG_LOG){
            Timber.plant(Timber.DebugTree())
        }
    }

    override fun getPathImageScreenshotCameraJF(serialCamera: String): String {
      return ImageUtils.getScreenshotFile(serialCamera, context).absolutePath?:""
    }

    override fun getBitmapImageScreenshotCameraJF(serialCamera: String): Bitmap? {
        return ImageUtils.getScreenshotFile(serialCamera, context).absolutePath?.getBitmapFromFilePath()
    }
}