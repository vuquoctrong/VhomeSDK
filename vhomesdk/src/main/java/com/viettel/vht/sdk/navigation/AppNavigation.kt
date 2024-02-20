package com.viettel.vht.sdk.navigation

import android.os.Bundle
import com.vht.sdkcore.navigationComponent.BaseNavigator

interface AppNavigation : BaseNavigator {

    fun openGateWayDeviceScreen(bundle: Bundle? = null)

    fun openDetailCurtainsScreen(bundle: Bundle? = null)
}