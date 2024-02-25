package com.viettel.vht.sdk.ui.jfcameradetail.forgot_password

import android.app.Application
import android.os.Message
import android.text.TextUtils
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.basic.G
import com.google.gson.Gson
import com.lib.EUIMSG
import com.lib.FunSDK
import com.lib.IFunSDKResult
import com.lib.MsgContent
import com.lib.sdk.bean.GetSafetyAbility
import com.lib.sdk.bean.HandleConfigData
import com.lib.sdk.bean.JsonConfig
import com.lib.sdk.bean.StringUtils
import com.manager.device.DeviceManager
import com.vht.sdkcore.base.BaseViewModel
import com.vht.sdkcore.network.Result.*
import com.vht.sdkcore.network.Status
import com.vht.sdkcore.pref.RxPreferences
import com.vht.sdkcore.utils.Define
import com.vht.sdkcore.utils.SingleLiveEvent
import com.viettel.vht.sdk.jfmanager.JFApiManager
import com.viettel.vht.sdk.network.repository.HomeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import timber.log.Timber
import java.net.URLDecoder
import java.util.regex.Pattern
import javax.inject.Inject

@HiltViewModel
class RetrievePasswordCameraJFViewModel @Inject constructor(
    application: Application,
    private val homeRepository: HomeRepository,
    private val rxPreferences: RxPreferences
) : BaseViewModel(), IFunSDKResult {

    lateinit var devId: String
    private val deviceManager: DeviceManager
    private var userId = 0
    private var newPassword = ""

    val isShowNewPassword = MutableLiveData(true)
    val isShowConfirmPassword = MutableLiveData(true)

    val requestOTPStatus = SingleLiveEvent<com.vht.sdkcore.network.Result<Any>>()
    val verifyOTPStatus = SingleLiveEvent<com.vht.sdkcore.network.Result<Any>>()
    val setNewPasswordStatus = SingleLiveEvent<com.vht.sdkcore.network.Result<Any>>()

    var retryCount = 5
    var isCountDownTimer = false

    init {
        userId = FunSDK.GetId(userId, this)
        deviceManager = DeviceManager.getInstance()
        JFApiManager.initJFApiManager(application.applicationContext)
    }

    override fun OnFunSDKResult(message: Message, msgContent: MsgContent): Int {
        Timber.tag(TAG).d("OnFunSDKResult")
        Timber.tag(TAG).d("message: ${message.what} - $message")
        Timber.tag(TAG).d("msgContent: ${Gson().toJson(msgContent)}")
        when (message.what) {
            EUIMSG.DEV_CONFIG_JSON_NOT_LOGIN -> {
                val json = G.ToString(msgContent.pData)
                if (StringUtils.contrast(JsonConfig.GET_SAFETY_ABILITY, msgContent.str)) {
                    if (message.arg1 >= 0) {
                        val handleConfigData: HandleConfigData<*> = HandleConfigData<Any?>()
                        if (handleConfigData.getDataObj(json, GetSafetyAbility::class.java)) {
                            val safetyAbility = handleConfigData.obj as GetSafetyAbility
                            return if (safetyAbility.verifyQRCode == 1) {
                                getVerifyQRCode()
                                0
                            } else {
                                // No contact is set. You need to set the contact before using the password retrieval function.
                                requestOTPStatus.value = Error(ERROR_NOT_SET_CONTACT)
                                0
                            }
                        }
                    }
                    requestOTPStatus.value = Error(message.arg1.toString())
                }
                if (StringUtils.contrast(JsonConfig.GET_VERIFY_QR_CODE, msgContent.str)) {
                    if (message.arg1 >= 0) {
                        JSONObject(json).optJSONObject(JsonConfig.GET_VERIFY_QR_CODE)
                            ?.optJSONObject("VerifyQRCode")
                            ?.optString("Text")?.let {
                                if (!TextUtils.isEmpty(it)) {
                                    postPassword(it)
                                    return 0
                                }
                            }
                    }
                    requestOTPStatus.value = Error(message.arg1.toString())
                }
                if (StringUtils.contrast(JsonConfig.CHECK_VERIFY_CODE, msgContent.str)) {
                    if (message.arg1 < 0) {
                        verifyOTPStatus.value = Error(message.arg1.toString())
                    } else {
                        verifyOTPStatus.value = Success("VerifyOTP success")
                    }
                    isLoading.value = false
                }
                if (StringUtils.contrast(JsonConfig.SET_NEW_PASSWORD, msgContent.str)) {
                    if (message.arg1 < 0) {
                        setNewPasswordStatus.value = com.vht.sdkcore.network.Result.Error(message.arg1.toString())
                    } else {
                        val username = FunSDK.DevGetLocalUserName(devId)
                        FunSDK.DevSetLocalPwd(
                            devId,
                            if (TextUtils.isEmpty(username)) "admin" else username,
                            newPassword
                        )
                        setNewPasswordStatus.value = com.vht.sdkcore.network.Result.Success(newPassword)
                    }
                    isLoading.value = false
                }
            }

            EUIMSG.DEV_GET_JSON -> {
                if (StringUtils.contrast(JSON_CONFIG_SECURITY_PHONE, msgContent.str)) {
                    if (message.arg1 >= 0) {
                        val jsonObject = JSONObject(G.ToString(msgContent.pData))
                        jsonObject.optJSONObject(JSON_CONFIG_SECURITY_PHONE)?.let {
                            val securityPhone = it.optString("SecurityPhone", "")
                            if (securityPhone != rxPreferences.getUserPhoneNumber()) {
                                it.put("SecurityPhone", rxPreferences.getUserPhoneNumber())
                                it.put("VerifyCodeRestorePwdType", 1)
                                setSecurityPhone(it.toString())
                            }
                        }
                    }
                }
            }

            EUIMSG.DEV_SET_JSON -> {
                if (StringUtils.contrast(JSON_CONFIG_SECURITY_PHONE, msgContent.str)) {
                    println()
                }
            }

            else -> Unit
        }
        return 0
    }

    override fun onCleared() {
        super.onCleared()
        FunSDK.UnRegUser(userId)
    }

    private fun getSafetyAbility() {
        FunSDK.DevConfigJsonNotLoginPtl(
            userId, devId, JsonConfig.GET_SAFETY_ABILITY, "",
            1650, -1, 0, Define.GET_SET_DEV_CONFIG_TIMEOUT, devId.hashCode(), 0
        )
        /*
        Sample data: { "GetSafetyAbility" : { "Question" : 2, "SetResetUser" : 1, "VerifyQRCode" : 2 }, "Name" : "GetSafetyAbility", "Ret" : 100 }
            "VerifyQRCode" : 2  No contact is set. You need to set the contact before using the password retrieval function.
            "VerifyQRCode" : 1  Supports password retrieval.
        */
    }

    private fun getSecurityPhone() {
        FunSDK.DevGetConfigByJson(
            userId, devId, JSON_CONFIG_SECURITY_PHONE,
            0, -1, Define.GET_SET_DEV_CONFIG_TIMEOUT, 0
        )
        /*
        Sample data:
            {
              "General.PwdSafety": {
                "PwdReset": [
                  {"QuestionAnswer": "", "QuestionIndex": 0},
                  {"QuestionAnswer": "", "QuestionIndex": 0},
                  {"QuestionAnswer": "", "QuestionIndex": 0},
                  {"QuestionAnswer": "", "QuestionIndex": 0}
                ],
                "SecurityEmail": "",
                "TipPageHide": false,
                "SecurityPhone": "18967146197",
                "VerifyCodeRestorePwdType": 1
              },
              "Name": "General.PwdSafety",
              "Ret": 100,
              "SessionID": "0x0000007a"
            }
         */
    }

    private fun setSecurityPhone(data: String) {
        FunSDK.DevSetConfigByJson(
            userId, devId, JSON_CONFIG_SECURITY_PHONE,
            data, -1, Define.GET_SET_DEV_CONFIG_TIMEOUT, 0
        )
    }

    @Suppress("DEPRECATION")
    private fun getVerifyQRCode() {
        FunSDK.DevConfigJsonNotLogin(
            userId, devId, JsonConfig.GET_VERIFY_QR_CODE,
            null, 1650, -1, 0, Define.GET_SET_DEV_CONFIG_TIMEOUT, 0
        )
        /*
        Sample data:
            {
              "GetVerifyQRCode": {
                "VerifyQRCode": {
                  "Text": "cZTKo1/gDcanRO6aKuOQjdde2vlZ9KcUFGtvEn56byFRVVFXVBmMthPZKn3EkqL+qYKOMA+AaEpnujfWmXkd7Q==",
                  "code": "0xD896747FEE38A83FE341760BDBAEDD6725DBAEB48375815EE0BEEBB6F555555F274A70074F8BDF00C6F08AAE97295A8E1E42F5DB955090D9FE6D8D0DF82A0274CD66FA8902713F7708EF4B453006757252100FAE7234D38D02545A06669544689B0105B767B7B13461FEDBC07006C58E27E26A0942822E27B4578167F29AD6F73D2CD4EA02017FA55FC6283F0E55D4A788856A72FF0A895D0F2E6BA7438D778F1AA0FA2877F8B8B80186EE25",
                  "size": 37
                }
              },
              "Name": "GetVerifyQRCode",
              "Ret": 100
            }
         */
    }

    private fun postPassword(textBody: String) {
        viewModelScope.launch(Dispatchers.IO) {
            homeRepository.postPassword(textBody).collect {
                when (it.status) {
                    Status.LOADING -> Unit

                    Status.ERROR -> {
                        withContext(Dispatchers.Main) {
                            requestOTPStatus.value = Error(it.exception.toString())
                            isLoading.value = false
                        }
                    }

                    Status.SUCCESS -> {
                        viewModelScope.launch(Dispatchers.IO) {
                            try {
                                val response = URLDecoder.decode(it.data ?: "", "UTF-8")
                                val data = Gson().fromJson(response, PostPasswordResp::class.java)
                                withContext(Dispatchers.Main) {
                                    if (data.msg == "SUCCESS") {
                                        requestOTPStatus.value = Success(data)
                                        retryCount = 5
                                    } else {
                                        requestOTPStatus.value = Error(data.msg)
                                    }
                                }
                            } catch (e: Exception) {
                                withContext(Dispatchers.Main) {
                                    requestOTPStatus.value = Error(e.toString())
                                }
                            }
                            withContext(Dispatchers.Main) {
                                isLoading.value = false
                            }
                        }
                    }
                }
            }
        }
    }

    data class PostPasswordResp(
        val msg: String,
        val code: Int,
    )

    fun getUserPhoneNumberMasked(): String {
        val phoneNumber = rxPreferences.getUserPhoneNumber()
        return phoneNumber.substring(0, phoneNumber.length - 7) +
                "****" +
                phoneNumber.substring(phoneNumber.length - 3)
    }

    fun registerRecoveryPhoneNumber() {
        getSecurityPhone()
    }

    fun requestOTP() {
        requestOTPStatus.value = com.vht.sdkcore.network.Result.Loading()
        isLoading.value = true
        getSafetyAbility()
    }

    fun retryRequestOTP() {
        requestOTPStatus.value = com.vht.sdkcore.network.Result.Loading()
        isLoading.value = true
        getVerifyQRCode()
    }

    fun verifyOTP(verifyCode: String) {
        val json = JSONObject()
        try {
            json.put("Name", JsonConfig.CHECK_VERIFY_CODE)
            json.put(JsonConfig.CHECK_VERIFY_CODE, JSONObject().apply {
                put("VerifyCode", verifyCode)
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
        retryCount--
        verifyOTPStatus.value = com.vht.sdkcore.network.Result.Loading()
        isLoading.value = true
        FunSDK.DevConfigJsonNotLoginPtl(
            userId, devId, JsonConfig.CHECK_VERIFY_CODE, json.toString(),
            1650, -1, 0, Define.GET_SET_DEV_CONFIG_TIMEOUT, 0, 0
        )
    }

    fun saveNewPassword(newPass: String, confirmPass: String) {
        isLoading.value = true

        if (newPass.isNotEmpty() || confirmPass.isNotEmpty()) {
            if (!isValidPassword(newPass, confirmPass)) {
                isLoading.value = false
                return
            }
        }
        newPassword = newPass

        val json = JSONObject()
        try {
            json.put("Name", JsonConfig.SET_NEW_PASSWORD)
            json.put(JsonConfig.SET_NEW_PASSWORD, JSONObject().apply {
                put("NewPassword", newPass)
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
        setNewPasswordStatus.value = com.vht.sdkcore.network.Result.Loading()
        FunSDK.DevConfigJsonNotLoginPtl(
            userId, devId, JsonConfig.SET_NEW_PASSWORD, json.toString(),
            1650, -1, 0, Define.GET_SET_DEV_CONFIG_TIMEOUT, 0, 0
        )
    }

    private fun isValidPassword(newPass: String, confirmPass: String): Boolean {
        if (!validatePassword(newPass)) {
            setNewPasswordStatus.value = com.vht.sdkcore.network.Result.Error(ERROR_NEW_PASSWORD)
            return false
        }
        if (confirmPass != newPass) {
            setNewPasswordStatus.value = com.vht.sdkcore.network.Result.Error(ERROR_CONFIRM_PASSWORD)
            return false
        }
        return true
    }

    private fun validatePassword(password: String) = Pattern.matches(
        "^(?=.*[@#\$%&+()/*:;!?~=^-])(?=.*[0-9])(?=.*[A-Z])(?=.*[a-z])[A-Za-z\\d@#\$%&+()/*:;!?~=^-]{8,16}\$",
        password
    )

    companion object {
        private val TAG = RetrievePasswordCameraJFViewModel::class.java.simpleName

        private const val JSON_CONFIG_SECURITY_PHONE = "General.PwdSafety"

        const val ERROR_NEW_PASSWORD = "ERROR_NEW_PASSWORD"
        const val ERROR_CONFIRM_PASSWORD = "ERROR_CONFIRM_PASSWORD"

        const val ERROR_NOT_SET_CONTACT = "ERROR_NOT_SET_CONTACT"
        const val ERROR_MAX_REQUEST_OTP = "ERROR_MAX_REQUEST_OTP"
    }
}
