package com.vht.sdkcore.utils

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.lifecycleScope
import com.vht.sdkcore.R
import com.vht.sdkcore.databinding.LayoutToastComonBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*

fun Context.isNetworkAvailable(): Boolean {
    val manager = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val connection = manager.activeNetworkInfo
    return connection != null && connection.isConnectedOrConnecting
}

fun Context.isWifiAvailable(): Boolean {
    val connManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
    val wifi = connManager!!.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
    return wifi!!.isConnected
}

fun Context.isNetworkOnline(): Boolean {
    var isOnline = false
    try {
        val manager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities =
            manager.getNetworkCapabilities(manager.activeNetwork) // need ACCESS_NETWORK_STATE permission
        isOnline =
            capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    } catch (e: java.lang.Exception) {
        e.printStackTrace()
    }
    return isOnline
}

fun Fragment.showCommonCustomToast(
    resTitle: Int = Constants.ERROR_NUMBER,
    icon: Int = R.drawable.ic_success_dialog,
    onFinish: (suspend () -> Unit)? = null,
    showImage: Boolean = false
): Dialog {
    return Dialog(requireContext(), R.style.ThemeDialog).apply {
        lifecycleScope.launch {
            setCancelable(false)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setCanceledOnTouchOutside(false)
            val binding = LayoutToastComonBinding.inflate(LayoutInflater.from(requireContext()))
            setContentView(binding.root)
            if (showImage) {
                binding.imvIcon.visible()
            } else {
                binding.imvIcon.gone()
            }
            binding.imvIcon.setImageResource(icon)
            binding.tvTitle.text = getString(resTitle)
            lifecycle.addObserver(object : LifecycleObserver {
                @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
                fun onDestroy() {
                    dismiss()
                }
            })
            show()
            delay(3000)
            withContext(Dispatchers.Main) {
                onFinish?.invoke()
                dismiss()
            }
        }
    }
}
fun  Int.getPermissionEzViz(): List<Int>? {
    val numbers = listOf(PermissionShareCameraEzViz.SHARE_LIVE.share, PermissionShareCameraEzViz.SHARE_PLAYBACK.share, PermissionShareCameraEzViz.SHARE_EVENT.share, PermissionShareCameraEzViz.SHARE_AUDIO.share, PermissionShareCameraEzViz.SHARE_PTZ.share)
    var result: List<Int>? = null

    // Thuật toán vét cạn
    fun backtrack(remainingSum: Int, currentCombination: List<Int>, startIndex: Int) {
        if (remainingSum == 0) {
            result = currentCombination
            return
        } else if (remainingSum < 0 || startIndex >= numbers.size) {
            return
        } else {
            for (i in startIndex until numbers.size) {
                val newCombination = currentCombination + numbers[i]
                backtrack(remainingSum - numbers[i], newCombination, i + 1)
                if (result != null) {
                    return
                }
            }
        }
    }

    // Gọi hàm backtrack để tìm tổ hợp duy nhất
    backtrack(this, emptyList(), 0)

    return result
}

fun TextView.setGradientColor(@ColorRes startColor: Int, @ColorRes endColor: Int) {
    val start = resources.getColor(startColor, null)
    val end = resources.getColor(endColor, null)
    paint.shader =
        LinearGradient(0f, 0f, width.toFloat(), textSize, start, end, Shader.TileMode.CLAMP)
}

fun View.hideKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(windowToken, 0)
}

fun View.showKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT);
}

@RequiresApi(Build.VERSION_CODES.O)
fun Long.getDateTime(): String? {
    return try {
        val timeZoneOffset = "+07:00"
        val dateTime = convertTimestampToDateTime(this, timeZoneOffset)
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        dateTime.format(formatter)
    } catch (e: Exception) {
        e.toString()
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun convertTimestampToDateTime(timestamp: Long, timeZoneOffset: String): LocalDateTime {
    val zoneOffset = ZoneOffset.of(timeZoneOffset)
    val instant = java.time.Instant.ofEpochSecond(timestamp)
    val dateTime = LocalDateTime.ofInstant(instant, ZoneId.of("GMT")).atOffset(zoneOffset).toLocalDateTime()
    return dateTime
}

enum class PermissionShareCameraEzViz(var share: Int){
    SHARE_LIVE(1),
    SHARE_PLAYBACK(2),
    SHARE_EVENT(4),
    SHARE_AUDIO(8),
    SHARE_PTZ(256)
}