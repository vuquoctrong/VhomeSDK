package com.viettel.vht.sdk.ui.jfcameradetail.settingwifi

import android.content.DialogInterface
import android.os.Bundle
import android.text.InputType
import android.view.View
import androidx.lifecycle.MutableLiveData
import com.vht.sdkcore.utils.dialog.BaseCustomDialog
import com.viettel.vht.sdk.R
import com.viettel.vht.sdk.databinding.DialogConnectWifiCameraJfBinding
import com.viettel.vht.sdk.utils.hideKeyboard
import com.viettel.vht.sdk.utils.onTextChanged
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ConnectWifiCameraJFDialog(
    private val wifiName: String? = null,
    private val callback: (String?, String?) -> Unit
) : BaseCustomDialog<DialogConnectWifiCameraJfBinding>(R.layout.dialog_connect_wifi_camera_jf) {

    private val isShowPassword = MutableLiveData(false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        setOnClick()
        bindingStateView()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        activity?.currentFocus?.hideKeyboard()
    }

    private fun initView() {
        isCancelable = false
        wifiName?.let {
            binding.inputNameWifi.setText(it)
            binding.inputNameWifi.isEnabled = false
        } ?: run {
            binding.inputNameWifi.isEnabled = true
        }
        checkEnableButton()
    }

    private fun setOnClick() {
        binding.ivShowPassword.setOnClickListener {
            isShowPassword.value = !(isShowPassword.value ?: false)
        }
        binding.btnConfirm.setOnClickListener {
            callback.invoke(
                binding.inputNameWifi.text.toString(),
                binding.inputPassword.text.toString()
            )
            binding.root.hideKeyboard()
            dismiss()
        }
        binding.btnCancel.setOnClickListener {
            binding.root.hideKeyboard()
            dismiss()
        }
        binding.inputNameWifi.onTextChanged {
            checkEnableButton()
        }
    }

    private fun bindingStateView() {
        isShowPassword.observe(viewLifecycleOwner) {
            if (it) {
                binding.ivShowPassword.setImageResource(R.drawable.ic_eye_show_password)
                binding.inputPassword.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            } else {
                binding.ivShowPassword.setImageResource(R.drawable.ic_eye_hide_password)
                binding.inputPassword.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
        }
    }

    private fun checkEnableButton() {
        val isEnable = !binding.inputNameWifi.text.isNullOrEmpty()
        binding.btnConfirm.isEnabled = isEnable
        binding.btnConfirm.setBackgroundResource(
            if (isEnable) R.drawable.bg_button_setting_submit
            else R.drawable.bg_button_setting_disable
        )
    }

    companion object {
        const val TAG = "ConnectWifiCameraJFDialog"
    }
}
