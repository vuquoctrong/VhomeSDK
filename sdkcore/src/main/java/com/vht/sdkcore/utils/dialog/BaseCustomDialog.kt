package com.vht.sdkcore.utils.dialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import com.vht.sdkcore.base.BaseActivity
import com.vht.sdkcore.utils.Constants

abstract class BaseCustomDialog<BD : ViewDataBinding>(@LayoutRes private val layoutRes: Int) : DialogFragment(layoutRes) {

    protected lateinit var binding: BD

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, layoutRes, container, false)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return binding.root
    }

    override fun onStart() {
        super.onStart()

        dialog?.window?.apply {
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }

    fun <T> doOnFragmentResult(key: String, action: (T) -> Unit) {
        findNavController().currentBackStackEntry?.savedStateHandle?.run {
            getLiveData<T>(key).observe(viewLifecycleOwner) {
                action.invoke(it)
                clearSavedStateProvider(key)
            }
        }
    }

    fun setFragmentResult(key: String, value: Any) {
        findNavController().previousBackStackEntry?.savedStateHandle?.set(key, value)
    }

    protected fun showDialogNotification(
        title: String,
        type: DialogType,
        message: Int = Constants.ERROR_NUMBER,
        titleBtnConfirm: Int = Constants.ERROR_NUMBER,
        negativeTitle: Int = Constants.ERROR_NUMBER,
        onNegativeClick: (() -> Unit)? = null,
        onPositiveClick: () -> Unit
    ) {
        CommonAlertDialogNotification.getInstanceCommonAlertdialog(requireContext())
            .showDialog()
            .setTextNegativeButton(negativeTitle)
            .setTextPositiveButton(titleBtnConfirm)
            .setContent(message)
            .showCenterImage(type)
            .setDialogTitleWithString(title)
            .setOnNegativePressed {
                onNegativeClick?.invoke()
                it.dismiss()
            }
            .setOnPositivePressed {
                it.dismiss()
                onPositiveClick.invoke()
            }
    }

    fun showHideLoading(isShow: Boolean) {
        if (activity != null && activity is BaseActivity<*, *>) {
            if (activity?.isFinishing == false) {
                if (isShow) {
                    (activity as BaseActivity<*, *>?)!!.showLoading()
                } else {
                    (activity as BaseActivity<*, *>?)!!.hiddenLoading()
                }
            }

        }
    }
}