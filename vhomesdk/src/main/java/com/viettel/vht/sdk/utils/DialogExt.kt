package com.viettel.vht.sdk.utils

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.Editable
import android.text.Selection
import android.text.TextWatcher
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.vht.sdkcore.utils.Constants
import com.vht.sdkcore.utils.dialog.CommonAlertDialogNotification
import com.vht.sdkcore.utils.dialog.DialogType
import com.viettel.vht.sdk.R
import com.viettel.vht.sdk.databinding.DialogAddPresetBinding
import com.viettel.vht.sdk.databinding.DialogChangeNameCameraJfBinding
import com.viettel.vht.sdk.databinding.DialogGuildePresetBinding

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

fun Fragment.showCustomNotificationDialogPrivate(
    title: String,
    type: DialogType,
    message: Int = Constants.ERROR_NUMBER,
    titleBtnConfirm: Int = Constants.ERROR_NUMBER,
    negativeTitle: Int = Constants.ERROR_NUMBER,
    onPositiveClick: () -> Unit,
    onNegativeClick: () -> Unit
): Dialog {
    return CommonAlertDialogNotification.getInstanceCommonAlertdialog(requireContext())
        .showDialog()
        .setTextNegativeButton(negativeTitle)
        .setTextPositiveButton(titleBtnConfirm)
        .setContent(message)
        .showCenterImage(type)
        .setDialogTitleWithString(title)
        .setOnPositivePressed {
            it.dismiss()
            onPositiveClick.invoke()
        }
        .setOnNegativePressed {
            it.dismiss()
            onNegativeClick.invoke()
        }
}
fun Fragment.showDialogGuildePreset(
    textInput: String,
): Dialog {
    return Dialog(requireContext(), R.style.ThemeDialog).apply {
        setCancelable(true)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setCanceledOnTouchOutside(false)
        val binding =
            DialogGuildePresetBinding.inflate(LayoutInflater.from(requireContext()))
        setContentView(binding.root)

        binding.tvMessage.setText(textInput)

        binding.btnOK.setOnClickListener {
            binding.root.hideKeyboard()
            dismiss()
        }
        lifecycle.addObserver(object : LifecycleObserver {
            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            fun onDestroy() {
                dismiss()
            }
        })
        this.show()
    }
}


@SuppressLint("SetTextI18n")
fun Fragment.showDialogAddPreset(
    textInput: String = "",
    positiveListener: (String) -> Unit,
    negativeListener: (() -> Unit)? = null,
): Dialog {
    return Dialog(requireContext(), R.style.ThemeDialog).apply {
        setCancelable(true)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setCanceledOnTouchOutside(false)
        val binding =
            DialogAddPresetBinding.inflate(LayoutInflater.from(requireContext()))
        setContentView(binding.root)


        binding.edtInput.setText(textInput)
        binding.btnOK.disable()
        Selection.setSelection(binding.edtInput.text, binding.edtInput.length())
        binding.tvNumberEdt.text =  binding.edtInput.length().toString()+"/30"
        binding.edtInput.afterTextChanged {
            binding.tvNumberEdt.text = "${it.length}/30"
        }

        binding.btnCancel.setOnClickListener {
            negativeListener?.invoke()
            binding.root.hideKeyboard()
            dismiss()
        }
        binding.clMain.setOnClickListener {
            binding.root.hideKeyboard()
        }
        binding.btnClear.setOnClickListener {
            binding.edtInput.setText("")
        }


        binding.btnOK.setOnClickListener {
            positiveListener.invoke(binding.edtInput.text.toString().trim())
            binding.root.hideKeyboard()
            dismiss()
        }

        binding.edtInput.addTextChangedListener(object :TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (p0.toString().trim().isEmpty()) {
                    binding.btnClear.gone()
                } else {
                    binding.btnClear.visible()
                }
            }

            override fun afterTextChanged(p0: Editable?) {
                if (p0.toString().trim().isEmpty()) {
                    binding.tvError.visible()
                    binding.btnOK.disable()
                } else {
                    binding.tvError.gone()
                    binding.btnOK.enable()
                }
            }

        })

        lifecycle.addObserver(object : LifecycleObserver {
            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            fun onDestroy() {
                dismiss()
            }
        })
        this.show()
    }
}

