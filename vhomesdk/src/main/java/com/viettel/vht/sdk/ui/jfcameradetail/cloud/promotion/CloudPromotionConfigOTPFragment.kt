package com.viettel.vht.sdk.ui.jfcameradetail.cloud.promotion

import android.os.Build
import android.os.Bundle
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import com.vht.sdkcore.base.BaseFragment
import com.vht.sdkcore.network.Status
import com.vht.sdkcore.utils.Define
import com.vht.sdkcore.utils.dialog.DialogType
import com.vht.sdkcore.utils.onTextChange
import com.vht.sdkcore.utils.showCustomToast
import com.vht.sdkcore.utils.toastMessage
import com.viettel.vht.sdk.R
import com.viettel.vht.sdk.databinding.FragmentConfigOtpRegisterPromotionBinding
import com.viettel.vht.sdk.model.camera_cloud.RequestRegisterPromotionFree
import com.viettel.vht.sdk.navigation.AppNavigation
import com.viettel.vht.sdk.utils.gone
import com.viettel.vht.sdk.utils.visible
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CloudPromotionConfigOTPFragment :
    BaseFragment<FragmentConfigOtpRegisterPromotionBinding, DetailPromotionViewModel>() {

    @Inject
    lateinit var appNavigation: AppNavigation
    private val viewModel: DetailPromotionViewModel by viewModels()


    override val layoutId: Int
        get() = R.layout.fragment_config_otp_register_promotion

    override fun getVM(): DetailPromotionViewModel = viewModel

    private var orderID = ""
    private var dataRequest: RequestRegisterPromotionFree? = null
    private var totalRetry = 6

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        starTimer()
    }


    override fun initView() {
        super.initView()

        arguments?.let {
            if (it.containsKey(Define.BUNDLE_KEY.PARAM_DATA_REGISTER_CLOUD_PROMOTION)) {
                dataRequest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    it.getParcelable(
                        Define.BUNDLE_KEY.PARAM_DATA_REGISTER_CLOUD_PROMOTION,
                        RequestRegisterPromotionFree::class.java
                    )
                } else {
                    it.getParcelable(Define.BUNDLE_KEY.PARAM_DATA_REGISTER_CLOUD_PROMOTION) as RequestRegisterPromotionFree?
                }
            }

        }
        binding.btnOk.isClickable = false
        binding.btnOk.isEnabled = false
        binding.btnOk.alpha = 0.7f
        binding.tvNumberRetry.gone()
        binding.tvNumberRetry.text = "Thử lại $totalRetry"
    }

    //show lỗi khi thực hiện xác thực OTP
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



        binding.edtOTP.onTextChange {
            if (it.toString().isNotEmpty() && it.toString().length >= 4) {
                binding.btnOk.isClickable = true
                binding.btnOk.isEnabled = true
                binding.btnOk.alpha = 1f
            } else {
                binding.btnOk.isClickable = false
                binding.btnOk.isEnabled = false
                binding.btnOk.alpha = 0.6f
            }
        }

        viewModel.dataOTPOnOffPayLink.observe(viewLifecycleOwner){
            when (it.status) {
                Status.LOADING -> showHideLoading(true)

                Status.ERROR -> showHideLoading(false)

                Status.SUCCESS -> {
                    showHideLoading(false)
                    if(it.data?.code == 200){
                        orderID = it.data?.data?:""
                        starTimer()
                    }else{
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

        viewModel.setCamerasPricingCloudResponse.observe(viewLifecycleOwner) {
            when (it.status) {
                Status.LOADING -> showHideLoading(true)
                Status.ERROR -> {
                    showHideLoading(false)
                    setTextRetry("Có lỗi xảy ra")
                }
                Status.SUCCESS -> {
                    showHideLoading(false)
                    it.data?.let { responseData ->
                        if (it.data?.code == 200) {
                            showCustomToast(
                                "Đăng ký thành công", true
                            ) {
                                requireActivity().finish()
                            }
                        } else {
                            setTextRetry(it.data?.message ?: "")
                        }
                    }

                }
            }
        }

    }

    override fun setOnClick() {
        super.setOnClick()
        binding.toolbar.setOnLeftClickListener {
            appNavigation.navigateUp()
        }

        binding.tvOpenRetry.setOnClickListener {
            viewModel.getOTPOnOffAutoChargingCloud("")
        }

        binding.btnOk.setOnClickListener {
            if (binding.edtOTP.text.toString().trim().isEmpty()) {
                toastMessage("Bạn cần nhập OTP")
            } else {
                //thực hiện gửi OTP
                binding.tvErrorOne.gone()
                dataRequest?.otp = binding.edtOTP.text.toString().trim()
                if(orderID.isNotEmpty()){
                    dataRequest?.order_id = orderID
                }
                setDataRegister()
            }

        }

    }

    private fun setDataRegister() {
        dataRequest?.let {
            viewModel.setListCameraPricingPaymentCloud(it)
        }

    }


    private fun starTimer() {
        viewModel.startCountdown()
    }
}