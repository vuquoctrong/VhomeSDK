package com.vht.sdkcore.pref

import com.vht.sdkcore.camera.DefinitionJF
import javax.inject.Singleton

@Singleton
interface RxPreferences {
    fun put(key: String, value: String)

    fun get(key: String): String?

    fun putBoolean(key: String, value: Boolean)

    fun getBoolean(key: String): Boolean

    fun clear()

    fun remove(key: String)

    fun getUserToken(): String?

    fun setUserToken(userToken: String, deviceToken: String = "")

    fun getRefreshToken(): String

    fun setRefreshToken(token: String)

    fun logout()

    fun setAppId(appId: String)

    fun getAppId(): String?

    fun setCameraAccessToken(accessToken: String)

    fun getCameraAccessToken(): String?

    fun setCameraVerifyCode(cameraNo: String, verifyCode: String)

    fun getCameraVerifyCode(cameraNo: String): String?

    fun setApiKeyAndEmail(apiKey: String, email: String)

    fun getApiKey(): String?

    fun getEmail(): String?

    fun setShowTypeListDevice(type: Int)

    fun getShowTypeListDevice(): Int

    fun setCurrentFamilyHome(familyResponse: String)

    fun getCurrentFamilyHome(): String?

    fun getCurrentOrganizationName(): String

    fun setSkipLogin(skipLogin: Boolean)

    fun isSkipLogin(): Boolean

    fun rememberEmail(email: String)

    fun saveUserInfor(email: String)

    fun getRememberEmail(): String?

    fun setDeviceToken(deviceToken: String)

    fun getDeviceToken(): String?

    fun setIdentifyId(identifyId: String)

    fun getIdentifyId(): String?

    fun setCurrentListDevice(listDevice: String)

    fun getCurrentListDevice(): String?

    fun setTopicsFcm(topics: String)

    fun getTopicsFcm(): String?

    fun rememberPasswordWifi(name: String, password: String)

    fun getRememberPasswordWifi(name: String): String?

    fun getGroupsDevice(): List<String>

    fun setGroupDevice(list: List<String>)

    fun getLayoutListDevice(default: Int = 0): Int

    fun setLayoutListDevice(value: Int)

    fun getDebugModeEnable(): Boolean

    fun setDebugModeEnable(isEnable: Boolean)

    fun setUserId(useId: String)

    fun getUserId(): String

    fun setUserPhoneNumber(phoneNumber: String)

    fun setOrgIDAccount(orgId: String)

    fun getOrgIDAccount(): String

    fun getUserPhoneNumber(): String

    fun setUserJF(userJF: String)

    fun getUserJF(): String

    fun setNONCE(nonce: String)

    fun getNONCE(): String

    fun setSecretKey(key: String)

    fun getSecretKey(): String

    fun setAuthorizationProtocol(authorization: String)

    fun getAuthorizationProtocol(): String

    fun setTimeRefreshAuthorizationProtocol(time: Long)

    fun getTimeRefreshAuthorizationProtocol(): Long

    fun setIMEI(imei: String)

    fun getIMEI(): String

    fun setShowPopupCardError(list: List<String>)

    fun getShowPopupCardError(): List<String>

    fun getInstructions(): List<Int>

    fun saveInstruction(instruction: Int)

    fun setStateShowDialogINFOUpdate(state: Boolean)

    fun getStateShowDialogINFOUpdate(): Boolean

    fun setListVersionINFOUpdate(version: String)

    fun getListVersionINFOUpdate(): List<String>

    fun setTokenJFTech(token: String)

    fun getTokeJFTech(): String

    fun setPushTokenJFTech(token: String)

    fun getPushTokeJFTech(): String

    fun getIMOUToken(): String

    fun getIMOUAdminToken(): String

    fun getIMOUOpenId(): String

    fun setIMOUToken(token: String)

    fun setIMOUAdminToken(token: String)

    fun setIMOUOpenId(openId: String)

    fun setIsShowingPopupLocalMode(isShowing: Boolean)

    fun isShowingPopupLocalMode(): Boolean

    fun setDefinitionJF(item: DefinitionJF)

    fun getTypeDefinitionJF(cameraId: String): Int

    fun setEzvizCameraVerifyCode(userID: String, serialCamera: String, verifyCode: String)

    fun getEzvizCameraVerifyCode(cameraSerial: String): String

    fun setIsAcceptPrivacyPolicy(isShowing: Boolean)

    fun getIsAcceptPrivacyPolicy(): Boolean

    fun setNotRequestOverlayAgain(isShowing: Boolean)

    fun getNotRequestOverlayAgain(): Boolean

    fun setNotRequestDoNotDisturbAgain(isShowing: Boolean)

    fun getNotRequestDoNotDisturbAgain(): Boolean

    fun setLinkUserManual(linkUser: String)

    fun getLinkUserManual(): String

    fun setFirebaseToken(token: String)

    fun getFirebaseToken(): String

    fun setIsNotShowDiaLogFreeCloud(isShowing: Boolean)

    fun getIsNotShowDiaLogFreeCloud(): Boolean

    fun setIsShowDiaLogSyncEZViz(isShowing: Boolean)

    fun getIsShowDiaLogSyncEZViz(): Boolean

    fun setIsNotShowDiaLogOneDayFreeCLoud(isShowing: Boolean)

    fun getIsNotShowDiaLogOneDayFreeCLoud(): Boolean

    fun setImagePreset(id: String, path: String)

    fun getImagePreset(id: String): String

    fun deleteImagePreset(id: String)

    fun setCurrentFamilyHomeSelected(listHomeSelected: String)

    fun getCurrentFamilyHomeSelected(): String?

    fun setCurrentFamilyCameraSelected(listHomeSelected: String, orgId: String = "")

    fun getCurrentFamilyCameraSelected(orgId: String = ""): String?

    fun setIsShowDialogSpreadVT(dataShow: String)

    fun getIsShowDialogSpreadVT(): String?

    fun setListSearchProductRecent(list: MutableList<String>)

    fun getListSearchProductRecent(): MutableList<String>

    fun setTypeManager(isTypeList: Boolean)

    fun getTypeManager(): Boolean

    fun setDefaultHome(orgId: String)

    fun getDefaultHome(): String

    fun setUseBiometricAuthentication(isUse: Boolean)

    fun getUseBiometricAuthentication(): Boolean

    fun setUserName(userName: String)

    fun getUserName(): String

    fun setUserPassword(userName: String)

    fun getUserPassword(): String

}