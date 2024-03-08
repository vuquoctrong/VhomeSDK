package com.viettel.vht.sdk.ui.jfcameradetail.forgot_password

import android.text.InputFilter
import android.text.InputType
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.vht.sdkcore.base.BaseFragment
import com.vht.sdkcore.network.Status
import com.vht.sdkcore.utils.Define
import com.vht.sdkcore.utils.dialog.CommonAlertDialogNotification
import com.vht.sdkcore.utils.dialog.DialogType
import com.viettel.vht.sdk.R
import com.viettel.vht.sdk.databinding.FragmentResetPasswordCameraJfBinding
import com.viettel.vht.sdk.navigation.AppNavigation
import com.viettel.vht.sdk.ui.jfcameradetail.forgot_password.RetrievePasswordCameraJFViewModel.Companion.ERROR_CONFIRM_PASSWORD
import com.viettel.vht.sdk.ui.jfcameradetail.forgot_password.RetrievePasswordCameraJFViewModel.Companion.ERROR_NEW_PASSWORD
import com.viettel.vht.sdk.utils.gone
import com.viettel.vht.sdk.utils.onTextChanged
import com.viettel.vht.sdk.utils.visible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import javax.inject.Inject


@AndroidEntryPoint
class ResetPasswordCameraJFFragment :
    BaseFragment<FragmentResetPasswordCameraJfBinding, RetrievePasswordCameraJFViewModel>() {

    @Inject
    lateinit var appNavigation: AppNavigation

    private val viewModel: RetrievePasswordCameraJFViewModel by viewModels()

    override val layoutId = R.layout.fragment_reset_password_camera_jf

    override fun getVM() = viewModel

    override fun initView() {
        super.initView()
        arguments?.let {
            viewModel.devId = it.getString(Define.BUNDLE_KEY.PARAM_MODEL_CAMERA) ?: ""
        }
        checkStateButton()
        val filter = InputFilter { source, start, end, _, _, _ ->
            for (i in start until end) {
                if (Character.isWhitespace(source[i])) {
                    return@InputFilter ""
                }
            }
            null
        }
        binding.edtNewPassword.filters = arrayOf(filter)
        binding.edtConfirmPassword.filters = arrayOf(filter)
    }

    override fun setOnClick() {
        super.setOnClick()
        binding.toolBar.setOnLeftClickListener {
            onBackPressed()
        }
        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    onBackPressed()
                }
            }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
        binding.edtNewPassword.onTextChanged {
            checkStateButton()
        }
        binding.edtConfirmPassword.onTextChanged {
            checkStateButton()
        }
        binding.ivShowNewPassword.setOnClickListener {
            viewModel.isShowNewPassword.value = !(viewModel.isShowNewPassword.value ?: false)
        }
        binding.ivShowConfirmPassword.setOnClickListener {
            viewModel.isShowConfirmPassword.value =
                !(viewModel.isShowConfirmPassword.value ?: false)
        }
        binding.btnChangePassword.setOnClickListener {
            viewModel.saveNewPassword(
                binding.edtNewPassword.text?.trim().toString(),
                binding.edtConfirmPassword.text?.trim().toString()
            )
        }
    }

    private fun onBackPressed() {
        CommonAlertDialogNotification.getInstanceCommonAlertdialog(requireContext())
            .showDialog()
            .setDialogTitleWithString("Xác nhận không thiết lập mật khẩu mã hóa?")
            .setContent("Nếu mật khẩu mã hóa không được thiết lập, mật khẩu của bạn sẽ là trống.")
            .setTextPositiveButtonWithString("XÁC NHẬN")
            .setTextNegativeButtonWithString("HỦY")
            .showCenterImage(DialogType.CONFIRM)
            .setOnNegativePressed { dialog ->
                dialog.dismiss()
            }
            .setOnPositivePressed { dialog ->
                dialog.dismiss()
                viewModel.saveNewPassword("", "")
            }
    }

    override fun bindingStateView() {
        super.bindingStateView()
        viewModel.isShowNewPassword.observe(viewLifecycleOwner) {
            if (it) {
                binding.ivShowNewPassword.setImageResource(R.drawable.ic_eye_show_password)
                binding.edtNewPassword.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            } else {
                binding.ivShowNewPassword.setImageResource(R.drawable.ic_eye_hide_password)
                binding.edtNewPassword.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
        }
        viewModel.isShowConfirmPassword.observe(viewLifecycleOwner) {
            if (it) {
                binding.ivShowConfirmPassword.setImageResource(R.drawable.ic_eye_show_password)
                binding.edtConfirmPassword.inputType =
                    InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            } else {
                binding.ivShowConfirmPassword.setImageResource(R.drawable.ic_eye_hide_password)
                binding.edtConfirmPassword.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
        }
        viewModel.setNewPasswordStatus.observe(viewLifecycleOwner) {
            when (it.status) {
                Status.LOADING -> Unit

                Status.SUCCESS -> {
                    if (it.data.toString().isEmpty()) {
                        appNavigation.navigateUp()
                    } else {
                        CommonAlertDialogNotification
                            .getInstanceCommonAlertdialog(requireContext())
                            .showDialog()
                            .showCenterImage(DialogType.SUCCESS)
                            .setDialogTitleWithString("Đặt lại mật khẩu mã hóa thành công")
                            .hideOptionButton()
                        lifecycleScope.launchWhenStarted {
                            delay(3000)
                            CommonAlertDialogNotification
                                .getInstanceCommonAlertdialog(requireContext())
                                .dismiss()
                            appNavigation.navigateUp()
                        }
                    }
                }

                Status.ERROR -> {
                    when (it.exception) {
                        ERROR_NEW_PASSWORD -> {
                            binding.tvErrorNewPassword.visible()
                        }

                        ERROR_CONFIRM_PASSWORD -> {
                            binding.tvErrorConfirmPassword.visible()
                        }

                        else -> {
                            CommonAlertDialogNotification.getInstanceCommonAlertdialog(
                                requireContext()
                            )
                                .showDialog()
                                .setDialogTitleWithString("Đặt lại mật khẩu mã hóa không thành công")
                                .setTextPositiveButtonWithString("OK")
                                .showCenterImage(DialogType.NOTIFICATION)
                                .hideOptionButton()
                                .showPositiveButton()
                                .setOnPositivePressed { dialog ->
                                    dialog.dismiss()
                                }
                        }
                    }
                }
            }
        }
    }

    private fun checkStateButton() {
        binding.tvErrorNewPassword.gone()
        binding.tvErrorConfirmPassword.gone()
        if (binding.edtNewPassword.text.toString().isEmpty()
            || binding.edtConfirmPassword.text.toString().isEmpty()
        ) {
            binding.btnChangePassword.isEnabled = false
            binding.btnChangePassword.setBackgroundResource(R.drawable.bg_button_setting_disable)
        } else {
            binding.btnChangePassword.isEnabled = true
            binding.btnChangePassword.setBackgroundResource(R.drawable.bg_button_setting_submit)
        }
    }
}
