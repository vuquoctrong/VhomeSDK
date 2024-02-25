package com.viettel.vht.sdk.utils

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date


fun Fragment.exitFullScreenMode() {
    activity?.apply {
        window.clearFlags(View.SYSTEM_UI_FLAG_FULLSCREEN)
        window.clearFlags(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
        window.clearFlags(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
        window.clearFlags(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION)
    }
}

fun Fragment.initFullscreenMode() {
    activity?.apply {
        window.addFlags(View.SYSTEM_UI_FLAG_FULLSCREEN)
        window.addFlags(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
        window.addFlags(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
        window.addFlags(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION)
        window.addFlags(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        window.addFlags(View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        //  window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
    }
}

fun Date.convertDateToStartDate(): Long {
    val string = SimpleDateFormat("dd-M-yyyy").format(this)
    return SimpleDateFormat("dd-M-yyyy").parse(string).time
}

fun Bitmap.toUri(context: Context, delete: Boolean = false): Uri? {
    val bytes = ByteArrayOutputStream()
    this.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
    val path: String? = MediaStore.Images.Media.insertImage(
        context.contentResolver,
        this,
        "IMG${System.currentTimeMillis()}",
        null
    )
    path?.let { it ->
        val data = Uri.parse(it)
        if (delete) {
            Uri.parse(it).deleteExternalStorage(context)
        }
        return data
    }
    return null
}

fun Uri.deleteExternalStorage(context: Context) {
    try {
        val check = context.contentResolver.delete(this, null, null)
        DebugConfig.log("delete", "success $check")
    } catch (e: SecurityException) {
        DebugConfig.log("delete", "error")
    }
}

fun View.hideKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(windowToken, 0)
}

fun View.disable() {
    isEnabled = false
    alpha = 0.5F
}

fun View.enable() {
    isEnabled = true
    alpha = 1F
}

fun Long.toDate(): String {
    val format = SimpleDateFormat("dd-MM-yyyy")
    return format.format(Date(this))
}

fun getMidnightCalendar(timeMillis: Long): Calendar {
    val strDate = SimpleDateFormat("dd-MM-yyyy").format(Date(timeMillis))
    val resultDate = SimpleDateFormat("dd-MM-yyyy hh:mm:ss").parse("$strDate 00:00:00")
    val calendar = Calendar.getInstance().apply {
        time = resultDate ?: Date()
    }
    return calendar
}