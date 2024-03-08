package com.viettel.vht.sdk.ui.jfcameradetail.forgot_password

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.vht.sdkcore.base.BaseFragment
import com.vht.sdkcore.network.Status
import com.vht.sdkcore.utils.Define
import com.vht.sdkcore.utils.dialog.CommonAlertDialogNotification
import com.vht.sdkcore.utils.dialog.DialogType
import com.vht.sdkcore.utils.isNetworkAvailable
import com.viettel.vht.sdk.R
import com.viettel.vht.sdk.databinding.FragmentVerificationCodeCameraJfBinding
import com.viettel.vht.sdk.navigation.AppNavigation
import com.viettel.vht.sdk.utils.gone
import com.viettel.vht.sdk.utils.onTextChanged
import com.viettel.vht.sdk.utils.showCustomNotificationDialog
import com.viettel.vht.sdk.utils.visible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import javax.inject.Inject

@AndroidEntryPoint
@SuppressLint("SetTextI18n")
class VerificationCodeCameraJFFragment :
    BaseFragment<FragmentVerificationCodeCameraJfBinding, RetrievePasswordCameraJFViewModel>() {

    @Inject
    lateinit var appNavigation: AppNavigation

    private val viewModel: RetrievePasswordCameraJFViewModel by viewModels()

    override val layoutId = R.layout.fragment_verification_code_camera_jf

    override fun getVM() = viewModel

    private var job: Job? = null

    override fun initView() {
        super.initView()
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        exitFullScreenMode()
        arguments?.let {
            viewModel.devId = it.getString(Define.BUNDLE_KEY.PARAM_MODEL_CAMERA) ?: ""
        }
        binding.tvTitle.text =
            "Vui lòng nhập mã OTP được gửi đến số ${viewModel.getUserPhoneNumberMasked()}."
        retryNewCountTime()
    }

    private fun retryNewCountTime() {
        job?.cancel()
        job = lifecycleScope.launchWhenStarted {
            binding.tvDescription.text = "Mã OTP sẽ hết hạn trong"
            binding.tvTime.visible()
            binding.btnRetry.gone()
            binding.tvErrorOTP.gone()
            binding.tvNumberRetry.gone()
            viewModel.isCountDownTimer = true
            checkEnableButtonVerifyOTP()
            repeat(120) {
                binding.tvTime.text =
                    "${(120 - it) / 60}:${if ((120 - it) % 60 < 10) "0" else ""}${(120 - it) % 60}"
                delay(1000)
            }
            binding.tvDescription.text = "OTP đã hết hạn.\nVui lòng gửi lại OTP để tiếp tục."
            binding.tvTime.text = ""
            binding.tvTime.gone()
            binding.btnRetry.visible()
            viewModel.isCountDownTimer = false
            checkEnableButtonVerifyOTP()
        }
    }

    override fun setOnClick() {
        super.setOnClick()
        binding.toolBar.setOnLeftClickListener {
            appNavigation.navigateUp()
        }
        binding.edtOTP.onTextChanged {
            if (binding.edtOTP.text.toString().isEmpty()) {
                binding.ivClearOTP.gone()
            } else {
                binding.ivClearOTP.visible()
            }
            checkEnableButtonVerifyOTP()
        }
        binding.ivClearOTP.setOnClickListener {
            binding.edtOTP.setText("")
        }
        binding.btnVerifyOTP.setOnClickListener {
            if (context?.isNetworkAvailable() == true) {
                if (viewModel.retryCount <= 0) {
                    CommonAlertDialogNotification.getInstanceCommonAlertdialog(requireContext())
                        .showDialog()
                        .setDialogTitleWithString("Bạn đã nhập sai mã OTP quá 5 lần\nVui lòng thử lại sau 2 phút")
                        .setTextPositiveButtonWithString("OK")
                        .showCenterImage(DialogType.ERROR)
                        .hideOptionButton()
                        .showPositiveButton()
                        .setOnPositivePressed { dialog ->
                            dialog.dismiss()
                        }
                } else {
                    viewModel.verifyOTP(binding.edtOTP.text.toString())
                }
            } else {
                showCustomNotificationDialog(
                    title = getString(com.vht.sdkcore.R.string.no_connection),
                    type = DialogType.ERROR,
                    titleBtnConfirm = com.vht.sdkcore.R.string.text_close
                ) {}
            }
        }
        binding.btnRetry.setOnClickListener {
            if (context?.isNetworkAvailable() == true) {
                viewModel.retryRequestOTP()
            } else {
                showCustomNotificationDialog(
                    title = getString(com.vht.sdkcore.R.string.no_connection),
                    type = DialogType.ERROR,
                    titleBtnConfirm = com.vht.sdkcore.R.string.text_close
                ) {}
            }
        }
    }

    override fun bindingStateView() {
        super.bindingStateView()
        viewModel.requestOTPStatus.observe(viewLifecycleOwner) {
            when (it.status) {
                Status.LOADING -> Unit

                Status.SUCCESS -> {
                    retryNewCountTime()
                }

                Status.ERROR -> {
                    when (it.exception) {
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
        viewModel.verifyOTPStatus.observe(viewLifecycleOwner) {
            when (it.status) {
                Status.LOADING -> Unit

                Status.SUCCESS -> {
                    appNavigation.openResetPasswordCameraJF(
                        bundleOf(Define.BUNDLE_KEY.PARAM_MODEL_CAMERA to viewModel.devId)
                    )
                }

                Status.ERROR -> {
                    binding.tvErrorOTP.visible()
                    binding.tvNumberRetry.text = "Thử lại: ${viewModel.retryCount}"
                    binding.tvNumberRetry.visible()
                }
            }
        }
    }

    private fun checkEnableButtonVerifyOTP() {
        val isEnable = binding.edtOTP.text.toString().length == 6 && viewModel.isCountDownTimer
        binding.btnVerifyOTP.isEnabled = isEnable
        binding.btnVerifyOTP.setBackgroundResource(
            if (isEnable) R.drawable.bg_button_setting_submit
            else R.drawable.bg_button_setting_disable
        )
    }
}