fun Fragment.showDialogEditPreset(
    textInput: String = "",
    positiveListener: (String) -> Unit,
    negativeListener: (() -> Unit)? = null,
): Dialog {
    return Dialog(requireContext(), R.style.ThemeDialog).apply {
        setCancelable(true)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setCanceledOnTouchOutside(false)
        val binding =
            DialogAddPresetBinding.inflate(LayoutInflater.from(requireContext()))
        setContentView(binding.root)


        binding.edtInput.setText(textInput)
        Selection.setSelection(binding.edtInput.text, binding.edtInput.length())
        binding.tvTitle.text =  "Đổi tên góc quay"
        binding.tvNumberEdt.text =  binding.edtInput.length().toString()+"/30"
        binding.edtInput.afterTextChanged {
            binding.tvNumberEdt.text = "${it.length}/30"
        }

        binding.btnCancel.setOnClickListener {
            negativeListener?.invoke()
            binding.root.hideKeyboard()
            dismiss()
        }
        binding.clMain.setOnClickListener {
            binding.root.hideKeyboard()
        }
        binding.btnClear.setOnClickListener {
            binding.edtInput.setText("")
        }


        binding.btnOK.setOnClickListener {
            positiveListener.invoke(binding.edtInput.text.toString().trim())
            binding.root.hideKeyboard()
            dismiss()
        }

        binding.edtInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (p0.toString().trim().isEmpty()) {
                    binding.btnClear.gone()
                } else {
                    binding.btnClear.visible()
                }
            }

            override fun afterTextChanged(p0: Editable?) {
                if (p0.toString().trim().isEmpty()) {
                    binding.tvError.visible()
                    binding.btnOK.isEnabled = false
                } else {
                    binding.tvError.gone()
                    binding.btnOK.isEnabled = true
                }
            }

        })

        lifecycle.addObserver(object : LifecycleObserver {
            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            fun onDestroy() {
                dismiss()
            }
        })
        this.show()
    }


}


@SuppressLint("SetTextI18n")
fun Fragment.showDialogEditNameCameraJF(
    textInput: String,
    positiveListener: (String) -> Unit,
    negativeListener: (() -> Unit)? = null,
): Dialog {
    return Dialog(requireContext(), R.style.ThemeDialog).apply {
        setCancelable(true)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setCanceledOnTouchOutside(false)
        val binding =
            DialogChangeNameCameraJfBinding.inflate(LayoutInflater.from(requireContext()))
        setContentView(binding.root)


        binding.edtInput.setText(textInput)
        Selection.setSelection(binding.edtInput.text, binding.edtInput.length())
        binding.tvNumberEdt.text =  binding.edtInput.length().toString()+"/30"
        binding.edtInput.afterTextChanged {
            binding.tvNumberEdt.text = "${it.length}/30"
        }

        binding.btnCancel.setOnClickListener {
            negativeListener?.invoke()
            binding.root.hideKeyboard()
            dismiss()
        }
        binding.clMain.setOnClickListener {
            binding.root.hideKeyboard()
        }


        binding.btnOK.setOnClickListener {
            positiveListener.invoke(binding.edtInput.text.toString().trim())
            binding.root.hideKeyboard()
            dismiss()
        }

        binding.edtInput.afterTextChanged {
            if (it.trim().isEmpty()) {
                binding.tvError.visible()
                binding.btnOK.isEnabled = false
            } else {
                binding.tvError.gone()
                binding.btnOK.isEnabled = true
            }
        }

        lifecycle.addObserver(object : LifecycleObserver {
            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            fun onDestroy() {
                dismiss()
            }
        })
        this.show()
    }
}