package com.viettel.vht.sdk.network.repository

import com.viettel.vht.sdk.network.AuthApiInterface
import com.vht.sdkcore.pref.RxPreferences
import com.vht.sdkcore.utils.AppLog
import com.vht.sdkcore.utils.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import com.viettel.vht.sdk.model.login.LoginResponse
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

class LoginRepository @Inject constructor(
    private val rxPreferences: RxPreferences,
    private val authApiInterface: AuthApiInterface,
) {

    fun login(bodyJson: String): Flow<com.vht.sdkcore.network.Result<LoginResponse>> = flow {
        try {
            emit(com.vht.sdkcore.network.Result.Loading())
            val response = authApiInterface.login(
                bodyJson.toRequestBody(),
                AppLog.LogLogin.LOGIN.screenID,
                AppLog.LogLogin.LOGIN.actionID
            )
            response.let {
                rxPreferences.setUserToken(it.token, it.deviceToken)
                rxPreferences.setCameraAccessToken(it.ezToken)
                rxPreferences.setRefreshToken(it.refreshToken)
                rxPreferences.setUserId(it.userId)
                rxPreferences.setUserPhoneNumber(it.phone ?: Constants.EMPTY)
                rxPreferences.setAuthorizationProtocol("")
                rxPreferences.setOrgIDAccount(it.orgId ?: Constants.EMPTY)
                rxPreferences.setIsAcceptPrivacyPolicy(it.acceptEula?:false)
                rxPreferences.setUserName(it.name ?: Constants.EMPTY)

                emit(com.vht.sdkcore.network.Result.Success(it))
            }
        } catch (e: Exception) {
            emit(com.vht.sdkcore.network.Result.Error(e.toString()))
            e.printStackTrace()
        }
    }.flowOn(Dispatchers.IO)


}