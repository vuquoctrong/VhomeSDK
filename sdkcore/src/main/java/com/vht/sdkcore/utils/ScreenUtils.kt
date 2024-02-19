package com.vht.sdkcore.utils

import android.content.Context
import android.content.res.Resources
import android.util.DisplayMetrics
import android.view.WindowManager
import kotlin.math.roundToInt


object ScreenUtils {

    fun getScreenRatio(context: Context): Float {
        val metrics = context.resources.displayMetrics
        return metrics.widthPixels.toFloat() / metrics.heightPixels.toFloat()
    }

    fun getScreenHeight(context: Context): Int {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val dm = DisplayMetrics()
        windowManager.defaultDisplay?.getMetrics(dm)
        return dm.heightPixels
    }

    fun getScreenWidth(context: Context?): Int {
        val windowManager = context?.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val dm = DisplayMetrics()
        windowManager.defaultDisplay?.getMetrics(dm)

        return dm.widthPixels
    }

    fun dpToPx(dp: Float): Int {
        val density = Resources.getSystem().displayMetrics.density
        return (dp * density).roundToInt()
    }

    fun pxToDp(px: Float): Float {
        val densityDpi = Resources.getSystem().displayMetrics.densityDpi.toFloat()
        return px / (densityDpi / 160f)
    }
}
