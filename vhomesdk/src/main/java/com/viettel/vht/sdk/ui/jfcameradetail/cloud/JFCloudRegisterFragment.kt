package com.viettel.vht.sdk.ui.jfcameradetail.cloud

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.observe
import com.vht.sdkcore.base.BaseFragment
import com.vht.sdkcore.network.Status
import com.vht.sdkcore.pref.RxPreferences
import com.vht.sdkcore.utils.Define
import com.vht.sdkcore.utils.dialog.DialogType
import com.vht.sdkcore.utils.onTextChange
import com.vht.sdkcore.utils.showCustomToast
import com.vht.sdkcore.utils.toastMessage
import com.viettel.vht.sdk.R
import com.viettel.vht.sdk.databinding.DialogReferCodeCloudBinding
import com.viettel.vht.sdk.databinding.FragmentJfCloudRegisterBinding
import com.viettel.vht.sdk.model.camera_cloud.BuyCloudVeRequest
import com.viettel.vht.sdk.model.camera_cloud.CloudStoragePackage
import com.viettel.vht.sdk.navigation.AppNavigation
import com.viettel.vht.sdk.ui.jfcameradetail.cloud.adapter.StoragePackageAdapter
import com.viettel.vht.sdk.ui.jfcameradetail.cloud.adapter.StorageTimeAdapter
import com.viettel.vht.sdk.utils.gone
import com.viettel.vht.sdk.utils.hideKeyboard
import com.viettel.vht.sdk.utils.setSingleClick
import com.viettel.vht.sdk.utils.showCustomNotificationDialog
import com.viettel.vht.sdk.utils.showDialogEnterPhonePayLink
import com.viettel.vht.sdk.utils.visible
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.text.DecimalFormat
import javax.inject.Inject

