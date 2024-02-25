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
}