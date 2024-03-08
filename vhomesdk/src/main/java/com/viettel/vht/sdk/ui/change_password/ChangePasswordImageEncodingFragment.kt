package com.viettel.vht.sdk.ui.change_password

import android.graphics.Paint
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.vht.sdkcore.base.BaseFragment
import com.vht.sdkcore.network.Status
import com.vht.sdkcore.utils.Define
import com.vht.sdkcore.utils.dialog.CommonAlertDialogNotification
import com.vht.sdkcore.utils.dialog.DialogType
import com.vht.sdkcore.utils.isNetworkAvailable

import com.viettel.vht.sdk.ui.change_password.ChangePasswordImageEncodingJFViewModel.Companion.ERROR_CONFIRM_PASSWORD
import com.viettel.vht.sdk.ui.change_password.ChangePasswordImageEncodingJFViewModel.Companion.ERROR_NEW_PASSWORD
import com.viettel.vht.sdk.ui.change_password.ChangePasswordImageEncodingJFViewModel.Companion.ERROR_OLD_PASSWORD
import com.viettel.vht.sdk.ui.change_password.ChangePasswordImageEncodingJFViewModel.Companion.ERROR_UNKNOWN
import com.viettel.vht.sdk.ui.change_password.ChangePasswordImageEncodingJFViewModel.Companion.SUCCESS
import com.viettel.vht.sdk.R
import com.viettel.vht.sdk.databinding.FragmentChangePasswordImageEncodingJfBinding
import com.viettel.vht.sdk.navigation.AppNavigation
import com.viettel.vht.sdk.ui.jfcameradetail.forgot_password.RetrievePasswordCameraJFViewModel
import com.viettel.vht.sdk.utils.gone
import com.viettel.vht.sdk.utils.showCustomNotificationDialog
import com.viettel.vht.sdk.utils.visible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import javax.inject.Inject

