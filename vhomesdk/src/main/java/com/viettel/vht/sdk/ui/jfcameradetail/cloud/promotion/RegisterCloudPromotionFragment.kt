package com.viettel.vht.sdk.ui.jfcameradetail.cloud.promotion

import android.app.Dialog
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.observe
import com.vht.sdkcore.base.BaseFragment
import com.vht.sdkcore.network.Status
import com.vht.sdkcore.utils.Define
import com.vht.sdkcore.utils.dialog.CommonAlertDialogNotification
import com.vht.sdkcore.utils.dialog.DialogType
import com.vht.sdkcore.utils.onTextChange
import com.vht.sdkcore.utils.showCustomToast
import com.viettel.vht.sdk.R
import com.viettel.vht.sdk.databinding.DialogReferCodeCloudBinding
import com.viettel.vht.sdk.databinding.FragmentRegisterCloudPromotionBinding
import com.viettel.vht.sdk.model.camera_cloud.DataFreePricing
import com.viettel.vht.sdk.model.camera_cloud.RequestRegisterPromotionFree
import com.viettel.vht.sdk.navigation.AppNavigation
import com.viettel.vht.sdk.utils.gone
import com.viettel.vht.sdk.utils.hideKeyboard
import com.viettel.vht.sdk.utils.invisible
import com.viettel.vht.sdk.utils.setSingleClick
import com.viettel.vht.sdk.utils.showCustomNotificationDialog
import com.viettel.vht.sdk.utils.showDialogEnterPhonePayLink
import com.viettel.vht.sdk.utils.visible
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class RegisterCloudPromotionFragment :
    BaseFragment<FragmentRegisterCloudPromotionBinding, DetailPromotionViewModel>() {

    @Inject
    lateinit var appNavigation: AppNavigation

    private val viewModel: DetailPromotionViewModel by viewModels()

    // kiểm tra tài khoản đã liên kết chưa
    private var isPayLink = false

    private var isSelectPayMethod = true
    private var titleDialogExtendCloud = ""


    override val layoutId: Int
        get() = R.layout.fragment_register_cloud_promotion

    override fun getVM(): DetailPromotionViewModel = viewModel

    private var adapterCameraSelect: CameraSelectFreeAdapter? = null

    private lateinit var dialogReferCodeCloud: Dialog
    private var priceCloud = 0



    override fun initView() {
        super.initView()
        viewModel.getListPricingPaymentCloud()
        adapterCameraSelect = CameraSelectFreeAdapter {
            checkSelectAllDevice()
            enableButtonOk()
        }
        binding.rcvListDevice.adapter = adapterCameraSelect
        binding.tvPrice.paintFlags = binding.tvPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        binding.tvPriceRegister.paintFlags = binding.tvPriceRegister.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        viewModel.getListPaymentLink()
        viewModel.getCheckInvitationCode()
        dialogReferCodeCloud = showDialogReferCodeCloud()
    }

    private fun String.format(): String {
        val num = this.toDouble()
        val formatted = String.format("%,.0f", num)
        return formatted.replace(",", ".")
    }

    override fun bindingStateView() {
        super.bindingStateView()
        viewModel.dataListPricingCloud.observe(viewLifecycleOwner) {
            when (it.status) {
                Status.LOADING -> showHideLoading(false)
                Status.ERROR -> showHideLoading(false)
                Status.SUCCESS -> {
                    showHideLoading(false)
                    it.data?.let { responseData ->
                        if (it.data?.code == 200) {
                            // thực hiện show các dòng camera.
                            val dataList = it?.data?.data?.promotions?.free ?: listOf()
                            if (dataList.isNotEmpty()) {
                                dataList.forEach { data ->
                                    data.isSelected = viewModel.cbAllDeviceLiveData.value ?: false
                                }
                                binding.tvTimeStorage.text =
                                    "Lưu sự kiện ${it?.data?.data?.promotions?.cloudPeriod} ngày"
                                binding.tvTime.text =
                                    "${it?.data?.data?.promotions?.cloudTime?.div(30)} tháng"
                                binding.tvPrice.text =
                                    "${it?.data?.data?.promotions?.price?.format()}đ"
                                priceCloud = it?.data?.data?.promotions?.price?.toInt() ?: 0
                                val priceMonth = it?.data?.data?.promotions?.price?.toInt()
                                    ?.div((it?.data?.data?.promotions?.cloudTime?.div(30) ?: 1))
                                    ?: 0
                                titleDialogExtendCloud = getString(
                                    com.vht.sdkcore.R.string.string_show_content_title_cloud_camera,
                                    it?.data?.data?.promotions?.cloudPeriod,
                                    it?.data?.data?.promotions?.cloudTime?.div(30),
                                    priceMonth.toString().format() + "đ/tháng"
                                )
                                handleDataCache(listData = dataList)
                                viewModel.cbExtendLiveData.value = false
                                binding.rbPayLink.isSelected = true
                                isSelectPayMethod = true
                            }

                        } else {
                            showDialogError(
                                title = "Lỗi xảy ra",
                                type = DialogType.ERROR,
                                message = it.data?.message ?: "",
                                titleBtnConfirm = com.vht.sdkcore.R.string.ok,
                                onPositiveClick = {

                                }
                            )
                        }
                    }

                }
            }
        }

        viewModel.dataCheckInvitationCode.observe(viewLifecycleOwner) {
            when (it.status) {
                Status.ERROR -> {
                    binding.clReferralCode.gone()
                }
                Status.SUCCESS -> {
                    it.data?.let { data ->
                        if (data.invitationCode == "enable") {
                            binding.clReferralCode.visible()
                        } else {
                            binding.clReferralCode.gone()
                        }
                    }
                }
                else -> {
                    binding.clReferralCode.gone()
                }
            }
        }

        viewModel.cbAllDeviceLiveData.observe(viewLifecycleOwner) {
            if (it) {
                binding.cbAllDevice.setImageResource(R.drawable.ic_enable_square)
            } else {
                binding.cbAllDevice.setImageResource(R.drawable.ic_disable_square)
            }
        }



        viewModel.listPaymentLink.observe(viewLifecycleOwner) {
            when (it.status) {
                Status.LOADING -> showHideLoading(true)

                Status.ERROR -> showHideLoading(false)

                Status.SUCCESS -> {
                    showHideLoading(false)
                    it.data?.let { listPaymentLinkResponse ->
                        val listPaymentLinkRegister =
                            listPaymentLinkResponse.data?.filter { it.typeRequest == "register" }
                                ?: listOf()
                        isPayLink = listPaymentLinkRegister.isNotEmpty()

                    }
                }
            }
        }

        viewModel.urlRegisterPayLink.observe(viewLifecycleOwner) {
            when (it.status) {
                Status.LOADING -> showHideLoading(true)

                Status.ERROR -> showHideLoading(false)

                Status.SUCCESS -> {
                    showHideLoading(false)

                    val urlUnRegister = it.data?.data ?: ""
                    if (urlUnRegister.isNotEmpty()) {
                        val bundle = bundleOf().apply {
                            putString(Define.BUNDLE_KEY.PARAM_URL_REGISTER_PAY_LINK, urlUnRegister)
                        }
                        appNavigation.openRegisterPayLinkFragment(bundle)
                    } else {
                        showDialogError(
                            title = "Lỗi xảy ra",
                            type = DialogType.ERROR,
                            message = it.data?.message ?: "",
                            titleBtnConfirm = com.vht.sdkcore.R.string.ok,
                            onPositiveClick = {

                            }
                        )
                    }

                }
            }
        }
        viewModel.dataOTPOnOffPayLink.observe(viewLifecycleOwner) {
            when (it.status) {
                Status.LOADING -> showHideLoading(true)

                Status.ERROR -> showHideLoading(false)

                Status.SUCCESS -> {
                    showHideLoading(false)
                    if (it.data?.code == 200) {
                        showCustomNotificationDialog(
                            title = getString(com.vht.sdkcore.R.string.string_verification_code_sent_phone),
                            type = DialogType.SUCCESS,
                            titleBtnConfirm = com.vht.sdkcore.R.string.text_ok
                        ) {
                            // bổ sung màn OTP
                            val listData =
                                adapterCameraSelect?.currentList?.filter { it.isSelected }
                            val listItem = mutableListOf<RequestRegisterPromotionFree.Item>()

                            listData?.forEach {
                                listItem.add(
                                    RequestRegisterPromotionFree.Item(
                                        it.name,
                                        it.id,
                                        it.serial,
                                        it.type
                                    )
                                )
                            }
                            val invitationCode = if (viewModel.referCodeCloud.value == null) {
                                ""
                            } else {
                                viewModel.referCodeCloud.value.toString()
                            }
                            val dataRequest = RequestRegisterPromotionFree(
                                list = listItem,
                                otp = "",
                                order_id = it.data?.data ?: "",
                                invitation_code = invitationCode
                            )

                            val bundle = bundleOf().apply {
                                putParcelable(
                                    Define.BUNDLE_KEY.PARAM_DATA_REGISTER_CLOUD_PROMOTION,
                                    dataRequest
                                )
                            }

                            appNavigation.openGlobalManageCloudPromotionOTP(bundle)
                        }
                    } else {
                        showDialogError(
                            title = "Lỗi xảy ra",
                            type = DialogType.ERROR,
                            message = it.data?.message ?: "",
                            titleBtnConfirm = com.vht.sdkcore.R.string.ok,
                            onPositiveClick = {

                            }
                        )
                    }
                }
            }
        }

        viewModel.cbExtendLiveData.observe(viewLifecycleOwner) {
            if (it) {
                binding.cbExtend.setImageResource(R.drawable.ic_enable_square)
            } else {
                binding.cbExtend.setImageResource(R.drawable.ic_disable_square)
            }
            enableButtonOk()
        }
        viewModel.referCodeCloud.observe(viewLifecycleOwner) { code ->
            if (!code.isNullOrEmpty()) {
                binding.tvReferCode.text = code
            } else {
                binding.tvReferCode.text = code
                binding.tvReferCode.hint = getString(com.vht.sdkcore.R.string.string_hint_refer_code)
            }
        }
    }

    private fun enableButtonOk() {
        var isSelectItemNull = false
        val totalCamera = adapterCameraSelect?.currentList?.filter { it.isSelected }?.size ?: 0
        adapterCameraSelect?.currentList?.forEach {
            if (it.isSelected) {
                isSelectItemNull = true
                return@forEach
            }
        }
        setUIEnableRegister(isSelectPayMethod && viewModel.cbExtendLiveData.value ?: false && isSelectItemNull)
        binding.btnPayment.text = getString(com.vht.sdkcore.R.string.string_register_total,totalCamera)
        if (totalCamera == 0) {
            binding.tvPriceRegister.invisible()
        } else {
            binding.tvPriceRegister.visible()
            binding.tvPriceRegister.text = (totalCamera * priceCloud).toString().format() + "đ"
        }
    }


    private fun checkSelectAllDevice() {
        var isSelectAll = true
        adapterCameraSelect?.currentList?.forEach {
            if (!it.isSelected) {
                isSelectAll = false
                return@forEach
            }
        }
        viewModel.cbAllDeviceLiveData.value = isSelectAll
    }

    override fun setOnClick() {
        super.setOnClick()
        binding.toolbar.setOnLeftClickListener {
            appNavigation.navigateUp()
        }

        binding.cbExtend.setOnClickListener {
            viewModel.cbExtendLiveData.value = !(viewModel.cbExtendLiveData.value ?: true)
        }

        binding.tvReferCode.setSingleClick {
            dialogReferCodeCloud.show()
        }

        binding.rbGroupPay.setOnCheckedChangeListener { _, checkedId ->
            isSelectPayMethod = true
            setUIEnableRegister(isSelectPayMethod && viewModel.cbExtendLiveData.value ?: true)
        }

        binding.cbAllDevice.setOnClickListener {
            viewModel.cbAllDeviceLiveData.value = !(viewModel.cbAllDeviceLiveData.value ?: false)
            adapterCameraSelect?.onSelectedAll(viewModel.cbAllDeviceLiveData.value?:false)
            enableButtonOk()
        }


        binding.tvTermsOfUse.setOnClickListener {
            val bundle = bundleOf().apply {
                putString(
                    Define.BUNDLE_KEY.PARAM_SCREEN_OPEN_PRIVATE_POLICY,
                    Define.BUNDLE_KEY.SETTING_PARAM_SCREEN_OPEN_PRIVATE_POLICY
                )
            }
           // appNavigation.openGlobalPolicyFragment(bundle)
        }

        binding.tvExtend.setOnClickListener {
            CommonAlertDialogNotification.getInstanceCommonAlertdialog(requireContext())
                .showDialog()
                .setDialogTitleWithString(titleDialogExtendCloud)
                .showCenterImage(DialogType.NOTIFICATION)
                .setContent(com.vht.sdkcore.R.string.string_show_content_extent_cloud_camera)
                .setTextPositiveButton(com.vht.sdkcore.R.string.ok)
                .showPositiveButton()
                .setOnPositivePressed {
                    it.dismiss()
                }
        }

        binding.btnPayment.setOnClickListener {
            if (isPayLink) {
                showDialogNotification(
                    title = getString(com.vht.sdkcore.R.string.string_note_register_automatic),
                    type = DialogType.CONFIRM,
                    titleBtnConfirm = com.vht.sdkcore.R.string.continue_text,
                    message = com.vht.sdkcore.R.string.string_show_config_extend_cloud,
                    negativeTitle = com.vht.sdkcore.R.string.string_cancel,
                    onNegativeClick = {
                    },
                    onPositiveClick = {
                        // bổ sung màn OTP
                        viewModel.getOTPOnOffAutoChargingCloud("")
                    }
                )

            } else {
                showDialogNotification(
                    title = getString(com.vht.sdkcore.R.string.string_you_do_not_have_payment_account_link_to_money),
                    type = DialogType.CONFIRM,
                    titleBtnConfirm = com.vht.sdkcore.R.string.continue_link,
                    negativeTitle = com.vht.sdkcore.R.string.string_cancel,
                    message = com.vht.sdkcore.R.string.string_content_show_webview_pay_link,
                    onNegativeClick = {

                    },
                    onPositiveClick = {
                        showDialogEnterPhonePayLink(onClickOK = {
                            viewModel.getURLRegisterPaymentLink(it)
                        })
                    }
                )
            }
        }

    }

    private fun Fragment.showDialogReferCodeCloud(): Dialog {
        return Dialog(requireContext(), R.style.ThemeDialog).apply {
            setCancelable(false)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setCanceledOnTouchOutside(false)
            setCancelable(false)
            val binding =
                DialogReferCodeCloudBinding.inflate(LayoutInflater.from(requireContext()))
            setContentView(binding.root)
            var isSuccess = false
            if ((viewModel.referCodeCloud.value ?: "").isNotEmpty()) {
                binding.edtPhone.setText(viewModel.referCodeCloud.value ?: "")
            } else {
                binding.edtPhone.hint = getString(com.vht.sdkcore.R.string.string_hint_refer_code_dialog)
            }
            binding.edtPhone.onTextChange {
                isSuccess = if (it.toString().trim().isEmpty()) {
                    binding.edtPhone.hint = getString(com.vht.sdkcore.R.string.string_hint_refer_code_dialog)
                    binding.imvClearText.gone()
                    false
                } else {
                    binding.imvClearText.visible()
                    binding.tvError.gone()
                    true
                }
            }
            binding.imvClearText.setOnClickListener {
                binding.edtPhone.setText("")
                binding.imvClearText.gone()
            }

            binding.btnOk.setOnClickListener {
                binding.tvError.gone()
                if (isSuccess) {
                    binding.root.hideKeyboard()
                    viewModel.getDataCheckCloudReferCloud(binding.edtPhone.text.toString().trim())
                } else {
                    viewModel.referCodeCloud.value = ""
                    dismiss()
                }
            }
            viewModel.responseReferCodeCloud.observe(viewLifecycleOwner) { data ->
                if (data?.code == 200) {
                    if (data?.data?.status.equals("1")) {
                        viewModel.referCodeCloud.value = binding.edtPhone.text.toString().trim()
                        showCustomToast(resTitle = com.vht.sdkcore.R.string.string_refer_code_success, onFinish = {
                        }, showImage = true)
                        dismiss()
                    } else {
                        binding.tvError.text = getString(com.vht.sdkcore.R.string.string_refer_code_error_member)
                        binding.tvError.visible()
                    }

                } else {
                    val errorString = data?.message
                    binding.tvError.text = errorString
                    binding.tvError.visible()
                }
            }

            binding.btnCancel.setOnClickListener {
                binding.edtPhone.setText(viewModel.referCodeCloud.value ?: "")
                binding.edtPhone.setSelection(binding.edtPhone.text.toString().trim()?.length ?: 0)
                binding.root.hideKeyboard()
                dismiss()
            }
            lifecycle.addObserver(object : LifecycleObserver {
                @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
                fun onDestroy() {
                    dismiss()
                }
            })

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.referCodeCloud.value = ""
        adapterCameraSelect = null
        viewModel.listCamera.clear()
    }

    private fun setUIEnableRegister(isEnable: Boolean) {
        if (isEnable) {
            binding.btnPayment.isClickable = true
            binding.btnPayment.isEnabled = true
            binding.btnPayment.setBackgroundResource(R.drawable.bg_corner_8_f8214b)
        } else {
            binding.btnPayment.isClickable = false
            binding.btnPayment.isEnabled = false
            binding.btnPayment.setBackgroundResource(R.drawable.bg_corner_8_cecece)
        }
    }

    private fun handleDataCache(listData: List<DataFreePricing>){
        listData.forEach {
            viewModel.listCamera.forEach{ dataCache ->
                if(it.serial == dataCache.serial){
                    it.isSelected = dataCache.isSelected
                }
            }
        }
        adapterCameraSelect?.submitList(listData)
        viewModel.listCamera.addAll(listData)
    }

}