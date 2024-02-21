package com.viettel.vht.sdk.utils

import android.app.Dialog
import androidx.fragment.app.Fragment
import com.vht.sdkcore.utils.Constants
import com.vht.sdkcore.utils.dialog.CommonAlertDialogNotification
import com.vht.sdkcore.utils.dialog.DialogType

fun Fragment.showCustomNotificationDialog(
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