package com.vht.sdkcore.pref

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.vht.sdkcore.camera.DefinitionJF
import com.vht.sdkcore.utils.Constants
import com.vht.sdkcore.utils.Utils
import dagger.hilt.android.qualifiers.ApplicationContext
import org.json.JSONObject
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class AppPreferences @Inject constructor(
    @ApplicationContext context: Context
) : RxPreferences {
  private  fun getSharePreferences(context: Context): SharedPreferences {
            return EncryptedSharedPreferences.create(
                Constants.PREF_FILE_NAME,
                MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
                context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )

    }

    companion object {
        const val PARAM_BEARER = "Bearer "
        const val PARAM_SIGN = "Sign "
        const val PREF_PARAM_USER_INFO = "PREF_PARAM_USER_INFO"
        const val PREF_PARAM_APP_ID = "PREF_PARAM_APP_ID"
        const val PREF_PARAM_CAMERA_ACCESS_TOKEN = "PREF_PARAM_CAMERA_ACCESS_TOKEN"
        const val PREF_PARAM_REFRESH_TOKEN = "PREF_PARAM_REFRESH_TOKEN"
        const val PREF_PARAM_API_KEY = "PREF_PARAM_API_KEY"
        const val PREF_PARAM_NAME = "PREF_PARAM_NAME"
        const val PREF_PARAM_EMAIL = "PREF_PARAM_EMAIL"
        const val PREF_SHOW_TYPE_LIST_DEVICE = "PREF_SHOW_TYPE_LIST_DEVICE"
        const val PREF_CURRENT_FAMILY_HOME = "PREF_CURRENT_FAMILY_HOME"
        const val PREF_SKIP_LOGIN = "PREF_SKIP_LOGIN"
        const val PREF_PARAM_PASSWORD = "PREF_PARAM_PASSWORD"
        const val PREF_PARAM_REMEMBER_EMAIL = "PREF_PARAM_REMEMBER_EMAIL"
        const val PREF_PARAM_DEVICE_TOKEN = "PREF_PARAM_DEVICE_TOKEN"
        const val PREF_PARAM_IDENTIFY_ID = "PREF_PARAM_IDENTIFY_ID"
        const val PREF_CURRENT_LIST_DEVICE = "PREF_CURRENT_LIST_DEVICE"
        const val PREF_TOPICS_FCM = "PREF_TOPICS_FCM"
        const val PREF_IP_GETWAY = "PREF_IP_GETWAY"
        const val PREF_PARAM_REMEMBER_PASSWORD_WIFI = "PREF_PARAM_REMEMBER_PASSWORD_WIFI"
        const val PREF_FIRST_GUI_HOME = "PREF_FIRST_GUI_HOME"
        const val PREF_FIRST_GUI_SCENCE = "PREF_FIRST_GUI_SCENCE"
        const val PREF_FIRST_GUI_AI = "PREF_FIRST_GUI_AI"
        const val PREF_GROUP_DEVICE = "PREF_GROUP_DEVICE"
        const val PREF_POPUP_CARD_ERROR = "PREF_POPUP_CARD_ERROR"
        const val PREF_LAYOUT_LIST_DEVICE = "PREF_LAYOUT_LIST_DEVICE"
        const val PREF_ORG_ID = "PREF_ORG_ID"
        const val PREF_DEBUG_MODE = "PREF_DEBUG_MODE"
        const val USER_SECRET_KEY = "USER_SECRET_KEY"
        const val USER_ID = "USER_ID"
        const val USER_PHONE_NUMBER = "USER_PHONE_NUMBER"
        const val PREF_USER_JF = "PREF_USER_JF"
        const val NONCE_USER_KEY = "NONCE_USER_KEY"
        const val AUT_CAMERA_VTT_PROTOCOL = "AUT_CAMERA_VTT_PROTOCOL"
        const val TIME_REFRESH_AUT_CAMERA_VTT_PROTOCOL = "TIME_REFRESH_AUT_CAMERA_VTT_PROTOCOL"
        const val KEY_SHARE_PREF_IMEI = "KEY_SHARE_PREF_IMEI"
        const val PREF_INSTRUCTIONS = "PREF_INSTRUCTIONS"
        const val STATUS_INFO_VERSION_UPDATE = "STATUS_INFO_VERSION_UPDATE"
        const val LIST_VERSION_INFO_DISABLE_UPDATE = "LIST_VERSION_INFO_DISABLE_UPDATE"
        const val PREF_TOKEN_JF_TECH = "PREF_TOKEN_JFT_ECH"
        const val PREF_PUSH_TOKEN_JF_TECH = "PREF_PUSH_TOKEN_JF_TECH"
        const val PREF_IMOU_TOKEN = "PREF_IMOU_TOKEN"
        const val PREF_IMOU_ADMIN_TOKEN = "PREF_IMOU_ADMIN_TOKEN"
        const val PREF_IMOU_OPEN_ID = "PREF_IMOU_OPEN_ID"
        const val PREF_IS_SHOWING_POPUP_LOCAL_MODE = "PREF_IS_SHOWING_POPUP_LOCAL_MODE"
        const val PREF_DEFINITION_JF = "PREF_DEFINITION_JF"
        const val PREF_ORG_ID_ACCOUT = "PREF_ORG_ID_ACCOUT"
        const val PREF_LINK_URL_USER_MANUAL = "PREF_LINK_URL_USER_MANUAL"
        const val KEY_EZVIZ_CAMERA_VERIFY_CODE = "KEY_EZVIZ_CAMERA_VERIFY_CODE"
        const val PREF_ENABLE_SHOW_PRIVATE_POLICY = "PREF_ENABLE_SHOW_PRIVATE_POLICY"
        const val PREF_ENABLE_SHOW_FREE_CLOUD = "PREF_ENABLE_SHOW_FREE_CLOUD"
        const val PREF_ENABLE_SHOW_SYNC_EZVIZ = "PREF_ENABLE_SHOW_SYNC_EZVIZ"
        const val PREF_ENABLE_NOT_SHOW_ONE_DAY_FREE_CLOUD =
            "PREF_ENABLE_NOT_SHOW_ONE_DAY_FREE_CLOUD"
        const val NOT_REQUEST_OVERLAY_AGAIN = "NOT_REQUEST_OVERLAY_AGAIN"
        const val NOT_REQUEST_DO_NOT_DISTURB_AGAIN = "NOT_REQUEST_DO_NOT_DISTURB_AGAIN"
        const val FIREBASE_TOKEN = "FIREBASE_TOKEN"
        const val IMAGE_PRESET = "IMAGE_PRESET"
        const val PREF_HOME_SELECTED = "PREF_HOME_SELECTED"
        const val PREF_CAMERA_SELECTED = "PREF_CAMERA_SELECTED"
        const val PREF_IS_SHOW_DIALOG_SPREAD = "PREF_IS_SHOW_DIALOG_SPREAD"

        const val PREF_LIST_SEARCH_RECENT = "PREF_LIST_SEARCH_RECENT"
        const val PREF_TYPE_MANAGER = "PREF_TYPE_MANAGER"
        const val PREF_DEFAULT_HOME = "PREF_DEFAULT_HOME"
        const val PREF_DEFAULT_GET_BIOMETRIC_AUTHENTICATION =
            "PREF_DEFAULT_GET_BIOMETRIC_AUTHENTICATION"
        const val PREF_USER_NAME =
            "PREF_USER_NAME"
        const val PREF_USER_PASSWORD =
            "PREF_USER_PASSWORD"

        const val PREF_RECOMMEND_UPDATE_FIRMWARE_JF = "PREF_RECOMMEND_UPDATE_FIRMWARE_JF"
    }


    private val mPrefs: SharedPreferences = getSharePreferences(context)

    override fun put(key: String, value: String) {
        val editor: SharedPreferences.Editor = mPrefs.edit()
        editor.putString(key, value)
        editor.apply()
    }

    override fun get(key: String): String? {
        return mPrefs.getString(key, "")
    }

    override fun putBoolean(key: String, value: Boolean) {
        val editor: SharedPreferences.Editor = mPrefs.edit()
        editor.putBoolean(key, value)
        editor.apply()
    }

    override fun getBoolean(key: String): Boolean {
        return mPrefs.getBoolean(key, false)
    }

    override fun clear() {
        val editor: SharedPreferences.Editor = mPrefs.edit()
        editor.clear()
        editor.apply()
    }

    override fun remove(key: String) {
        val editor: SharedPreferences.Editor = mPrefs.edit()
        editor.remove(key)
        editor.apply()
    }

    override fun getUserToken() = get(PREF_PARAM_USER_INFO)

    override fun setUserToken(userToken: String, deviceToken: String) {
        put(PREF_PARAM_USER_INFO, PARAM_BEARER + userToken)
        if (deviceToken.isNotEmpty()) {
            put(PREF_PARAM_DEVICE_TOKEN, deviceToken)
        }
    }

    override fun logout() {
        remove(PREF_PARAM_USER_INFO)
        remove(PREF_PARAM_REFRESH_TOKEN)
        remove(PREF_SHOW_TYPE_LIST_DEVICE)
        remove(PREF_CURRENT_FAMILY_HOME)
        remove(PREF_PARAM_REMEMBER_PASSWORD_WIFI)
        remove(PREF_PARAM_DEVICE_TOKEN)
        remove(PREF_PARAM_EMAIL)
        remove(PREF_USER_JF)
        remove(PREF_TOKEN_JF_TECH)
        remove(USER_PHONE_NUMBER)
        remove(PREF_DEFAULT_GET_BIOMETRIC_AUTHENTICATION)
        remove(PREF_USER_NAME)
        remove(PREF_USER_PASSWORD)
        Log.d("kienda", "logout PREF_PARAM_USER_INFO 00: ${getUserToken()}")
        setUserPassword("")
    }

    override fun setAppId(appId: String) = put(PREF_PARAM_APP_ID, appId)

    override fun getAppId() = get(PREF_PARAM_APP_ID)

    override fun setCameraAccessToken(accessToken: String) {
        put(PREF_PARAM_CAMERA_ACCESS_TOKEN, accessToken)
    }

    override fun getCameraAccessToken(): String? = get(PREF_PARAM_CAMERA_ACCESS_TOKEN)

    override fun getRefreshToken(): String {
        return get(PREF_PARAM_REFRESH_TOKEN) ?: ""
    }

    override fun setRefreshToken(token: String) {
        put(PREF_PARAM_REFRESH_TOKEN, token)
    }

    override fun setCameraVerifyCode(cameraNo: String, verifyCode: String) {
        put(cameraNo, verifyCode)
    }

    override fun getCameraVerifyCode(cameraNo: String): String? = get(cameraNo)

    override fun setApiKeyAndEmail(apiKey: String, email: String) {
        put(PREF_PARAM_API_KEY, apiKey)
        put(PREF_PARAM_EMAIL, email)
    }

    override fun getApiKey() = get(PREF_PARAM_API_KEY)

    override fun getEmail() = get(PREF_PARAM_EMAIL)

    override fun setShowTypeListDevice(type: Int) {
        mPrefs.edit().putInt(PREF_SHOW_TYPE_LIST_DEVICE, type).apply()
    }

    override fun getShowTypeListDevice(): Int {
        return mPrefs.getInt(PREF_SHOW_TYPE_LIST_DEVICE, 0)
    }

    override fun setCurrentFamilyHome(familyResponse: String) {
        if (familyResponse.isEmpty()) {
            remove(PREF_CURRENT_FAMILY_HOME)
        } else {
            mPrefs.edit().putString(PREF_CURRENT_FAMILY_HOME, familyResponse).apply()
        }
    }

    override fun getCurrentFamilyHome(): String? {
        return mPrefs.getString(PREF_CURRENT_FAMILY_HOME, "")
    }

    override fun getCurrentOrganizationName(): String {
        return try {
            JSONObject(getCurrentFamilyHome() ?: "").getString("name")
        } catch (e: Exception) {
            ""
        }
    }

    override fun setSkipLogin(skipLogin: Boolean) {
        mPrefs.edit().putBoolean(PREF_SKIP_LOGIN, skipLogin).apply()
    }

    override fun isSkipLogin(): Boolean {
        return mPrefs.getBoolean(PREF_SKIP_LOGIN, false)
    }

    override fun saveUserInfor(email: String) {
        put(PREF_PARAM_EMAIL, email)
    }

    override fun getRememberEmail(): String? {
        return mPrefs.getString(PREF_PARAM_REMEMBER_EMAIL, "")
    }

    override fun rememberEmail(email: String) {
        put(PREF_PARAM_REMEMBER_EMAIL, email)
    }

    override fun setDeviceToken(deviceToken: String) {
        put(PREF_PARAM_DEVICE_TOKEN, deviceToken)
    }

    override fun getDeviceToken(): String? {
        return mPrefs.getString(PREF_PARAM_DEVICE_TOKEN, "")
    }

    override fun setIdentifyId(identifyId: String) {
        put(PREF_PARAM_IDENTIFY_ID, identifyId)
    }

    override fun getIdentifyId(): String? {
        return mPrefs.getString(PREF_PARAM_IDENTIFY_ID, "")
    }

    override fun setCurrentListDevice(deviceInHome: String) {
        mPrefs.edit().putString(PREF_CURRENT_LIST_DEVICE, deviceInHome).apply()
    }

    override fun getCurrentListDevice(): String? {
        return mPrefs.getString(PREF_CURRENT_LIST_DEVICE, "")
    }

    override fun setTopicsFcm(topics: String) {
        mPrefs.edit().putString(PREF_TOPICS_FCM, topics).apply()
    }

    override fun getTopicsFcm(): String? {
        return mPrefs.getString(PREF_TOPICS_FCM, "")
    }

    override fun rememberPasswordWifi(name: String, password: String) {
        put(name, password)
    }

    override fun getRememberPasswordWifi(name: String): String? {
        return mPrefs.getString(name, "")
    }

    override fun getGroupsDevice(): List<String> {
        val result = mPrefs.getString(PREF_GROUP_DEVICE, "")
        return try {
            Gson().fromJson(result, Array<String>::class.java).toList()
        } catch (e: Exception) {
            listOf()
        }
    }

    override fun setGroupDevice(list: List<String>) {
        mPrefs.edit().putString(PREF_GROUP_DEVICE, Gson().toJson(list)).apply()
    }

    override fun getLayoutListDevice(default: Int): Int {
        return mPrefs.getInt(PREF_LAYOUT_LIST_DEVICE, default)
    }

    override fun setLayoutListDevice(value: Int) {
        mPrefs.edit().putInt(PREF_LAYOUT_LIST_DEVICE, value).apply()
    }

    override fun getDebugModeEnable(): Boolean {
        return mPrefs.getBoolean(PREF_DEBUG_MODE, false)
    }

    override fun setDebugModeEnable(isEnable: Boolean) {
        mPrefs.edit().putBoolean(PREF_DEBUG_MODE, isEnable).apply()
    }

    override fun setUserId(useId: String) {
        mPrefs.edit().putString(USER_ID, useId).apply()
    }

    override fun getUserId(): String {
        return mPrefs.getString(USER_ID, "") ?: ""
    }

    override fun setUserPhoneNumber(phoneNumber: String) {
        mPrefs.edit().putString(USER_PHONE_NUMBER, phoneNumber).apply()
    }

    override fun getUserPhoneNumber(): String {
        return mPrefs.getString(USER_PHONE_NUMBER, Constants.EMPTY) ?: Constants.EMPTY
    }

    override fun setUserJF(userJF: String) {
        mPrefs.edit().putString(PREF_USER_JF, userJF).apply()
    }

    override fun getUserJF(): String {
        if (mPrefs.getString(PREF_USER_JF, "").isNullOrEmpty()) {
            if (getUserPhoneNumber().isNotEmpty()) {
                return "JF${getUserPhoneNumber().removeRange(0, 1)}"
            }
        }
        return mPrefs.getString(PREF_USER_JF, "") ?: ""
    }

    override fun setNONCE(nonce: String) {
        mPrefs.edit().putString(NONCE_USER_KEY, nonce).apply()
    }

    override fun getNONCE(): String {
        return mPrefs.getString(NONCE_USER_KEY, "") ?: ""
    }

    override fun setSecretKey(key: String) {
        mPrefs.edit().putString(USER_SECRET_KEY, key).apply()
    }

    override fun getSecretKey(): String {
        return mPrefs.getString(USER_SECRET_KEY, "") ?: ""
    }

    override fun setAuthorizationProtocol(authorization: String) {
        mPrefs.edit().putString(AUT_CAMERA_VTT_PROTOCOL, authorization).apply()
    }

    override fun getAuthorizationProtocol(): String {
        return mPrefs.getString(AUT_CAMERA_VTT_PROTOCOL, "") ?: ""
    }

    override fun setTimeRefreshAuthorizationProtocol(time: Long) {
        mPrefs.edit().putLong(TIME_REFRESH_AUT_CAMERA_VTT_PROTOCOL, time).apply()
    }

    override fun getTimeRefreshAuthorizationProtocol(): Long {
        return mPrefs.getLong(TIME_REFRESH_AUT_CAMERA_VTT_PROTOCOL, 0)
    }

    override fun setIMEI(imei: String) {
        mPrefs.edit().putString(KEY_SHARE_PREF_IMEI, imei).apply()
    }

    override fun getIMEI(): String {
        var imei = mPrefs.getString(KEY_SHARE_PREF_IMEI, "")
        if (imei.isNullOrEmpty()) {
            imei = UUID.randomUUID().toString()
            mPrefs.edit().putString(KEY_SHARE_PREF_IMEI, imei).apply()
        }
        return imei
    }

    override fun setShowPopupCardError(list: List<String>) {
        mPrefs.edit().putString(PREF_POPUP_CARD_ERROR, Gson().toJson(list)).apply()
    }

    override fun getShowPopupCardError(): List<String> {
        val result = mPrefs.getString(PREF_POPUP_CARD_ERROR, "")
        return try {
            Gson().fromJson(result, Array<String>::class.java).toList()
        } catch (e: Exception) {
            listOf()
        }
    }

    override fun getInstructions(): List<Int> {
        return try {
            Gson().fromJson(mPrefs.getString(PREF_INSTRUCTIONS, ""), Array<Int>::class.java)
                .toList()
        } catch (_: Exception) {
            listOf()
        }
    }

    override fun saveInstruction(instruction: Int) {
        val current = getInstructions() + instruction
        mPrefs.edit().putString(PREF_INSTRUCTIONS, Gson().toJson(current.toSet().sorted())).apply()
    }

    override fun setStateShowDialogINFOUpdate(state: Boolean) {
        mPrefs.edit().putBoolean(STATUS_INFO_VERSION_UPDATE, state).apply()
    }

    override fun getStateShowDialogINFOUpdate(): Boolean {
        return mPrefs.getBoolean(STATUS_INFO_VERSION_UPDATE, false)
    }

    override fun setListVersionINFOUpdate(version: String) {
        val result = mPrefs.getString(LIST_VERSION_INFO_DISABLE_UPDATE, "")
        val data = try {
            Gson().fromJson(result, Array<String>::class.java).toMutableList()
        } catch (e: Exception) {
            mutableListOf()
        }
        data.add(version)
        mPrefs.edit().putString(LIST_VERSION_INFO_DISABLE_UPDATE, Gson().toJson(data.toList()))
            .apply()

    }

    override fun getListVersionINFOUpdate(): List<String> {
        val result = mPrefs.getString(LIST_VERSION_INFO_DISABLE_UPDATE, "")
        return try {
            Gson().fromJson(result, Array<String>::class.java).toList()
        } catch (e: Exception) {
            listOf()
        }
    }

    override fun setTokenJFTech(token: String) {
        mPrefs.edit().putString(PREF_TOKEN_JF_TECH, token).apply()
    }

    override fun getTokeJFTech(): String {
        return mPrefs.getString(PREF_TOKEN_JF_TECH, "") ?: ""
    }

    override fun setPushTokenJFTech(token: String) {
        mPrefs.edit().putString(PREF_PUSH_TOKEN_JF_TECH, token).apply()
    }

    override fun getPushTokeJFTech(): String {
        return mPrefs.getString(PREF_PUSH_TOKEN_JF_TECH, "") ?: ""
    }

    override fun setIMOUToken(token: String) {
        mPrefs.edit().putString(PREF_IMOU_TOKEN, token).apply()
    }

    override fun setIMOUAdminToken(token: String) {
        mPrefs.edit().putString(PREF_IMOU_ADMIN_TOKEN, token).apply()
    }

    override fun setIMOUOpenId(openId: String) {
        mPrefs.edit().putString(PREF_IMOU_OPEN_ID, openId).apply()
    }

    override fun getIMOUToken(): String {
        return mPrefs.getString(PREF_IMOU_TOKEN, "") ?: ""
    }

    override fun getIMOUAdminToken(): String {
        return mPrefs.getString(PREF_IMOU_ADMIN_TOKEN, "") ?: ""
    }

    override fun getIMOUOpenId(): String {
        return mPrefs.getString(PREF_IMOU_OPEN_ID, "") ?: ""
    }

    override fun setIsShowingPopupLocalMode(isShowing: Boolean) {
        mPrefs.edit().putBoolean(PREF_IS_SHOWING_POPUP_LOCAL_MODE, isShowing).apply()
    }

    override fun isShowingPopupLocalMode(): Boolean {
        return mPrefs.getBoolean(PREF_IS_SHOWING_POPUP_LOCAL_MODE, false)
    }

    override fun setDefinitionJF(item: DefinitionJF) {
        val result: MutableList<DefinitionJF>
        val serializedObject: String? = mPrefs.getString(PREF_DEFINITION_JF, null)
        val gson = Gson()

        if (serializedObject != null) {
            val type = object : TypeToken<List<DefinitionJF>>() {}.type
            result = gson.fromJson(serializedObject, type)
            if (result.any { it.cameraId == item.cameraId }) {
                result.find { it.cameraId == item.cameraId }?.type = item.type
            } else {
                result.add(item)
            }
        } else {
            result = mutableListOf()
            result.add(item)
        }
        mPrefs.edit().putString(PREF_DEFINITION_JF, gson.toJson(result)).apply()
    }

    override fun getTypeDefinitionJF(cameraId: String): Int {
        var result: MutableList<DefinitionJF> = mutableListOf()
        val serializedObject: String? = mPrefs.getString(PREF_DEFINITION_JF, null)
        if (serializedObject != null) {
            val gson = Gson()
            val type = object : TypeToken<List<DefinitionJF>>() {}.type
            result = gson.fromJson(serializedObject, type)
        }
        return result.find { it.cameraId == cameraId }?.type ?: 0
    }

    override fun setEzvizCameraVerifyCode(
        userID: String,
        serialCamera: String,
        verifyCode: String
    ) {
        mPrefs.edit().putString(
            KEY_EZVIZ_CAMERA_VERIFY_CODE + userID + serialCamera,
            "$userID" + "_" + "$serialCamera" + "_" + "$verifyCode"
        ).apply()
    }

    override fun getEzvizCameraVerifyCode(serialCamera: String): String {
        return mPrefs.getString(KEY_EZVIZ_CAMERA_VERIFY_CODE + getUserId() + serialCamera, "") ?: ""
    }

    override fun setFirebaseToken(token: String) {
        mPrefs.edit().putString(FIREBASE_TOKEN, token).apply()
    }

    override fun getFirebaseToken(): String {
        return mPrefs.getString(FIREBASE_TOKEN, "") ?: ""
    }

    override fun setImagePreset(id: String, path: String) {
        Log.d("TAG", "set ImagePreset: id = $id --- path = $path")
        mPrefs.edit().putString(id, path).apply()
    }

    override fun getImagePreset(id: String): String {
        Log.d("TAG", "get ImagePreset: id = $id --- path = ${mPrefs.getString("$id", "") ?: ""}")
        return mPrefs.getString(id, "") ?: ""
    }

    override fun deleteImagePreset(id: String) {
        val path = getImagePreset(id)
        Utils.deleteFile(path)
        mPrefs.edit().remove(id)
    }

    override fun setOrgIDAccount(orgId: String) {
        mPrefs.edit().putString(PREF_ORG_ID_ACCOUT, orgId).apply()
    }

    override fun getOrgIDAccount(): String {
        return mPrefs.getString(PREF_ORG_ID_ACCOUT, "") ?: ""
    }

    override fun setIsAcceptPrivacyPolicy(isShowing: Boolean) {
        mPrefs.edit().putBoolean(PREF_ENABLE_SHOW_PRIVATE_POLICY, isShowing).apply()
    }

    override fun getIsAcceptPrivacyPolicy(): Boolean {
        return mPrefs.getBoolean(PREF_ENABLE_SHOW_PRIVATE_POLICY, false)
    }

    override fun setIsNotShowDiaLogFreeCloud(isShowing: Boolean) {
        mPrefs.edit().putBoolean(PREF_ENABLE_SHOW_FREE_CLOUD, isShowing).apply()
    }

    override fun getIsNotShowDiaLogFreeCloud(): Boolean {
        return mPrefs.getBoolean(PREF_ENABLE_SHOW_FREE_CLOUD, false)
    }

    override fun setIsShowDiaLogSyncEZViz(isShowing: Boolean) {
        mPrefs.edit().putBoolean(PREF_ENABLE_SHOW_SYNC_EZVIZ, isShowing).apply()
    }

    override fun getIsShowDiaLogSyncEZViz(): Boolean {
        return mPrefs.getBoolean(PREF_ENABLE_SHOW_SYNC_EZVIZ, false)
    }

    override fun setNotRequestOverlayAgain(isShowing: Boolean) {
        mPrefs.edit().putBoolean(NOT_REQUEST_OVERLAY_AGAIN, isShowing).apply()
    }

    override fun getNotRequestOverlayAgain(): Boolean {
        return mPrefs.getBoolean(NOT_REQUEST_OVERLAY_AGAIN, false)
    }

    override fun setIsNotShowDiaLogOneDayFreeCLoud(isShowing: Boolean) {
        mPrefs.edit().putBoolean(PREF_ENABLE_NOT_SHOW_ONE_DAY_FREE_CLOUD, isShowing).apply()
    }

    override fun getIsNotShowDiaLogOneDayFreeCLoud(): Boolean {
        return mPrefs.getBoolean(PREF_ENABLE_NOT_SHOW_ONE_DAY_FREE_CLOUD, false)
    }

    override fun setLinkUserManual(linkUser: String) {
        mPrefs.edit().putString(PREF_LINK_URL_USER_MANUAL, linkUser).apply()
    }

    override fun getLinkUserManual(): String {
        return mPrefs.getString(PREF_LINK_URL_USER_MANUAL, "") ?: ""
    }

    override fun setNotRequestDoNotDisturbAgain(isShowing: Boolean) {
        mPrefs.edit().putBoolean(NOT_REQUEST_DO_NOT_DISTURB_AGAIN, isShowing).apply()
    }

    override fun getNotRequestDoNotDisturbAgain(): Boolean {
        return mPrefs.getBoolean(NOT_REQUEST_DO_NOT_DISTURB_AGAIN, false)
    }

    /*    override fun setShowPopupCardError(list: List<String>) {
            mPrefs.edit().putString(PREF_POPUP_CARD_ERROR, Gson().toJson(list)).apply()
        }

        override fun getShowPopupCardError(): List<String> {
            val result = mPrefs.getString(PREF_POPUP_CARD_ERROR, "")
            return try {
                Gson().fromJson(result, Array<String>::class.java).toList()
            } catch (e: Exception) {
                listOf()
            }
        }*/

    override fun setCurrentFamilyHomeSelected(listHomeSelected: String) {
        if (listHomeSelected.isEmpty()) {
            remove(PREF_HOME_SELECTED + getUserId())
        } else {
            mPrefs.edit().putString(PREF_HOME_SELECTED + getUserId(), listHomeSelected).apply()
        }
    }

    override fun getCurrentFamilyHomeSelected(): String? {
        return mPrefs.getString(PREF_HOME_SELECTED + getUserId(), "")
    }

    override fun setCurrentFamilyCameraSelected(listCamSelected: String, orgId: String) {
        if (listCamSelected.isEmpty()) {
            remove(PREF_CAMERA_SELECTED + getUserId() + orgId)
        } else {
            mPrefs.edit().putString(PREF_CAMERA_SELECTED + getUserId() + orgId, listCamSelected)
                .apply()
        }
    }

    override fun getCurrentFamilyCameraSelected(orgId: String): String? {
        return mPrefs.getString(PREF_CAMERA_SELECTED + getUserId() + orgId, "")
    }

    override fun setIsShowDialogSpreadVT(dataShow: String) {
        mPrefs.edit().putString(PREF_IS_SHOW_DIALOG_SPREAD, dataShow).apply()
    }

    override fun getIsShowDialogSpreadVT(): String? {
        return mPrefs.getString(PREF_IS_SHOW_DIALOG_SPREAD, "")
    }

    override fun setTypeManager(isTypeList: Boolean) {
        mPrefs.edit().putBoolean(PREF_TYPE_MANAGER, isTypeList).apply()
    }

    override fun getTypeManager(): Boolean {
        return mPrefs.getBoolean(PREF_TYPE_MANAGER, false)
    }

    override fun setDefaultHome(orgId: String) {
        mPrefs.edit().putString(PREF_TYPE_MANAGER + "_" + getUserId(), orgId).apply()
    }

    override fun getDefaultHome(): String {
        return mPrefs.getString(PREF_TYPE_MANAGER + "_" + getUserId(), "") ?: ""
    }

    override fun setUseBiometricAuthentication(isUse: Boolean) {
        mPrefs.edit()
            .putBoolean(PREF_DEFAULT_GET_BIOMETRIC_AUTHENTICATION + "_" + getUserId(), isUse)
            .apply()
    }

    override fun getUseBiometricAuthentication(): Boolean {
        return mPrefs.getBoolean(
            PREF_DEFAULT_GET_BIOMETRIC_AUTHENTICATION + "_" + getUserId(),
            false
        )
    }

    override fun setUserName(userName: String) {
        mPrefs.edit().putString(PREF_USER_NAME + "_" + getUserId(), userName).apply()
    }

    override fun getUserName(): String {
        return mPrefs.getString(PREF_USER_NAME + "_" + getUserId(), "") ?: ""
    }

    override fun setUserPassword(userPassword: String) {
        mPrefs.edit().putString(PREF_USER_PASSWORD + "_" + getUserId(), userPassword).apply()
    }

    override fun getUserPassword(): String {
        return mPrefs.getString(PREF_USER_PASSWORD + "_" + getUserId(), "") ?: ""
    }

    override fun setListSearchProductRecent(list: MutableList<String>) {
        mPrefs.edit().putString(PREF_LIST_SEARCH_RECENT, Gson().toJson(list)).apply()
    }

    override fun getListSearchProductRecent(): MutableList<String> {
        val result = mPrefs.getString(PREF_LIST_SEARCH_RECENT, "")
        return try {
            Gson().fromJson(result, Array<String>::class.java).toMutableList()
        } catch (e: Exception) {
            mutableListOf<String>()
        }
    }
}