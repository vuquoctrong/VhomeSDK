package com.viettel.vht.sdk.ui.quickhistory


import android.os.Bundle
import android.view.View
import com.vht.sdkcore.utils.dialog.BaseCustomDialog
import com.viettel.vht.sdk.R
import com.viettel.vht.sdk.databinding.DialogInputPasswordCameraJfBinding
import com.viettel.vht.sdk.utils.gone
import com.viettel.vht.sdk.utils.hideKeyboard
import com.viettel.vht.sdk.utils.visible
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class InputPasswordCameraDialog(
    val isShowSubTitle: Boolean = false,
    val textSubTitle: String = "",
    val callback: (String?) -> Unit
) :
    BaseCustomDialog<DialogInputPasswordCameraJfBinding>(R.layout.dialog_input_password_camera_jf) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showHideSubTitle(isShowSubTitle)
        if (textSubTitle.isNotBlank()) {
            binding.tvSubTitle.text = textSubTitle
        }
        binding.btnConfirm.setOnClickListener {
            callback.invoke(binding.edtPassWord.text.toString())
            binding.root.hideKeyboard()
            dismiss()
        }
        binding.btnCancel.setOnClickListener {
            callback.invoke(null)
            binding.root.hideKeyboard()
            dismiss()
        }
    }

    fun showHideSubTitle(isShow: Boolean) {
        if (isShow) {
            binding.tvSubTitle.visible()
        } else {
            binding.tvSubTitle.gone()
        }
    }

    fun setTextSubTitle(text: String) {
        binding.tvSubTitle.text = text
    }
}