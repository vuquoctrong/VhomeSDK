package com.vht.sdkcore.utils.dialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.vht.sdkcore.R
import com.vht.sdkcore.databinding.DialogInputPasswordBinding

class InputPasswordDialog : DialogFragment() {

    companion object {
        const val TAG = "InputPasswordDialog"
        fun newInstance(): InputPasswordDialog {
            return InputPasswordDialog()
        }
    }

    private var binding: DialogInputPasswordBinding? = null
    private var onPasswordListener: OnInputPasswordListener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.dialog_input_password, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.edtPassword?.requestFocus()

        binding?.btnOk?.setOnClickListener {
            val password = binding?.edtPassword?.text.toString().trim()
            if (TextUtils.isEmpty(password)) {
                Toast.makeText(
                    requireContext(),
                    "Password is empty",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                onPasswordListener?.onInputPassword(password)
            }
            dismiss()
        }
        binding?.btnCancel?.setOnClickListener {
            dismiss()
        }
    }

    fun setOnClickListener(onclickListener: OnInputPasswordListener) {
        this.onPasswordListener = onclickListener
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.setCancelable(false)
    }

    interface OnInputPasswordListener {
        fun onInputPassword(password: String)
    }
}