@AndroidEntryPoint
class ChangePasswordImageEncodingFragment :
    BaseFragment<FragmentChangePasswordImageEncodingJfBinding, ChangePasswordImageEncodingJFViewModel>() {

    @Inject
    lateinit var appNavigation: AppNavigation

    private val viewModel: ChangePasswordImageEncodingJFViewModel by viewModels()
    private val retrievePasswordViewModel: RetrievePasswordCameraJFViewModel by viewModels()

    override val layoutId = R.layout.fragment_change_password_image_encoding_jf

    override fun getVM() = viewModel

    override fun initView() {
        super.initView()
        arguments.takeIf { it?.containsKey(Define.BUNDLE_KEY.PARAM_ID) == true }?.let {
            viewModel.devId = it.getString(Define.BUNDLE_KEY.PARAM_ID) ?: ""
            retrievePasswordViewModel.devId = it.getString(Define.BUNDLE_KEY.PARAM_ID) ?: ""
        }
        binding.btnForgotPassword.paintFlags = Paint.UNDERLINE_TEXT_FLAG
        checkStateButton()
    }

    override fun setOnClick() {
        super.setOnClick()
        binding.toolBar.setOnLeftClickListener {
            appNavigation.navigateUp()
        }
        binding.edtOldPassword.handleEdittextOnTextChange {
            checkStateButton()
        }
        binding.edtNewPassword.handleEdittextOnTextChange {
            checkStateButton()
        }
        binding.edtConfirmPassword.handleEdittextOnTextChange {
            checkStateButton()
        }

        binding.btnChangePassword.setOnClickListener {
            if (!binding.btnChangePassword.isEnabled) {
                return@setOnClickListener
            }
            if (context?.isNetworkAvailable() == true) {
                viewModel.saveNewPassword(
                    binding.edtOldPassword.getTextEditText(),
                    binding.edtNewPassword.getTextEditText(),
                    binding.edtConfirmPassword.getTextEditText()
                )
            } else {
                showCustomNotificationDialog(
                    title = getString(com.vht.sdkcore.R.string.no_connection),
                    type = DialogType.ERROR,
                    titleBtnConfirm = com.vht.sdkcore.R.string.text_close
                ) {}
            }
        }
        binding.btnForgotPassword.setOnClickListener {
            retrievePasswordViewModel.requestOTP()
        }
    }

    override fun bindingStateView() {
        super.bindingStateView()
        viewModel.state.observe(viewLifecycleOwner) {
            when (it) {
                ERROR_OLD_PASSWORD -> {
                    binding.tvErrorOldPassword.visible()
                }

                ERROR_NEW_PASSWORD -> {
                    binding.tvErrorNewPassword.visible()
                }

                ERROR_CONFIRM_PASSWORD -> {
                    binding.tvErrorConfirmPassword.visible()
                }

                ERROR_UNKNOWN -> {
                    CommonAlertDialogNotification.getInstanceCommonAlertdialog(requireContext())
                        .showDialog()
                        .showCenterImage(DialogType.ERROR)
                        .setDialogTitleWithString("Đổi mật khẩu mã hóa thất bại")
                        .hideOptionButton()
                    lifecycleScope.launchWhenStarted {
                        delay(3000)
                        CommonAlertDialogNotification.getInstanceCommonAlertdialog(requireContext())
                            .dismiss()
                    }
                }

                SUCCESS -> {
                    CommonAlertDialogNotification.getInstanceCommonAlertdialog(requireContext())
                        .showDialog()
                        .showCenterImage(DialogType.SUCCESS)
                        .setDialogTitleWithString("Đổi mật khẩu mã hóa thành công")
                        .hideOptionButton()
                    lifecycleScope.launchWhenStarted {
                        delay(3000)
                        CommonAlertDialogNotification.getInstanceCommonAlertdialog(requireContext())
                            .dismiss()
                        appNavigation.navigateUp()
                    }
                }

                else -> Unit
            }
        }
        viewModel.error.observe(viewLifecycleOwner) {
            CommonAlertDialogNotification.getInstanceCommonAlertdialog(requireContext())
                .showDialog()
                .showCenterImage(DialogType.ERROR)
                .setDialogTitleWithString(it)
                .hideOptionButton()
            lifecycleScope.launchWhenStarted {
                delay(3000)
                CommonAlertDialogNotification.getInstanceCommonAlertdialog(requireContext())
                    .dismiss()
            }
        }
        retrievePasswordViewModel.requestOTPStatus.observe(viewLifecycleOwner) {
            when (it.status) {
                Status.LOADING -> {
                    showHideLoading(true)
                }

                Status.SUCCESS -> {
                    showHideLoading(false)
                    appNavigation.openVerificationCodeCameraJF(
                        bundleOf(Define.BUNDLE_KEY.PARAM_MODEL_CAMERA to retrievePasswordViewModel.devId)
                    )
                }

                Status.ERROR -> {
                    showHideLoading(false)
                    when (it.exception) {
                        RetrievePasswordCameraJFViewModel.ERROR_NOT_SET_CONTACT -> {
                            CommonAlertDialogNotification
                                .getInstanceCommonAlertdialog(requireContext())
                                .showDialog()
                                .showCenterImage(DialogType.ERROR)
                                .setDialogTitleWithString("Không thể sử dụng chức năng do Camera chưa từng xem trực tiếp với mật khẩu đã thiết lập.")
                                .setContent("Nếu bạn quên mật khẩu vui lòng xóa thiết bị trên ứng dụng và thực hiện thêm lại thiết bị này.")
                                .setTextPositiveButtonWithString("OK")
                                .setOnPositivePressed { dialog ->
                                    dialog.dismiss()
                                }
                                .hideOptionButton()
                                .showPositiveButton()
                        }

                        RetrievePasswordCameraJFViewModel.ERROR_MAX_REQUEST_OTP -> {
                            CommonAlertDialogNotification
                                .getInstanceCommonAlertdialog(requireContext())
                                .showDialog()
                                .showCenterImage(DialogType.ERROR)
                                .setDialogTitleWithString("Bạn đã sử dụng tối đa số lượng OTP trong ngày\nVui lòng thử lại sau")
                                .setTextPositiveButtonWithString("OK")
                                .setOnPositivePressed { dialog ->
                                    dialog.dismiss()
                                }
                                .hideOptionButton()
                                .showPositiveButton()
                        }

                        else -> {
                            CommonAlertDialogNotification
                                .getInstanceCommonAlertdialog(requireContext())
                                .showDialog()
                                .showCenterImage(DialogType.ERROR)
                                .setDialogTitleWithString("Không thể thực hiện chức năng này")
                                .setContent(it.exception.toString())
                                .setTextPositiveButtonWithString("OK")
                                .setOnPositivePressed { dialog ->
                                    dialog.dismiss()
                                }
                                .hideOptionButton()
                                .showPositiveButton()
                        }
                    }
                }
            }
        }
    }

    private fun checkStateButton() {
        binding.tvErrorOldPassword.gone()
        binding.tvErrorNewPassword.gone()
        binding.tvErrorConfirmPassword.gone()
        if (binding.edtNewPassword.getTextEditText().isEmpty()
            || binding.edtConfirmPassword.getTextEditText().isEmpty()
        ) {
            binding.btnChangePassword.isEnabled = false
            binding.btnChangePassword.setBackgroundResource(R.drawable.bg_button_setting_disable)
        } else {
            binding.btnChangePassword.isEnabled = true
            binding.btnChangePassword.setBackgroundResource(R.drawable.bg_button_setting_submit)
        }
    }
}
