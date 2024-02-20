package com.vht.sdkcore.navigationComponent

import android.os.Bundle
import androidx.annotation.IdRes
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import com.vht.sdkcore.R
import com.vht.sdkcore.navigationComponent.BaseNavigator
import timber.log.Timber
import java.lang.ref.WeakReference

val defaultNavOptions = NavOptions.Builder()
    .setEnterAnim(R.anim.slide_in_left)
    .setExitAnim(R.anim.slide_out_right)
    .setPopEnterAnim(R.anim.slide_in_right)
    .setPopExitAnim(R.anim.slide_out_left)
    .build()

val nullNavOptions = NavOptions.Builder()
    .setEnterAnim(R.anim.null_anim)
    .setExitAnim(R.anim.null_anim)
    .setPopEnterAnim(R.anim.null_anim)
    .setPopExitAnim(R.anim.null_anim)
    .build()

abstract class BaseNavigatorImpl : BaseNavigator {

    override var navController: NavController? = null
    override var navControllerHome: NavController? = null

    override fun bind(navController: NavController) {
        this.navController = WeakReference(navController).get()

        navController.addOnDestinationChangedListener { _, destination, _ ->
            run {
                Timber.tag("Back stack Navigation").d(destination.navigatorName)
            }
        }
    }

    override fun bindNavigationHome(navController: NavController) {
        this.navControllerHome = WeakReference(navController).get()

        navController.addOnDestinationChangedListener { _, destination, _ ->
            run {
                Timber.tag("Back stack Navigation").d(destination.navigatorName)
            }
        }
    }

    override fun unbind() {
        navController = null
        navControllerHome = null
    }

    override fun openScreen(
        @IdRes id: Int,
        bundle: Bundle?
    ) {
        try {
            val action = navController?.currentDestination?.getAction(id)
            action?.let {
                navController?.navigate(id, bundle)
            }
        }catch (e:Exception){
            Timber.e("Error openScreen ${e.message}")
        }

    }

    override fun openScreenFromHome(id: Int, bundle: Bundle?) {
        navControllerHome?.navigate(id, bundle)
    }

    override fun navigateUp(): Boolean? {
        return navController?.navigateUp()
    }

    override fun navigateUpWithHome(): Boolean? {
        return navController?.navigateUp()
    }

    override fun currentFragmentId(): Int? {
        return navController?.currentDestination?.id
    }

    override fun setStartDestination(@IdRes id: Int) {
        if (navController == null) {
            return
        }
        val navGraph = navController?.graph
        if (navGraph != null) {
            navController?.graph = navGraph
        }
    }

    override fun popopBackStack(destination: Int) {
        navController?.popBackStack(destination, false)
        navControllerHome?.popBackStack(destination, false)
    }

    override fun clearCurrentFromBackstack() {
        currentFragmentId()?.let {
            navController?.clearBackStack(it)
        }
    }
}