package com.vht.sdkcore.utils

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.lifecycleScope
import com.vht.sdkcore.R
import com.vht.sdkcore.databinding.LayoutCustomToastAndroidTvBinding
import com.vht.sdkcore.databinding.LayoutCustomToastBinding
import com.vht.sdkcore.databinding.LayoutToastBinding
import com.vht.sdkcore.utils.dialog.CommonAlertDialogNotification
import com.vht.sdkcore.utils.dialog.DialogType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

inline suspend fun executeWithRetry(
    retries: Int = 1,
    delayMillis: Long = 100L,
    callIfNeedRetry: () -> Boolean
) {
    var countRetry = 0
    while (countRetry <= retries) {
        try {
            delay(delayMillis)
            val needRetry = callIfNeedRetry()
            Log.d("TAG_RETRY", "executeWithRetry: countRetry = $countRetry")
            if (needRetry) {
                countRetry++
            } else {
                break
            }
        } catch (e: Exception) {
            Log.d("TAG_RETRY", "throw Exception countRetry = $countRetry")
            if (countRetry <= retries) {
                countRetry++
            }
        }
    }
}

fun Fragment.showDialogErrorInternet(
    title: String,
    type: DialogType,
    message: Int = Constants.ERROR_NUMBER,
    titleBtnConfirm: Int = Constants.ERROR_NUMBER,
    negativeTitle: Int = Constants.ERROR_NUMBER,
    onPositiveClick: () -> Unit
): Dialog {
    return CommonAlertDialogNotification.getInstanceCommonAlertdialog(requireContext())
        .showDialog()
        .setTextNegativeButton(negativeTitle)
        .setTextPositiveButton(titleBtnConfirm)
        .setContent(message)
        .showCenterImage(type)
        .setDialogTitleWithString(title)
        .setOnNegativePressed {
            it.dismiss()
        }
        .setOnPositivePressed {
            it.dismiss()
            onPositiveClick.invoke()
        }
}

fun Fragment.showCustomToast(
    title: String,
    isSuccess: Boolean = false,
    onFinish: (suspend () -> Unit)? = null,
): Dialog {
    return Dialog(requireContext(), R.style.ThemeDialog).apply {
        lifecycleScope.launch {
            setCancelable(false)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setCanceledOnTouchOutside(false)
            val binding = LayoutCustomToastBinding.inflate(LayoutInflater.from(requireContext()))
            setContentView(binding.root)
            binding.tvTitle.text = title
            binding.imvIcon.visible()
            if (isSuccess) {
                binding.imvIcon.setImageResource(R.drawable.ic_success_dialog)
            } else {
                binding.imvIcon.setImageResource(R.drawable.ic_show_toast_error)
            }
            lifecycle.addObserver(object : LifecycleObserver {
                @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
                fun onDestroy() {
                    dismiss()
                }
            })
            show()
            delay(2000)
            withContext(Dispatchers.Main) {
                onFinish?.invoke()
                dismiss()
            }
        }
    }
}

fun Fragment.showCustomToast(
    resTitle: Int = R.string.string_success_setting_time_zone,
    resTitleString:  String = "",
    icon: Int = R.drawable.ic_show_toast_success,
    onFinish: (suspend () -> Unit)? = null,
    showImage: Boolean = false
): Dialog {
    return Dialog(requireContext(), R.style.ThemeDialog).apply {
        lifecycleScope.launch {
            setCancelable(false)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setCanceledOnTouchOutside(false)
            val binding = LayoutToastBinding.inflate(LayoutInflater.from(requireContext()))
            setContentView(binding.root)
            if (showImage) {
                binding.imvIcon.visible()
            } else {
                binding.imvIcon.gone()
            }
            binding.imvIcon.setImageResource(icon)
            binding.tvTitle.text = getString(resTitle)
            if(resTitleString.isNotEmpty()){
                binding.tvTitle.text =resTitleString
            }
            lifecycle.addObserver(object : LifecycleObserver {
                @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
                fun onDestroy() {
                    dismiss()
                }
            })
            show()
            delay(2000)
            withContext(Dispatchers.Main) {
                onFinish?.invoke()
                dismiss()
            }
        }
    }
}


fun Fragment.showCustomToastForAndroidTV(
    title: String,
    isSuccess: Boolean = false,
    onFinish: (suspend () -> Unit)? = null,
): Dialog {
    return Dialog(requireContext(), R.style.ThemeDialog).apply {
        lifecycleScope.launch {
            setCancelable(false)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setCanceledOnTouchOutside(false)
            val binding = LayoutCustomToastAndroidTvBinding.inflate(LayoutInflater.from(requireContext()))
            setContentView(binding.root)
            binding.tvTitle.text = title
            binding.imvIcon.visible()
            if (isSuccess) {
                binding.imvIcon.setImageResource(R.drawable.ic_dialog_success)
            } else {
                binding.imvIcon.setImageResource(R.drawable.ic_dialog_error)
            }
            lifecycle.addObserver(object : LifecycleObserver {
                @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
                fun onDestroy() {
                    dismiss()
                }
            })
            show()
            delay(2000)
            withContext(Dispatchers.Main) {
                onFinish?.invoke()
                dismiss()
            }
        }
    }
}

fun Context.dpToPx(dp: Int): Int {
    return (dp * resources.displayMetrics.density).toInt()
}

fun Context.pxToDp(px: Int): Int {
    return (px / resources.displayMetrics.density).toInt()
}




