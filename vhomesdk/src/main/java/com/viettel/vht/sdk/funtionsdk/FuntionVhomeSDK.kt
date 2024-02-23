package com.viettel.vht.sdk.funtionsdk

import android.content.Context
import android.content.Intent
import com.vht.sdkcore.pref.RxPreferences
import com.vht.sdkcore.utils.AppLog
import com.vht.sdkcore.utils.Constants
import com.vht.sdkcore.utils.Define
import com.viettel.vht.sdk.network.AuthApiInterface
import com.viettel.vht.sdk.network.NetworkEvent
import com.viettel.vht.sdk.network.NetworkState
import com.viettel.vht.sdk.ui.main.SDKVHomeMainActivity
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
    fun loginAccountVHome(phone: String?, password: String?, listener: VHomeSDKLoginListener?)

    fun openAddCameraJF(context: Context)

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
    private val  apiInterface: AuthApiInterface,
    private val networkEvent: NetworkEvent

) : VHomeSDKManager {

    private var errorCode: Int? = 0

    init {
        coroutineScope.launch {
            networkEvent.observableNetworkState
                .flowOn(Dispatchers.Main)
                .collectLatest { status ->

                    when (status) {
                        is NetworkState.UNAUTHORIZED -> {
                            Timber.d("Main_NetworkState.UNAUTHORIZED")

                            networkEvent.publish(NetworkState.INITIALIZE)
                        }

                        is NetworkState.GENERIC -> {
                            errorCode = status.exception.code
                            networkEvent.publish(NetworkState.INITIALIZE)
                        }

                        else -> {}
                    }
                }
        }
    }

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
                val response = authApiInterface.login(
                    jsonObject.toString().toRequestBody(),
                    AppLog.LogLogin.LOGIN.screenID,
                    AppLog.LogLogin.LOGIN.actionID
                )
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

    override fun openAddCameraJF(context: Context) {
        context.startActivity(Intent(context, SDKVHomeMainActivity::class.java))
    }

}