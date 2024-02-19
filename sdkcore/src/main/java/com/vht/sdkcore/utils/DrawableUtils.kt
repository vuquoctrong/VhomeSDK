package com.vht.sdkcore.utils

import android.content.Context
import android.graphics.drawable.Drawable
import android.graphics.drawable.InsetDrawable
import androidx.core.content.ContextCompat
import com.vht.sdkcore.R


object DrawableUtils {

    fun getThreeDots(context: Context): Drawable {
        val drawable: Drawable? = ContextCompat.getDrawable(context, R.drawable.ic_camera_online)
        //Add padding to too large icon
        return InsetDrawable(drawable, 100, 0, 100, 0)
    }


}