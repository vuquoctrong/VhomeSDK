@file:Suppress("ImplicitThis")

package com.viettel.vht.sdk.utils

import android.util.Log
import com.viettel.vht.sdk.BuildConfig


object DebugConfig {
    //region Config
    val SHOW_DEBUG_LOG by lazy {
        BuildConfig.DEBUG
    }
    val SHOW_ALL_DEBUG_LOG = true
    //endregion Config


    /**
     * Log Message With Custom TAG
     */
    fun log(TAG:String, message: String){
        val str = " $message"
            if(SHOW_DEBUG_LOG){
                if(SHOW_ALL_DEBUG_LOG){
                    Log.i(TAG, str)
                }else{
                    Log.d(TAG, str)
                }
            }
        }

    /**
     * Log error with method tag
     */
    fun loge(message: String){
        val str = Thread.currentThread().stackTrace
        var tag = if(str.size > 3){
            str[3].className +" "+str[3].methodName
        }else{
            BuildConfig.LIBRARY_PACKAGE_NAME
        }
        loge(tag,message)
    }

    //region Log
    /**
     * Log Message With Method TAG
     */
    fun log(message:String){
        if(SHOW_DEBUG_LOG){
            val str = Thread.currentThread().stackTrace
            var tag = if(str.size > 3){
                str[3].className +" "+str[3].methodName
            }else{
                BuildConfig.LIBRARY_PACKAGE_NAME
            }
            log(tag,message)
        }
    }



    /**
     * Log error with custom tag
     */
    fun loge(tag:String, message: String){
        Log.e(tag,message)
    }

    //endregion Log
}