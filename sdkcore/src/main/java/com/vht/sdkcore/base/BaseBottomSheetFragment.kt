package com.vht.sdkcore.base

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.vht.sdkcore.R
import com.vht.sdkcore.utils.Constants
import com.vht.sdkcore.utils.dialog.BaseDialog
import com.vht.sdkcore.utils.dialog.InputDialog

abstract class BaseBottomSheetFragment<BD : ViewDataBinding, VM : BaseViewModel> : BottomSheetDialogFragment() {

    private var _binding: BD? = null
    val binding get() = _binding!!
    private lateinit var viewModel: VM

    @get: LayoutRes
    abstract val layoutId: Int
    abstract fun getVM(): VM

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = getVM()
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = DataBindingUtil.inflate(inflater, layoutId, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()

        setOnClick()

        bindingStateView()

        bindingAction()
    }

    open fun setOnClick() {

    }

    open fun initView() {
        with(viewModel) {

            messageError.observe(viewLifecycleOwner) {
                var message = ""
                if (it is String) {
                    message = it
                } else {
                    if (it is Int) {
                        try {
                            message = getString(it)
                        } catch (e: Exception) {
                            //do nothing
                        }
                    }
                }
                if (!TextUtils.isEmpty(message)) {
                    showAlertDialog(message)
                }
            }

            isLoading.observe(viewLifecycleOwner) {
                showHideLoading(it)
            }

        }
    }

    open fun bindingStateView() {

    }

    open fun bindingAction() {

    }

    override fun onDestroyView() {
        binding.unbind()
        _binding = null
        super.onDestroyView()
    }


    fun showHideLoading(isShow: Boolean) {
        if (activity != null && activity is BaseActivity<*, *>) {
            if (isShow) {
                (activity as BaseActivity<*, *>?)!!.showLoading()
            } else {
                (activity as BaseActivity<*, *>?)!!.hiddenLoading()
            }
        }
    }

    fun showAlertDialog(message: String, onPositive: BaseDialog.OnDialogListener? = null) {
        BaseDialog(requireContext())
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton(R.string.ok, onPositive)
            .show()
    }

    fun showAlertDialog(@StringRes message: Int) {
        BaseDialog(requireContext())
            .setMessage(message)
            .setPositiveButton(R.string.ok, null)
            .show()
    }

    fun showConfirmDialog(
        @StringRes message: Int,
        onPositive: BaseDialog.OnDialogListener? = null,
        onNegative: BaseDialog.OnDialogListener? = null,
    ) {
        BaseDialog(requireContext())
            .setMessage(message)
            .setPositiveButton(R.string.ok, onPositive)
            .setNegativeButton(R.string.cancel, onNegative)
            .show()
    }

    fun showConfirmDialog(
        message: String,
        onPositive: BaseDialog.OnDialogListener? = null,
        onNegative: BaseDialog.OnDialogListener? = null,
    ) {
        BaseDialog(requireContext())
            .setMessage(message)
            .setPositiveButton(R.string.ok, onPositive)
            .setNegativeButton(R.string.cancel, onNegative)
            .show()
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

    protected fun showInputDialog(
        title: String,
        inputLabel: String,
        content: String? = null,
        maxLength: Int,
        titleBtnConfirm: Int = Constants.ERROR_NUMBER,
        titleBtnCancel: Int = Constants.ERROR_NUMBER,
        onPositiveClick: ((String) -> Unit)? = null,
        onNegativeClick: (() -> Unit)? = null
    ) {
        InputDialog.getInstanceInputDialog(requireContext())
            .showDialog()
            .setDialogTitleWithString(title)
            .setInputLabelWithString(inputLabel)
            .setContent(content)
            .setMaxInputLength(maxLength)
            .setTextNegativeButton(titleBtnCancel)
            .setTextPositiveButton(titleBtnConfirm)
            .setOnNegativePressed {
                onNegativeClick?.invoke()
            }
            .setOnPositivePressed { _, input ->
                onPositiveClick?.invoke(input)
            }

    }
}