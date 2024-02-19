package com.vht.sdkcore.navigationComponent

import android.os.Bundle
import androidx.annotation.IdRes
import androidx.navigation.NavController

interface BaseNavigator {
    fun openScreen(
        @IdRes id: Int,
        bundle: Bundle? = null
    )

    fun openScreenFromHome(
        @IdRes id: Int,
        bundle: Bundle? = null
    )

    val navController: NavController?
    val navControllerHome: NavController?
    fun navigateUp(): Boolean?
    fun navigateUpWithHome(): Boolean?
    fun setStartDestination(@IdRes id: Int)
    fun currentFragmentId(): Int?
    fun bind(navController: NavController)
    fun unbind()
    fun popopBackStack(destination: Int)

    fun bindNavigationHome(navController: NavController)
    fun clearCurrentFromBackstack()
}