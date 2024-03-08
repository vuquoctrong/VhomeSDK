package com.viettel.vht.sdk.ui.vtpay

import android.os.Bundle
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import com.vht.sdkcore.base.BaseFragment
import com.vht.sdkcore.network.Status
import com.vht.sdkcore.utils.Define
import com.vht.sdkcore.utils.dialog.DialogType
import com.vht.sdkcore.utils.onTextChange
import com.vht.sdkcore.utils.showCustomToast
import com.vht.sdkcore.utils.toastMessage
import com.viettel.vht.sdk.R
import com.viettel.vht.sdk.databinding.FragmentConfigOtpOnOffPayLinkBinding
import com.viettel.vht.sdk.model.camera_cloud.OnOffAutoChargingCloud
import com.viettel.vht.sdk.navigation.AppNavigation
import com.viettel.vht.sdk.utils.gone
import com.viettel.vht.sdk.utils.visible
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ConfigOTPOnOffPayLinkFragment :
    BaseFragment<FragmentConfigOtpOnOffPayLinkBinding, TabSettingViewModel>() {


    private val viewModel: TabSettingViewModel by viewModels()

    @Inject
    lateinit var mainNavigation: AppNavigation


    override val layoutId: Int
        get() = R.layout.fragment_config_otp_on_off_pay_link

    override fun getVM(): TabSettingViewModel = viewModel

    private var orderID = ""
    private var serial = ""
    private var statusPayLink = "activate"
    private var totalRetry = 5

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


            if (it.containsKey(Define.BUNDLE_KEY.PARAM_DEVICE_SERIAL)) {
                serial = it.getString(Define.BUNDLE_KEY.PARAM_DEVICE_SERIAL) ?: ""
            }

            if(it.containsKey(Define.BUNDLE_KEY.PARAM_DATA_STATUS_PAYLINK)){
                statusPayLink = it.getString(Define.BUNDLE_KEY.PARAM_DATA_STATUS_PAYLINK)?:"activate"
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
                    mainNavigation.navigateUp()
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




        viewModel.responseOnOffAutoChargingCloud.observe(viewLifecycleOwner) {
            when (it.status) {
                Status.LOADING -> showHideLoading(true)

                Status.ERROR -> {
                    showHideLoading(false)
                    setTextRetry("Có lỗi xảy ra")
                }

                Status.SUCCESS -> {
                    showHideLoading(false)

                    it?.data?.let {
                        if (it.code == 200) {
                            if(statusPayLink == "activate"){
                                showCustomToast(title = "Bật thanh toán tự động thành công", isSuccess =  true,onFinish ={
                                    mainNavigation.navigateUp()
                                })
                            }else{
                                showCustomToast(title = "Tắt thanh toán tự động thành công", isSuccess =  true, onFinish = {
                                    mainNavigation.navigateUp()
                                })
                            }

                        } else {
                            setTextRetry(it.message?:"")
                        }
                    }

                }
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
           mainNavigation.navigateUp()
        }

        binding.tvOpenRetry.setOnClickListener {
            viewModel.getOTPOnOffAutoChargingCloud(serial)
        }

        binding.btnOk.setOnClickListener {
            if (binding.edtOTP.toString().isEmpty()) {
                toastMessage("Bạn cần nhập OTP")
            } else {
                binding.tvErrorOne.gone()
                // thực hiển gửi lệnh on/ off
                viewModel.setOnOffAutoChargingCloud(OnOffAutoChargingCloud(serial = serial, status =  statusPayLink, method = "VTPAY-WALLET", otp = binding.edtOTP.text.toString(), orderId = orderID))
            }

        }

    }


    private fun starTimer() {
        viewModel.startCountdown()
    }
}