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
}