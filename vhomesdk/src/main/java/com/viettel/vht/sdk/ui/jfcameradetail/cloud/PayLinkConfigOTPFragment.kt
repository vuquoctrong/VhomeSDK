package com.viettel.vht.sdk.ui.jfcameradetail.cloud

import android.os.Bundle
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.vht.sdkcore.base.BaseFragment
import com.vht.sdkcore.network.Status
import com.vht.sdkcore.utils.Define
import com.vht.sdkcore.utils.dialog.DialogType
import com.vht.sdkcore.utils.onTextChange
import com.vht.sdkcore.utils.showCustomToast
import com.vht.sdkcore.utils.toastMessage
import com.viettel.vht.sdk.R
import com.viettel.vht.sdk.databinding.FragmentConfigOtpPayLinkBinding
import com.viettel.vht.sdk.model.camera_cloud.BuyCloudVeRequest
import com.viettel.vht.sdk.model.camera_cloud.OnOffAutoChargingCloud
import com.viettel.vht.sdk.model.camera_cloud.PaymentPayLinkRequest
import com.viettel.vht.sdk.navigation.AppNavigation
import com.viettel.vht.sdk.utils.gone
import com.viettel.vht.sdk.utils.visible
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PayLinkConfigOTPFragment :
    BaseFragment<FragmentConfigOtpPayLinkBinding, PayLinkConfigOTPViewModel>() {

    @Inject
    lateinit var appNavigation: AppNavigation

    private val viewModel: PayLinkConfigOTPViewModel by viewModels()


    override val layoutId: Int
        get() = R.layout.fragment_config_otp_pay_link

    override fun getVM(): PayLinkConfigOTPViewModel = viewModel

    private var orderID = ""
    private var packageId = ""
    private var deviceId = ""
    private var serial = ""
    private var isExtendAutoCloud = false
    private var totalRetry = 5
    private var referCode =""
    private var isOpenGiftRelative = false
    private var userIdRelative = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        starTimer()
    }


    override fun initView() {
        super.initView()

        arguments?.let {
            if (it.containsKey(Define.BUNDLE_KEY.PARAM_DATA_ORDER_ID_VT_PAY_WALLET)) {
                orderID = it.getString(Define.BUNDLE_KEY.PARAM_DATA_ORDER_ID_VT_PAY_WALLET) ?: ""
            }

            if (it.containsKey(Define.BUNDLE_KEY.PARAM_PACKAGE_ID)) {
                packageId = it.getString(Define.BUNDLE_KEY.PARAM_PACKAGE_ID, "")
            }

            if (it.containsKey(Define.BUNDLE_KEY.PARAM_DEVICE_ID)) {
                deviceId = it.getString(Define.BUNDLE_KEY.PARAM_DEVICE_ID, "")
            }

            if (it.containsKey(Define.BUNDLE_KEY.PARAM_DATA_OPEN_AUTO_CHARGING_CLOUD)) {
                isExtendAutoCloud =
                    it.getBoolean(Define.BUNDLE_KEY.PARAM_DATA_OPEN_AUTO_CHARGING_CLOUD)
            }

            if (it.containsKey(Define.BUNDLE_KEY.PARAM_DEVICE_SERIAL)) {
                serial = it.getString(Define.BUNDLE_KEY.PARAM_DEVICE_SERIAL) ?: ""
            }
            if(it.containsKey(Define.BUNDLE_KEY.PARAM_DATA_REGISTER_REFER_CODE_CLOUD)){
                referCode = it.getString(Define.BUNDLE_KEY.PARAM_DATA_REGISTER_REFER_CODE_CLOUD)?:""
            }

            if (it.containsKey(Define.BUNDLE_KEY.PARAM_OPEN_TO_GIFT_RELATIVES)) {
                isOpenGiftRelative = it.getBoolean(Define.BUNDLE_KEY.PARAM_OPEN_TO_GIFT_RELATIVES)
            }

            if (it.containsKey(Define.BUNDLE_KEY.PARAM_USE_ID_TO_GIFT_RELATIVES)) {
                userIdRelative = it.getString(Define.BUNDLE_KEY.PARAM_USE_ID_TO_GIFT_RELATIVES) ?: ""
            }


        }
        binding.btnOk.isClickable = false
        binding.btnOk.isEnabled = false
        binding.btnOk.alpha = 0.7f
        binding.tvNumberRetry.gone()
        binding.tvNumberRetry.text = "Thử lại $totalRetry"
    }

    private fun setTextRetry(error: String){
        totalRetry --
        binding.tvNumberRetry.visible()
        binding.tvNumberRetry.text = "Thử lại $totalRetry"
        if(totalRetry == 0){
            showDialogError(
                title = "Bạn đã nhập sai mã xác minh 5 lần",
                type = DialogType.ERROR,
                message = "Xác thực thanh toán của bạn bị tạm khóa 5 phút. Bạn sẽ không nhận được OTP trong thời gian này. Vui lòng thử lại sau.",
                titleBtnConfirm = com.vht.sdkcore.R.string.ok,
                onPositiveClick = {
                    findNavController().navigateUp()
                }
            )
        }else{
            if(error.contains("OTP hết hạn")|| error.contains("OTP không chính xác")){
                binding.tvErrorOne.visible()
                binding.tvErrorOne.text  = error
            }else{
                showDialogError(
                    title = "Lỗi xảy ra",
                    type = DialogType.ERROR,
                    message = error?:"",
                    titleBtnConfirm = com.vht.sdkcore.R.string.ok,
                    onPositiveClick ={

                    }
                )
            }
        }
    }


    override fun bindingStateView() {
        super.bindingStateView()

        viewModel.countdownTimer.observe(viewLifecycleOwner) { seconds ->
            binding.tvTimeDown.text =
                String.format("%d", seconds / 60) + ":" + String.format("%02d", seconds % 60)

            if (seconds == 0) {
                binding.tvOpenRetry.setTextColor(requireActivity().getColor(R.color.text_EC0D3A))
                binding.tvOpenRetry.alpha = 1f
                binding.tvOpenRetry.isEnabled = true
                binding.tvOpenRetry.isClickable = true
            } else {
                binding.tvOpenRetry.setTextColor(requireActivity().getColor(R.color.text_EC0D3A))
                binding.tvOpenRetry.alpha = 0.8f
                binding.tvOpenRetry.isEnabled = false
                binding.tvOpenRetry.isClickable = false
            }
        }

        viewModel.getRegisterPaymentPackageV2.observe(viewLifecycleOwner) {
            when (it.status) {
                Status.LOADING -> showHideLoading(true)

                Status.ERROR -> showHideLoading(false)

                Status.SUCCESS -> {
                    showHideLoading(false)

                    if (it.data?.code == 200 && (it.data?.data ?: "").isNotEmpty()) {
                        orderID = it.data?.data ?: ""
                        starTimer()
                    } else {
                        showDialogError(
                            title = "Lỗi xảy ra",
                            type = DialogType.ERROR,
                            message = it.data?.message?:"",
                            titleBtnConfirm = com.vht.sdkcore.R.string.ok,
                            onPositiveClick ={

                            }
                        )
                    }

                }
            }
        }

        viewModel.paymentLinkPayWhenRegisterCloudResponse.observe(viewLifecycleOwner) {
            when (it.status) {
                Status.LOADING -> showHideLoading(true)

                Status.ERROR ->{
                    showHideLoading(false)
                    setTextRetry("Có lỗi xảy ra")
                }

                Status.SUCCESS -> {
                    showHideLoading(false)

                    it?.data?.let {
                        if (it.code == 200) {
                            if (isExtendAutoCloud) {
                                viewModel.setOnOffAutoChargingCloud(
                                    OnOffAutoChargingCloud(
                                        serial = serial,
                                        status = "activate",
                                        method = "VTPAY-WALLET",
                                        otp = "system-run",
                                        orderId = orderID
                                    )
                                )
                            } else {
                                showCustomToast(
                                    resTitleString = "Đăng ký thành công",
                                    onFinish = {
                                        if(isOpenGiftRelative){
                                            appNavigation.backToCameraCloudStorageAccountFragment()
                                        }else{
                                            appNavigation.backToCloudStorageJFCamera()
                                        }
                                    },
                                    showImage = true
                                )
                            }

                        } else {
                            val error = it.message?:""
                            if(error.contains("Vượt quá hạn mức")){
                                showDialogError(
                                    title = "Giao dịch thất bại",
                                    type = DialogType.ERROR,
                                    message = "Tài khoản của bạn đã sử dụng vượt hạn mức giao dịch ngày. Vui lòng tăng hạn mức để tiếp tục sử dụng hoặc thay đổi tài khoản thanh toán.",
                                    titleBtnConfirm = com.vht.sdkcore.R.string.ok,
                                    onPositiveClick = {
                                        findNavController().navigateUp()
                                    }
                                )

                            }else{
                                setTextRetry(it.message?:"")
                            }


                        }
                    }

                }
            }
        }

        viewModel.responseOnOffAutoChargingCloud.observe(viewLifecycleOwner) {
            when (it.status) {
                Status.LOADING -> showHideLoading(true)

                Status.ERROR -> {
                    showHideLoading(false)
                    toastMessage("Lỗi Bật tự động gia hạn thành công")
                }

                Status.SUCCESS -> {
                    showHideLoading(false)

                    it?.data?.let {
                        if (it.code == 200) {
                            showCustomToast(
                                resTitleString = "Đăng ký thành công",
                                onFinish = {
                                    if(isOpenGiftRelative){
                                        appNavigation.backToCameraCloudStorageAccountFragment()
                                    }else{
                                        appNavigation.backToCloudStorageJFCamera()
                                    }
                                },
                                showImage = true
                            )

                        } else {
                            showDialogError(
                                title = "Lỗi xảy ra",
                                type = DialogType.ERROR,
                                message = it.message?:"",
                                titleBtnConfirm = com.vht.sdkcore.R.string.ok,
                                onPositiveClick ={

                                }
                            )
                        }
                    }

                }
            }
        }

        binding.edtOTP.onTextChange {
            if (it.toString().isNotEmpty() && it.toString().length >= 6) {
                binding.btnOk.isClickable = true
                binding.btnOk.isEnabled = true
                binding.btnOk.alpha = 1f
            } else {
                binding.btnOk.isClickable = false
                binding.btnOk.isEnabled = false
                binding.btnOk.alpha = 0.5f
            }
        }

    }

    override fun setOnClick() {
        super.setOnClick()
        binding.toolbar.setOnLeftClickListener {
            appNavigation.navigateUp()
        }

        binding.tvOpenRetry.setOnClickListener {
            viewModel.getLinkBuyCloudURl(BuyCloudVeRequest(deviceId, packageId, "VTPAY-WALLET", invitationCode = referCode, userIdRelative =  userIdRelative))
        }

        binding.btnOk.setOnClickListener {
            if (binding.edtOTP.toString().isEmpty()) {
                toastMessage("Bạn cần nhập OTP")
            } else {
                binding.tvErrorOne.gone()
                viewModel.getPaymentLinkPayWhenRegisterCloud(
                    PaymentPayLinkRequest(
                        orderID,
                        binding.edtOTP.text.toString() ?: ""
                    )
                )
            }

        }

    }


    private fun starTimer() {
        viewModel.startCountdown()
    }
}