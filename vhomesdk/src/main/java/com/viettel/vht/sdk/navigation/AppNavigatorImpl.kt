package com.viettel.vht.sdk.navigation

import android.os.Bundle
import com.vht.sdkcore.navigationComponent.BaseNavigatorImpl
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

}