package com.viettel.vht.sdk.navigation

import android.os.Bundle
import com.vht.sdkcore.navigationComponent.BaseNavigatorImpl
import com.viettel.vht.sdk.R
import dagger.hilt.android.scopes.ActivityScoped
import javax.inject.Inject

@ActivityScoped
class AppNavigatorImpl @Inject constructor() : BaseNavigatorImpl(), AppNavigation {
    override fun openGateWayDeviceScreen(bundle: Bundle?) {
        TODO("Not yet implemented")
    }

    override fun openDetailCurtainsScreen(bundle: Bundle?) {
        TODO("Not yet implemented")
    }

    override fun openLanDeviceCameraJF(bundle: Bundle?) {
        openScreen(R.id.action_JFAddLocalNetworkFragment_to_JFLanConnectFragment, bundle)
    }

    override fun openWifiConnectCameraJF(bundle: Bundle?) {
        openScreen(R.id.action_JFChooseTypeAddFragment_to_JFTechQuickGide1AddCameraFragment, bundle)
    }

    override fun openLanConnectCameraJF(bundle: Bundle?) {
        openScreen(R.id.action_JFChooseTypeAddFragment_to_JFAddLocalNetworkFragment, bundle)
    }

    override fun openLanToSetNameCameraJF(bundle: Bundle?) {
        // openScreen(R.id.action_JFLanConnectFragment_to_PairSuccessFragment, bundle)
    }

    override fun openWifiRoomAddCamera(bundle: Bundle?) {
        openScreen(
            R.id.action_JFTechQuickGide1AddCameraFragment_to_JFTechWifiRoomAddCameraFragment,
            bundle
        )
    }

    override fun openShowQRCodeAddCamera(bundle: Bundle?) {
        openScreen(
            R.id.action_JFTechQuickGide2AddCameraFragment_to_JFTechShowQRCodeAddCameraFragment,
            bundle
        )
    }

    override fun openSetDeviceNameCameraJF(bundle: Bundle?) {
        // openScreen(R.id.action_JFTechSetPassWordAddCameraFragment_to_PairSuccessFragment, bundle)
    }

    override fun openSetPassWordCamera(bundle: Bundle?) {
        openScreen(
            R.id.action_JFTechShowQRCodeAddCameraFragment_to_JFTechSetPassWordAddCameraFragment,
            bundle
        )
    }

    override fun openQuickGide2AddCamera(bundle: Bundle?) {
        openScreen(
            R.id.action_JFTechWifiRoomAddCameraFragment_to_JFTechQuickGide2AddCameraFragment,
            bundle
        )
    }

    override fun openAddCameraJF(bundle: Bundle?) {
        openScreen(
            R.id.action_SDKVHomeLoadingFragment_to_add_camera_jftech_graph,
            bundle
        )
    }

    override fun openDetailCameraJF(bundle: Bundle?) {
        openScreen(
            R.id.action_SDKVHomeLoadingFragment_to_jfcamera_detail_graph,
            bundle
        )
    }

    override fun openSettingCameraJF(bundle: Bundle?) {
        openScreen(
            R.id.action_JFCameraDetailFragment_to_settingCameraJFFragment,
            bundle
        )
    }

    override fun openCardSettingJFCamera(bundle: Bundle?) {
        openScreen(R.id.action_settingCameraJFFragment_to_cardSettingJFFragment, bundle)
    }

    override fun openUpdateFirmwareJFCamera(bundle: Bundle?) {
        openScreen(R.id.action_SDKVHomeLoadingFragment_to_updateFirmwareCameraJFFragment, bundle)
    }

    override fun openInfoSettingCameraJF(bundle: Bundle?) {
        openScreen(R.id.action_settingCameraJFFragment_to_informationCameraJFFragment, bundle)
    }

    override fun openSpreadCameraFragment(bundle: Bundle?) {
        openScreen(R.id.global_action_spreadCameraFragment, bundle)
    }

    override fun openListScheduleAlarmJFCamera(bundle: Bundle?) {
        openScreen(
            R.id.action_settingCameraJFFragment_to_listScheduleAlarmSettingCameraJFFragment,
            bundle
        )
    }

    override fun openSettingAlarmFeatureCameraJFFragment(bundle: Bundle?) {
        openScreen(
            R.id.action_settingCameraJFFragment_to_settingAlarmFeatureCameraJFFragment,
            bundle
        )
    }

