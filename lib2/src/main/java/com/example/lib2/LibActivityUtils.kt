package com.example.lib2

import android.app.Activity
import android.content.Intent

object LibActivityUtils {
    const val REQUEST_CODE_SECOND_ACTIVITY = 1001

    fun startSecondActivity(activity: Activity, listener: LibCallback) {
        val intent = Intent(activity, MainActivity3::class.java)
        activity.startActivityForResult(intent, REQUEST_CODE_SECOND_ACTIVITY)
        MainActivity3.setOnDataChangeListener(listener)
    }
}