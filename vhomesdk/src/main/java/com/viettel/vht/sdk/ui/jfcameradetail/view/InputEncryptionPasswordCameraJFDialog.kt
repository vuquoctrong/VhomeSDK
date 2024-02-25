package com.viettel.vht.sdk.ui.jfcameradetail.view

import android.content.DialogInterface
import android.graphics.Paint
import android.os.Bundle
import android.text.InputType
import android.view.View
import androidx.lifecycle.MutableLiveData
import com.vht.sdkcore.utils.dialog.BaseCustomDialog
import com.viettel.vht.sdk.R
import com.viettel.vht.sdk.databinding.DialogInputEncryptionPasswordCameraJfBinding
import com.viettel.vht.sdk.utils.gone
import com.viettel.vht.sdk.utils.hideKeyboard
import com.viettel.vht.sdk.utils.visible
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class InputEncryptionPasswordCameraJFDialog(
    private val isShareCamera: Boolean = false,
    private val isShowSubTitle: Boolean = false,
    private val forgotPasswordCallback: () -> Unit,
    private val callback: (String?) -> Unit
) : BaseCustomDialog<DialogInputEncryptionPasswordCameraJfBinding>(R.layout.dialog_input_encryption_password_camera_jf) {

    private val isShowPassword = MutableLiveData(false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.tvForgotPassword.paintFlags = Paint.UNDERLINE_TEXT_FLAG
        binding.ivShowPassword.setOnClickListener {
            isShowPassword.value = !(isShowPassword.value ?: false)
        }
        showHideSubTitle(isShowSubTitle)
        showHideButtonForgotPassword(isShareCamera)
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
        binding.tvForgotPassword.setOnClickListener {
            forgotPasswordCallback.invoke()
            binding.root.hideKeyboard()
            dismiss()
        }
        isShowPassword.observe(viewLifecycleOwner) {
            if (it) {
                binding.ivShowPassword.setImageResource(R.drawable.ic_eye_show_password)
                binding.edtPassWord.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            } else {
                binding.ivShowPassword.setImageResource(R.drawable.ic_eye_hide_password)
                binding.edtPassWord.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
        }

    }

    private fun showHideButtonForgotPassword(isShow: Boolean) {
        if (isShow) {
            binding.tvForgotPassword.gone()
        } else {
            binding.tvForgotPassword.visible()
        }
    }

    private fun showHideSubTitle(isShow: Boolean) {
        if (isShow) {
            binding.tvSubTitle.visible()
        } else {
            binding.tvSubTitle.gone()
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        activity?.currentFocus?.hideKeyboard()
    }
}