    override fun openChangePasswordImageEncodingFragment(bundle: Bundle?) {
        openScreen(
            R.id.action_settingCameraJFFragment_to_changePasswordImageEncodingFragment,
            bundle
        )
    }

    override fun openCloudStorageJFCamera(bundle: Bundle?) {
        openScreen(R.id.action_settingCameraJFFragment_to_JFCloudStorageFragment, bundle)
    }

    override fun openSettingWifiCameraJF(bundle: Bundle?) {
        openScreen(R.id.action_informationCameraJFFragment_to_settingWifiCameraJFFragment, bundle)
    }

    override fun openAddScheduleAlarmJFCamera(bundle: Bundle?) {
        openScreen(
            R.id.action_listScheduleAlarmSettingCameraJFFragment_to_addScheduleAlarmSettingCameraJFFragment,
            bundle
        )
    }

    override fun openVerificationCodeCameraJF(bundle: Bundle?) {
        openScreen(R.id.action_to_verificationCodeCameraJFFragment, bundle)
    }

    override fun openResetPasswordCameraJF(bundle: Bundle?) {
        openScreen(
            R.id.action_verificationCodeCameraJFFragment_to_resetPasswordCameraJFFragment,
            bundle
        )
    }

    override fun openSmartAlertSetJFCamera(bundle: Bundle?) {
        openScreen(
            R.id.action_settingAlarmFeatureCameraJFFragment_to_JFSmartAlertSetFragment,
            bundle
        )
    }

    override fun openListAlarmVoicePeriodJFCamera(bundle: Bundle?) {
        openScreen(
            R.id.action_settingAlarmFeatureCameraJFFragment_to_listAlarmVoicePeriodJFFragment,
            bundle
        )
    }

    override fun openAddAlarmVoicePeriodJFCamera(bundle: Bundle?) {
        openScreen(
            R.id.action_listAlarmVoicePeriodJFFragment_to_addAlarmVoicePeriodJFFragment,
            bundle
        )
    }

    override fun openRegisterPayLinkFragment(bundle: Bundle?) {
        openScreen(R.id.action_global_RegisterPayLinkWebViewFragment, bundle)
    }

    override fun openViettelPayWebViewFragment(bundle: Bundle?) {
        openScreen(R.id.action_to_viettelPayWebviewFragment, bundle)
    }

    override fun backToCloudStorageJFCamera() {
        popopBackStack(R.id.JFCloudStorageFragment)
    }

    override fun backToCameraCloudStorageAccountFragment() {
        //todo mà danh sách list camera dăng ký, để chọn tặng cloud người thân
        //  popopBackStack(R.id.CameraCloudStorageAccountFragment)
    }

    override fun openPayLinkConfigOTP(bundle: Bundle?) {
        openScreen(R.id.action_global_PayLinkConfigOTPFragment, bundle)
    }

    override fun openCloudRegisterJFCamera(bundle: Bundle?) {
        openScreen(R.id.action_JFCloudStorageFragment_to_JFCloudRegisterFragment, bundle)
    }

    override fun openHistoryByCloudStorage(bundle: Bundle?) {
        openScreen(R.id.action_global_HistoryBuyCloudStorageFragment, bundle)
    }

    override fun openConfigOTPOnOffPayLinkFragment(bundle: Bundle?) {
        openScreen(R.id.action_global_ConfigOTPOnOffPayLinkFragment, bundle)
    }

    override fun openCloudStorageCamera(bundle: Bundle?) {
        openScreen(R.id.action_setting_to_JFCloudStorageFragment, bundle)
    }

    override fun openDetailHistoryCloudStorage(bundle: Bundle?) {
        openScreen(R.id.action_global_DetailBuyCloudStorageFragment, bundle)
    }

    override fun openRegisterCloudToDetailGiftRelatives(bundle: Bundle?) {
        openScreen(R.id.action_to_JFCloudRegisterFragment, bundle)
    }

    override fun openRegisterPromotionFree(bundle: Bundle?) {
        openScreen(R.id.action_global_RegisterCloudPromotionFragment, bundle)
    }

    override fun openGlobalManageCloudPromotionOTP(bundle: Bundle?) {
        openScreen(R.id.action_global_CloudPromotionConfigOTPFragment, bundle)
    }
}