package com.vht.sdkcore.utils

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText

fun Activity.turnScreenOn() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
        setShowWhenLocked(true)
        setTurnScreenOn(true)
    }
    window.addFlags(
        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                or WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
                or WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
    )

//    with(getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            requestDismissKeyguard(this@turnScreenOnAndKeyguardOff, null)
//        }
//    }
}

fun Activity.turnScreenOff() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
        setShowWhenLocked(false)
        setTurnScreenOn(false)
    }
    window.clearFlags(
        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                or WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
                or WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
    )
}

fun Context.isNetworkConnected(): Boolean {
    val connectivityManager = getSystemService(ConnectivityManager::class.java)
    val caps = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
    return caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) ?: false
}

fun Activity?.setupUI(view: View) {
    //Set up touch listener for non-text box views to hide keyboard.
    if (view !is EditText) {
        view.setOnTouchListener { v: View?, event: MotionEvent? ->
            hideKeyboard()
            false
        }
    }

    //If a layout container, iterate over children and seed recursion.
    if (view is ViewGroup) {
        for (i in 0 until view.childCount) {
            val innerView = view.getChildAt(i)
            setupUI(innerView)
        }
    }
}

fun Activity?.setupUI(view: View, vararg viewException: View) {
    //Set up touch listener for non-text box views to hide keyboard.
    if (view !is EditText) {
        view.setOnTouchListener { v: View?, event: MotionEvent? ->
            hideKeyboard()
            false
        }
    }

    //If a layout container, iterate over children and seed recursion.
    if (view is ViewGroup) {
        for (i in 0 until view.childCount) {
            val innerView = view.getChildAt(i)
            for (aViewException in viewException) {
                if (aViewException.id != innerView.id) {
                    setupUI(innerView)
                }
            }
        }
    }
}

fun Activity?.hideKeyboard() {
    val imm = this?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(this.window.decorView.windowToken, 0)
}
