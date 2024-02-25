package com.viettel.vht.sdk.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkInfo

object NetworkManager {

    fun isOnline(context: Context?): Boolean {
        val connMgr = context?.applicationContext?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo: NetworkInfo? = connMgr.activeNetworkInfo
        return networkInfo?.isConnected == true
    }

    fun handlerNetWorkState(context: Context?, callAvailable: () -> Unit, callLost: () -> Unit){
        val connectivityManager = context?.applicationContext?.getSystemService(ConnectivityManager::class.java)

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            connectivityManager?.registerDefaultNetworkCallback(object : ConnectivityManager.NetworkCallback() {
                //connect internet
                override fun onAvailable(network : Network) {
                   callAvailable.invoke()
                }

                // disconnect internet
                override fun onLost(network : Network) {
                    callLost.invoke()
                }

            })
        }
    }


    fun checkTypeInternet(context: Context?):AppEnum.TypeNetwork{
        val connMgr = context?.applicationContext?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connMgr.allNetworks.forEach { network ->
            connMgr.getNetworkInfo(network)?.apply {
                if (type == ConnectivityManager.TYPE_WIFI && isConnected) {
                    return AppEnum.TypeNetwork.WIFI
                }
                if (type == ConnectivityManager.TYPE_MOBILE && isConnected) {
                    return AppEnum.TypeNetwork.MOBILE
                }
            }
        }
        return AppEnum.TypeNetwork.WIFI

    }
}