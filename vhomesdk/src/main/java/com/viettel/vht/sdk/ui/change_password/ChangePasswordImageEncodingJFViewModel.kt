package com.viettel.vht.sdk.ui.change_password

import android.os.Message
import android.text.TextUtils
import com.google.gson.Gson
import com.lib.EUIMSG
import com.lib.FunSDK
import com.lib.IFunSDKResult
import com.lib.MsgContent
import com.lib.sdk.bean.JsonConfig
import com.lib.sdk.bean.StringUtils
import com.manager.device.DeviceManager
import com.vht.sdkcore.base.BaseViewModel
import com.vht.sdkcore.utils.Define
import com.vht.sdkcore.utils.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import org.json.JSONObject
import timber.log.Timber
import java.util.regex.Pattern
import javax.inject.Inject

@HiltViewModel
class ChangePasswordImageEncodingJFViewModel @Inject constructor() : BaseViewModel(),
    IFunSDKResult {

    lateinit var devId: String
    private val deviceManager: DeviceManager
    private var userId = 0
    private var newPassword = ""

    val state = SingleLiveEvent<String>()
    val error = SingleLiveEvent<String>()

    init {
        userId = FunSDK.GetId(userId, this)
        deviceManager = DeviceManager.getInstance()
    }

    override fun OnFunSDKResult(message: Message, msgContent: MsgContent): Int {
        Timber.tag(TAG).d("OnFunSDKResult")
        Timber.tag(TAG).d("message: ${message.what} - $message")
        Timber.tag(TAG).d("msgContent: ${Gson().toJson(msgContent)}")
        when (message.what) {
            EUIMSG.DEV_SET_JSON -> {
                if (StringUtils.contrast(JsonConfig.MODIFY_PASSWORD, msgContent.str)) {
                    if (message.arg1 < 0) {
                        state.value = ERROR_UNKNOWN
                    } else {
                        val username = FunSDK.DevGetLocalUserName(devId)
                        FunSDK.DevSetLocalPwd(
                            devId,
                            if (TextUtils.isEmpty(username)) "admin" else username,
                            newPassword
                        )
                        state.value = SUCCESS
                    }
                }
                isLoading.value = false
            }

            else -> {
            }
        }
        return 0
    }

    override fun onCleared() {
        super.onCleared()
        FunSDK.UnRegUser(userId)
    }

    fun saveNewPassword(oldPass: String, newPass: String, confirmPass: String) {
        if (!this::devId.isInitialized) return
        isLoading.value = true

        if (!isValidPassword(oldPass, newPass, confirmPass)) {
            isLoading.value = false
            return
        }

        newPassword = newPass
        val userName = FunSDK.DevGetLocalUserName(devId)
        val json = JSONObject()
        try {
            json.put("EncryptType", "MD5")
            json.put("NewPassWord", FunSDK.DevMD5Encrypt(newPass))
            json.put("PassWord", FunSDK.DevMD5Encrypt(oldPass))
            json.put("SessionID", "0x6E472E78")
            json.put("UserName", if (TextUtils.isEmpty(userName)) "admin" else userName)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        FunSDK.DevSetConfigByJson(
            userId, devId,
            JsonConfig.MODIFY_PASSWORD, json.toString(),
            -1, Define.GET_SET_DEV_CONFIG_TIMEOUT, 0
        )
    }

    private fun isValidPassword(oldPass: String, newPass: String, confirmPass: String): Boolean {
        if (oldPass != FunSDK.DevGetLocalPwd(devId)) {
            state.value = ERROR_OLD_PASSWORD
            error.value = "Mật khẩu cũ không chính xác"
            return false
        }
        if (oldPass == newPass) {
            state.value = ERROR_NEW_PASSWORD
            error.value = "Mật khẩu mới không được trùng với mật khẩu cũ"
            return false
        }
        if (!validatePassword(newPass)) {
            state.value = ERROR_NEW_PASSWORD
            error.value =
                "Mật khẩu phải có độ dài 8-16 ký tự, bao gồm ít nhất 1 chữ hoa, 1 chữ thường, 1 chữ số và 1 ký tự đặc biệt"
            return false
        }
        if (confirmPass != newPass) {
            state.value = ERROR_CONFIRM_PASSWORD
            error.value = "Mật khẩu nhập lại phải khớp với mật khẩu"
            return false
        }
        return true
    }

    private fun validatePassword(password: String) = Pattern.matches(
        "^(?=.*[@#\$%&+()/*:;!?~=^-])(?=.*[0-9])(?=.*[A-Z])(?=.*[a-z])[A-Za-z\\d@#\$%&+()/*:;!?~=^-]{8,16}\$",
        password
    )

    companion object {
        private const val TAG = "ChangePasswordImageEncodingJFViewModel"

        const val ERROR_OLD_PASSWORD = "ERROR_OLD_PASSWORD"
        const val ERROR_NEW_PASSWORD = "ERROR_NEW_PASSWORD"
        const val ERROR_CONFIRM_PASSWORD = "ERROR_CONFIRM_PASSWORD"
        const val ERROR_UNKNOWN = "ERROR_UNKNOWN"
        const val SUCCESS = "SUCCESS"
    }
}