@SuppressLint("SetTextI18n")
@AndroidEntryPoint
class JFCloudRegisterFragment :
    BaseFragment<FragmentJfCloudRegisterBinding, JFCloudStorageViewModel>() {

    @Inject
    lateinit var appNavigation: AppNavigation

    @Inject
    lateinit var rxPreferences: RxPreferences

    private val viewModel: JFCloudStorageViewModel by activityViewModels()

    private lateinit var storageTimeAdapter: StorageTimeAdapter
    private lateinit var dialogReferCodeCloud: Dialog

    override val layoutId: Int
        get() = R.layout.fragment_jf_cloud_register

    override fun getVM(): JFCloudStorageViewModel = viewModel

    private var listStoragePackage: ArrayList<StoragePackageAdapter.Data>? = null
    private val mapCloudPackage: MutableMap<Int, List<CloudStoragePackage>> = mutableMapOf()
    private var packageSelected: CloudStoragePackage? = null
    private var periodStoragePackage: Int? = null

    // kiểm tra tài khoản đã liên kết chưa
    private var isPayLink = false
    private var isFragmentCreated = false

    // kiểm tra màn đăng ký được mở từ button tặng gói cloud cho người thân
    private var isOpenGiftRelative = false
    private var userIdRelative = ""


    override fun initView() {
        super.initView()
        setDataToGiftRelative()
        binding.tvDeviceName.text = viewModel.deviceName
        setUIEnableRegister(false)
        packageSelected?.let {
            binding.tvMoney.text = "${DecimalFormat("#,###,###,###").format(it.price)}đ"
        }
        storageTimeAdapter = StorageTimeAdapter {
            if (it.isSelected) {
                binding.tvMoney.text = "${DecimalFormat("#,###,###,###").format(it.price)}đ"
                packageSelected = it
            } else {
                binding.tvMoney.text = "0đ"
                packageSelected = null
            }
        }
        binding.rcvStorageTime.adapter = storageTimeAdapter
        if (!isFragmentCreated) {
            viewModel.getListCloudStoragePackage(viewModel.deviceType)
            viewModel.getCheckInvitationCode()
            isFragmentCreated = true
        }
        viewModel.getListPaymentLink()
        showUIExtendCloud()
        dialogReferCodeCloud = showDialogReferCodeCloud()
    }

    /**
     *  Lấy thông tin từ màn hình  DetailGiftRelativesFragment
     */
    private fun setDataToGiftRelative() {
        arguments?.let {
            if (it.containsKey(Define.BUNDLE_KEY.PARAM_OPEN_TO_GIFT_RELATIVES)) {
                isOpenGiftRelative = it.getBoolean(Define.BUNDLE_KEY.PARAM_OPEN_TO_GIFT_RELATIVES)
            }
            if (isOpenGiftRelative) {
                if (it.containsKey(Define.BUNDLE_KEY.PARAM_ID)) {
                    viewModel.deviceId = it.getString(Define.BUNDLE_KEY.PARAM_ID) ?: ""
                }
                if (it.containsKey(Define.BUNDLE_KEY.PARAM_NAME)) {
                    viewModel.deviceName = it.getString(Define.BUNDLE_KEY.PARAM_NAME) ?: ""
                }
                if (it.containsKey(Define.BUNDLE_KEY.PARAM_DEVICE_SERIAL)) {
                    viewModel.deviceSerial =
                        it.getString(Define.BUNDLE_KEY.PARAM_DEVICE_SERIAL) ?: ""
                }
                if (it.containsKey(Define.BUNDLE_KEY.PARAM_DEVICE_TYPE)) {
                    viewModel.deviceType = it.getString(Define.BUNDLE_KEY.PARAM_DEVICE_TYPE) ?: ""
                }

                if (it.containsKey(Define.BUNDLE_KEY.PARAM_USE_ID_TO_GIFT_RELATIVES)) {
                    userIdRelative =
                        it.getString(Define.BUNDLE_KEY.PARAM_USE_ID_TO_GIFT_RELATIVES) ?: ""
                }
                binding.clReferralCode.gone()
            }
        }
    }

    private fun paymentRelative() {
        if (binding.rbPayGate.isChecked) {
            val bundle = Bundle().apply {
                putString(Define.BUNDLE_KEY.PARAM_DEVICE_TYPE, viewModel.deviceType)
                putString(Define.BUNDLE_KEY.PARAM_DEVICE_ID, viewModel.deviceId)
                putString(Define.BUNDLE_KEY.PARAM_PACKAGE_ID, packageSelected?.code)
                putString(Define.BUNDLE_KEY.PARAM_DEVICE_SERIAL, viewModel.deviceSerial)
                putBoolean(
                    Define.BUNDLE_KEY.PARAM_DATA_OPEN_AUTO_CHARGING_CLOUD,
                    false
                )
                putString(
                    Define.BUNDLE_KEY.PARAM_DATA_REGISTER_REFER_CODE_CLOUD,
                    ""
                )
                putBoolean(Define.BUNDLE_KEY.PARAM_OPEN_TO_GIFT_RELATIVES, true)
                putString(Define.BUNDLE_KEY.PARAM_USE_ID_TO_GIFT_RELATIVES, userIdRelative)
            }
            appNavigation.openViettelPayWebViewFragment(bundle)
            return
        }
        if (binding.rbPayLink.isChecked) {
            viewModel.getLinkBuyCloudURl(
                BuyCloudVeRequest(
                    viewModel.deviceId,
                    packageSelected?.code ?: "",
                    "VTPAY-WALLET",
                    invitationCode = "",
                    userIdRelative = userIdRelative
                )
            )
            return
        }
        if (binding.rbPayParent.isChecked) {
            toastMessage("Tính năng đang phát triển.")
            return
        }
    }

    private fun showUIExtendCloud() {
        if (viewModel.isShowExtendAutoCloud) {
            if (isOpenGiftRelative) {
                binding.clExtendCloud.gone()
            } else {
                binding.clExtendCloud.visible()
            }
            binding.swTurnOffExtend.isChecked = false
        } else {
            binding.clExtendCloud.gone()
            binding.swTurnOffExtend.isChecked = false
        }
    }

    override fun bindingStateView() {
        super.bindingStateView()
        viewModel.referCodeCloud.observe(viewLifecycleOwner) { code ->
            if (!code.isNullOrEmpty()) {
                binding.tvReferCode.text = code
            } else {
                binding.tvReferCode.text = code
                binding.tvReferCode.hint = getString(com.vht.sdkcore.R.string.string_hint_refer_code)
            }
        }
        viewModel.cloudPackages.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()) {
                //map data
                mapCloudPackage.clear()
                it.filter { it.status != 0 }.forEach { item ->
                    if (mapCloudPackage.isNotEmpty() && mapCloudPackage.containsKey(item.period)) {
                        val listData = mapCloudPackage[item.period]?.toMutableList()
                        listData!!.add(item)
                        listData.sortBy { data -> data.expired }
                        mapCloudPackage[item.period!!] = listData
                    } else {
                        mapCloudPackage[item.period!!] = listOf(item)
                    }
                }

                listStoragePackage = arrayListOf()
                mapCloudPackage.keys.forEachIndexed { _, value ->
                    listStoragePackage?.add(StoragePackageAdapter.Data(value, false))
                }
                listStoragePackage?.sortBy { data -> data.period }
                if (periodStoragePackage == null) {
                    val data = listStoragePackage?.find { it.period == 3 }
                    periodStoragePackage = data?.period ?: listStoragePackage?.get(0)?.period

                }
                periodStoragePackage?.let { period ->
                    onSelectStoragePackage(period)
                    listStoragePackage?.forEach { item -> item.isSelected = false }
                    listStoragePackage?.find { item -> item.period == period }?.isSelected = true
                }
            }
            Timber.d("mapCloudPackage: $mapCloudPackage")
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

                        if (isPayLink) {
                            binding.rbPayLink.visible()
                            binding.swTurnOffExtend.isChecked = viewModel.isShowExtendAutoCloud
                        } else {
                            binding.rbPayLink.gone()
                            binding.swTurnOffExtend.isChecked = false
                        }
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

        viewModel.dataCheckInvitationCode.observe(viewLifecycleOwner){
            when(it.status){
                Status.ERROR -> {
                    binding.clReferralCode.gone()
                }
                Status.SUCCESS -> {
                    it.data?.let {  data ->
                        if(data.invitationCode == "enable"){
                            binding.clReferralCode.visible()
                        }else{
                            binding.clReferralCode.gone()
                        }
                    }
                }

                else -> {}
            }
        }

        viewModel.getRegisterPaymentPackageV2.observe(viewLifecycleOwner) {
            when (it.status) {
                Status.LOADING -> showHideLoading(true)

                Status.ERROR -> showHideLoading(false)

                Status.SUCCESS -> {
                    showHideLoading(false)

                    val orderId = it.data?.data ?: ""
                    if (it.data?.code == 200 && orderId.isNotEmpty()) {

                        showCustomNotificationDialog(
                            title = "Mã xác minh sẽ được gửi đến SĐT của bạn, vui lòng nhập đúng mã để hoàn tất thanh toán",
                            type = DialogType.SUCCESS,
                            titleBtnConfirm = com.vht.sdkcore.R.string.text_ok
                        ) {
                            val bundle = bundleOf().apply {
                                putString(
                                    Define.BUNDLE_KEY.PARAM_DATA_ORDER_ID_VT_PAY_WALLET,
                                    orderId
                                )
                                putString(Define.BUNDLE_KEY.PARAM_DEVICE_ID, viewModel.deviceId)
                                putString(Define.BUNDLE_KEY.PARAM_PACKAGE_ID, packageSelected?.code)
                                putString(
                                    Define.BUNDLE_KEY.PARAM_DEVICE_SERIAL,
                                    viewModel.deviceSerial
                                )
                                if (isOpenGiftRelative) {
                                    putBoolean(
                                        Define.BUNDLE_KEY.PARAM_DATA_OPEN_AUTO_CHARGING_CLOUD,
                                        false
                                    )
                                    putString(
                                        Define.BUNDLE_KEY.PARAM_DATA_REGISTER_REFER_CODE_CLOUD,
                                        ""
                                    )
                                    putBoolean(Define.BUNDLE_KEY.PARAM_OPEN_TO_GIFT_RELATIVES, true)
                                    putString(
                                        Define.BUNDLE_KEY.PARAM_USE_ID_TO_GIFT_RELATIVES,
                                        userIdRelative
                                    )
                                }

                            }

                            appNavigation.openPayLinkConfigOTP(bundle)
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


    override fun setOnClick() {
        super.setOnClick()
        binding.toolbar.setOnLeftClickListener {
            appNavigation.navigateUp()
        }

        binding.rbGroupPay.setOnCheckedChangeListener { _, checkedId ->
            setUIEnableRegister(true)
        }


        binding.tvReferCode.setSingleClick {
            dialogReferCodeCloud.show()
        }

        binding.llNote.setOnClickListener {
            val bundle = bundleOf().apply {
                putString(
                    Define.BUNDLE_KEY.PARAM_SCREEN_OPEN_PRIVATE_POLICY,
                    Define.BUNDLE_KEY.SETTING_PARAM_SCREEN_OPEN_PRIVATE_POLICY
                )
            }
         //   appNavigation.openGlobalPolicyFragment(bundle)
        }
        binding.rlStoragePackage.setOnClickListener {
            BottomSheetStoragePackageFragment(listStoragePackage?.toList() ?: listOf()) {
                binding.tvMoney.text = "0đ"
                packageSelected = null
                onSelectStoragePackage(it)
                periodStoragePackage = it
            }.show(childFragmentManager, "BottomSheetStoragePackageFragment")
        }
        binding.btnPayment.setOnClickListener {
            if (packageSelected == null || isDoubleClick) {
                return@setOnClickListener
            }
            // thực hiện tặng cloud cho người thân.
            if (isOpenGiftRelative) {
                paymentRelative()
            } else {
                if (binding.rbPayGate.isChecked) {
                    if (!binding.swTurnOffExtend.isChecked) {
                        val bundle = Bundle().apply {
                            putString(Define.BUNDLE_KEY.PARAM_DEVICE_TYPE, viewModel.deviceType)
                            putString(Define.BUNDLE_KEY.PARAM_DEVICE_ID, viewModel.deviceId)
                            putString(Define.BUNDLE_KEY.PARAM_PACKAGE_ID, packageSelected?.code)
                            putString(Define.BUNDLE_KEY.PARAM_DEVICE_SERIAL, viewModel.deviceSerial)
                            putBoolean(
                                Define.BUNDLE_KEY.PARAM_DATA_OPEN_AUTO_CHARGING_CLOUD,
                                binding.swTurnOffExtend.isChecked
                            )
                            putString(
                                Define.BUNDLE_KEY.PARAM_DATA_REGISTER_REFER_CODE_CLOUD,
                                binding.tvReferCode.text.toString().trim()
                            )
                        }
                        appNavigation.openViettelPayWebViewFragment(bundle)
                        return@setOnClickListener
                    }

                    // kiểm tra xem tài khoản đã liên kết chưa khi đăng ký gia hạn tự động
                    if (isPayLink) {
                        showDialogNotification(
                            title = "Lưu ý khi đăng ký gia hạn tự động",
                            type = DialogType.CONFIRM,
                            titleBtnConfirm = com.vht.sdkcore.R.string.continue_text,
                            message = com.vht.sdkcore.R.string.string_show_config_extend_cloud,
                            negativeTitle = com.vht.sdkcore.R.string.string_cancel,
                            onNegativeClick = {
                            },
                            onPositiveClick = {
                                val bundle = Bundle().apply {
                                    putString(
                                        Define.BUNDLE_KEY.PARAM_DEVICE_TYPE,
                                        viewModel.deviceType
                                    )
                                    putString(Define.BUNDLE_KEY.PARAM_DEVICE_ID, viewModel.deviceId)
                                    putString(
                                        Define.BUNDLE_KEY.PARAM_PACKAGE_ID,
                                        packageSelected?.code
                                    )
                                    putString(
                                        Define.BUNDLE_KEY.PARAM_DEVICE_SERIAL,
                                        viewModel.deviceSerial
                                    )
                                    putBoolean(
                                        Define.BUNDLE_KEY.PARAM_DATA_OPEN_AUTO_CHARGING_CLOUD,
                                        binding.swTurnOffExtend.isChecked
                                    )
                                    putString(
                                        Define.BUNDLE_KEY.PARAM_DATA_REGISTER_REFER_CODE_CLOUD,
                                        binding.tvReferCode.text.toString().trim()
                                    )
                                }
                                appNavigation.openViettelPayWebViewFragment(bundle)
                            }
                        )

                    } else {
                        showDialogNotification(
                            title = "Bạn chưa có liên kết tài khoản thanh toán với Viettel Money",
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

                    return@setOnClickListener
                }
                if (binding.rbPayLink.isChecked) {

                    if (!binding.swTurnOffExtend.isChecked) {
                        viewModel.getLinkBuyCloudURl(
                            BuyCloudVeRequest(
                                viewModel.deviceId,
                                packageSelected?.code ?: "",
                                "VTPAY-WALLET",
                                invitationCode = binding.tvReferCode.text.toString().trim()
                            )
                        )
                        return@setOnClickListener
                    }
                    // kiểm tra xem tài khoản đã liên kết chưa khi đăng ký gia hạn tự động
                    if (isPayLink) {
                        viewModel.getLinkBuyCloudURl(
                            BuyCloudVeRequest(
                                viewModel.deviceId,
                                packageSelected?.code ?: "",
                                "VTPAY-WALLET",
                                invitationCode = binding.tvReferCode.text.toString().trim()
                            )
                        )

                    } else {
                        showDialogNotification(
                            title = "Bạn chưa có liên kết tài khoản thanh toán với Viettel Money",
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

                if (binding.rbPayParent.isChecked) {
                    toastMessage("Tính năng đang phát triển.")
                }
            }


        }


    }

    private fun onSelectStoragePackage(period: Int) {
        binding.tvTimeStorage.text = String.format("Lưu sự kiện %s ngày", period)
        Timber.d("period: $period")
        if (mapCloudPackage.isNotEmpty()) {
            val listData = mapCloudPackage[period]?.filter { it.status != 0 } ?: listOf()
            if (packageSelected == null) {
                val data = listData?.find { it.descriptions?.noteVi == "30 ngày" }
                data?.let {
                    packageSelected = it
                    binding.tvMoney.text =
                        "${DecimalFormat("#,###,###,###").format(packageSelected?.price)}đ"
                }
            }
            if (packageSelected != null) {
                listData?.forEach {
                    it.isSelected = it.code == packageSelected?.code
                }
            }


            storageTimeAdapter.submitList(listData)
        }
    }

    private fun setUIEnableRegister(isEnable: Boolean) {
        if (isEnable) {
            binding.btnPayment.isClickable = true
            binding.btnPayment.isEnabled = true
            binding.btnPayment.setBackgroundResource(R.drawable.bg_corner_16_top_bottom_f8214b)
        } else {
            binding.btnPayment.isClickable = true
            binding.btnPayment.isEnabled = true
            binding.btnPayment.setBackgroundResource(R.drawable.bg_corner_16_top_bottom_cecece)
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
                        dismiss()
                        showCustomToast(resTitle = com.vht.sdkcore.R.string.string_refer_code_success, onFinish = {
                        }, showImage = true)
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
    }


}