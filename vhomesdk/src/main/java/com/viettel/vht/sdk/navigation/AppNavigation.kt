package com.viettel.vht.sdk.navigation

import android.os.Bundle
import com.vht.sdkcore.navigationComponent.BaseNavigator

interface AppNavigation : BaseNavigator {

    fun openGateWayDeviceScreen(bundle: Bundle? = null)

    fun openDetailCurtainsScreen(bundle: Bundle? = null)

    fun openLanDeviceCameraJF(bundle: Bundle? = null)

    fun openWifiConnectCameraJF(bundle: Bundle? = null)

    fun openLanConnectCameraJF(bundle: Bundle? = null)

    fun openLanToSetNameCameraJF(bundle: Bundle? = null)

    fun openWifiRoomAddCamera(bundle: Bundle? = null)

    fun openShowQRCodeAddCamera(bundle: Bundle? = null)

    fun openSetDeviceNameCameraJF(bundle: Bundle? = null)

    fun openSetPassWordCamera(bundle: Bundle? = null)

    fun openQuickGide2AddCamera(bundle: Bundle?= null)

    fun openAddCameraJF(bundle: Bundle? = null)

    fun openDetailCameraJF(bundle: Bundle? = null)

    fun openSettingCameraJF(bundle: Bundle? = null)

    fun openCardSettingJFCamera(bundle: Bundle? = null)

    fun openUpdateFirmwareJFCamera(bundle: Bundle? = null)

    fun openInfoSettingCameraJF(bundle: Bundle? = null)

    fun openSpreadCameraFragment(bundle: Bundle? = null)

    fun openListScheduleAlarmJFCamera(bundle: Bundle? = null)

    fun openSettingAlarmFeatureCameraJFFragment(bundle: Bundle? = null)

    fun openChangePasswordImageEncodingFragment(bundle: Bundle? = null)

    fun openCloudStorageJFCamera(bundle: Bundle?= null)

    fun openSettingWifiCameraJF(bundle: Bundle? = null)

    fun openAddScheduleAlarmJFCamera(bundle: Bundle? = null)

    fun openVerificationCodeCameraJF(bundle: Bundle? = null)

    fun openResetPasswordCameraJF(bundle: Bundle? = null)

    fun openSmartAlertSetJFCamera(bundle: Bundle? = null)

    fun openListAlarmVoicePeriodJFCamera(bundle: Bundle? = null)

    fun openAddAlarmVoicePeriodJFCamera(bundle: Bundle? = null)

    fun openRegisterPayLinkFragment(bundle: Bundle? = null)

    fun openViettelPayWebViewFragment(bundle: Bundle? = null)

    fun backToCloudStorageJFCamera()

    fun backToCameraCloudStorageAccountFragment()

    fun openPayLinkConfigOTP(bundle: Bundle? = null)

    fun openCloudRegisterJFCamera(bundle: Bundle?= null)

    fun openHistoryByCloudStorage(bundle: Bundle? = null)

    fun openConfigOTPOnOffPayLinkFragment(bundle: Bundle? = null)

    fun openCloudStorageCamera(bundle: Bundle? = null)

    fun openDetailHistoryCloudStorage(bundle: Bundle? = null)

    fun openRegisterCloudToDetailGiftRelatives(bundle: Bundle? = null)

    fun openRegisterPromotionFree(bundle: Bundle? = null)

    fun openGlobalManageCloudPromotionOTP(bundle: Bundle? = null)

}