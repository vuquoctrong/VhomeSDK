package com.viettel.vht.sdk.ui.jfcameradetail.cloud

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.observe
import com.vht.sdkcore.base.BaseFragment
import com.vht.sdkcore.network.Status
import com.vht.sdkcore.pref.RxPreferences
import com.vht.sdkcore.utils.Define
import com.vht.sdkcore.utils.dialog.DialogType
import com.viettel.vht.sdk.R
import com.viettel.vht.sdk.databinding.FragmentJfCloudStorageBinding
import com.viettel.vht.sdk.model.camera_cloud.CloudStorageRegistered
import com.viettel.vht.sdk.navigation.AppNavigation
import com.viettel.vht.sdk.ui.jfcameradetail.cloud.adapter.CloudRegisteredListAdapter
import com.viettel.vht.sdk.utils.DebugConfig
import com.viettel.vht.sdk.utils.enable
import com.viettel.vht.sdk.utils.gone
import com.viettel.vht.sdk.utils.showDialogEnterPhonePayLink
import com.viettel.vht.sdk.utils.visible
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.time.Duration
import java.time.Instant
import javax.inject.Inject

@AndroidEntryPoint
class JFCloudStorageFragment :
    BaseFragment<FragmentJfCloudStorageBinding, JFCloudStorageViewModel>() {

    @Inject
    lateinit var appNavigation: AppNavigation

    @Inject
    lateinit var rxPreferences: RxPreferences

    private val viewModel: JFCloudStorageViewModel by activityViewModels()

    override val layoutId: Int
        get() = R.layout.fragment_jf_cloud_storage

    override fun getVM(): JFCloudStorageViewModel = viewModel

    private var isPostpaid = false

    private var isAutomaticRegisterCloud = false

    // kiểm tra tài khoản đã liên kết chưa
    private var isPayLink = false


    // thể hiện là gói đã hết hạn chưa và cần gia hạn
    private var isExtendCloud = false
    // thông tin bặt tắt gia hạn
    private var serial = ""
    private var statusPayLink = "activate"

    private lateinit var adapter: CloudRegisteredListAdapter

    private var listDataResponse: MutableList<CloudStorageRegistered>? = null

    override fun onResume() {
        super.onResume()
        Timber.d("onResume")
        viewModel.getServerStatusAuto(serial = viewModel.deviceSerial)
        viewModel.getListPaymentLink()
    }

    override fun initView() {
        super.initView()
        arguments?.let {
            if (it.containsKey(Define.BUNDLE_KEY.PARAM_ID)) {
                viewModel.deviceId = it.getString(Define.BUNDLE_KEY.PARAM_ID) ?: ""
            }
            if (it.containsKey(Define.BUNDLE_KEY.PARAM_NAME)) {
                viewModel.deviceName = it.getString(Define.BUNDLE_KEY.PARAM_NAME) ?: ""
            }
            if (it.containsKey(Define.BUNDLE_KEY.PARAM_DEVICE_SERIAL)) {
                viewModel.deviceSerial = it.getString(Define.BUNDLE_KEY.PARAM_DEVICE_SERIAL) ?: ""
            }
            if (it.containsKey(Define.BUNDLE_KEY.PARAM_DEVICE_TYPE)) {
                viewModel.deviceType = it.getString(Define.BUNDLE_KEY.PARAM_DEVICE_TYPE) ?: ""
            }
        }
        binding.tvNameCamera.text = viewModel.deviceName.toString().trim()

        goneUI()
        adapter = CloudRegisteredListAdapter(rxPreferences,onOffAutoChargingListener = {
            // thực hiện bật/tắt gia hạn tự động
            DebugConfig.log("Trong","CloudRegisteredListAdapter click $it")
            if(isPayLink){
                if(it){
                    // thực hiện bật
                    setStatusAutoErrorListAdapter()
                    showDialogNotification(
                        title = "Xác nhận kích hoạt gia hạn tự động",
                        message = com.vht.sdkcore.R.string.string_content_accept_item_pay_link,
                        type = DialogType.CONFIRM,
                        titleBtnConfirm = com.vht.sdkcore.R.string.save_accept,
                        negativeTitle = com.vht.sdkcore.R.string.string_cancel
                    ) {
                        // gọi api lấy OTP
                        statusPayLink = "activate"
                        serial = viewModel.deviceSerial
                        viewModel.getOTPOnOffAutoChargingCloud(serial)
                    }
                }else{
                    // thực hiện tắt
                    setStatusAutoErrorListAdapter()
                    showDialogNotification(
                        title = "Xác nhận hủy gia hạn tự động",
                        message = com.vht.sdkcore.R.string.string_content_reject_item_pay_link,
                        type = DialogType.CONFIRM,
                        titleBtnConfirm = com.vht.sdkcore.R.string.save_accept,
                        negativeTitle = com.vht.sdkcore.R.string.string_cancel
                    ) {
                        // gọi api lấy OTP
                        statusPayLink = "deactivate"
                        serial = viewModel.deviceSerial
                        viewModel.getOTPOnOffAutoChargingCloud(serial)

                    }
                }

            }else{
                if(it){
                    // thực hiện bật
                    setStatusAutoErrorListAdapter()
                    showDialogNotification(
                        title = "Bạn chưa có liên kết tài khoản thanh toán với Viettel Money",
                        type = DialogType.CONFIRM,
                        titleBtnConfirm = com.vht.sdkcore.R.string.continue_link,
                        negativeTitle = com.vht.sdkcore.R.string.string_cancel,
                        message = com.vht.sdkcore.R.string.string_content_show_webview_pay_link,
                        onNegativeClick = {
                            setStatusAutoErrorListAdapter()
                        },
                        onPositiveClick = {
                            showDialogEnterPhonePayLink(onClickOK = {
                                viewModel.getURLRegisterPaymentLink(it)
                            })
                        }
                    )
                }else{
                    // thực hiện tắt
                    setStatusAutoErrorListAdapter()
                    showDialogNotification(
                        title = "Xác nhận hủy gia hạn tự động",
                        message = com.vht.sdkcore.R.string.string_content_reject_item_pay_link,
                        type = DialogType.CONFIRM,
                        titleBtnConfirm = com.vht.sdkcore.R.string.save_accept,
                        negativeTitle = com.vht.sdkcore.R.string.string_cancel
                    ) {
                        // gọi api lấy OTP
                        statusPayLink = "deactivate"
                        serial = viewModel.deviceSerial
                        viewModel.getOTPOnOffAutoChargingCloud(serial)

                    }
                }

            }
        })
        binding.rcvPackage.adapter = adapter
    }




    override fun bindingStateView() {
        super.bindingStateView()

        viewModel.listCloudLiveData.observe(viewLifecycleOwner) {
            when (it) {
                is com.viettel.vht.sdk.network.Result.Loading -> {
                    showHideLoading(true)
                }
                is com.viettel.vht.sdk.network.Result.Error -> {
                    showHideLoading(false)
                    Timber.e("${it.error}")
                    listDataResponse?.clear()
                    goneUI()
                }
                is com.viettel.vht.sdk.network.Result.Success -> {
                    showHideLoading(false)
                    val datas = it.data?.data?.sortedByDescending {
                        it.numberSort()
                    } ?: listOf()
                    listDataResponse?.clear()
                    listDataResponse?.addAll(datas)
                    logicUIListRegister(datas)
                }
            }
        }

        viewModel.statusAutoChargingCloud.observe(viewLifecycleOwner, observer = {
            when (it) {
                is com.viettel.vht.sdk.network.Result.Loading -> {
                    showHideLoading(true)
                }
                is com.viettel.vht.sdk.network.Result.Error -> {
                    showHideLoading(false)
                    Timber.e("${it.error}")
                    listDataResponse?.clear()
                    goneUI()
                }
                is com.viettel.vht.sdk.network.Result.Success -> {
                    showHideLoading(false)
                    isAutomaticRegisterCloud = false
                    if(it.data != null){
                      val dataAuto =  it.data?.data?.first()
                        isAutomaticRegisterCloud = if(dataAuto != null){
                            dataAuto.status =="activate"
                        }else{
                            false
                        }
                    }
                    adapter.isAutomaticRegisterCloud = isAutomaticRegisterCloud
                    viewModel.getListCloudStorage(serial = viewModel.deviceSerial)
                }
            }
        })

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
        // tiến hành liên kết tài khoản
        viewModel.urlRegisterPayLink.observe(viewLifecycleOwner) {
            when (it.status) {
                Status.LOADING -> showHideLoading(true)

                Status.ERROR -> {
                    showHideLoading(false)
                    setStatusAutoErrorListAdapter()
                }

                Status.SUCCESS -> {
                    showHideLoading(false)

                    val urlUnRegister = it.data?.data ?: ""
                    if (urlUnRegister.isNotEmpty()) {
                        val bundle = bundleOf().apply {
                            putString(Define.BUNDLE_KEY.PARAM_URL_REGISTER_PAY_LINK, urlUnRegister)
                        }
                        appNavigation.openRegisterPayLinkFragment(bundle)
                    } else {
                        setStatusAutoErrorListAdapter()
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
        // lắng nghe nhận otp khi  on/off tự động gia hạn
        viewModel.dataOTPOnOffPayLink.observe(viewLifecycleOwner){
            when (it.status) {
                Status.LOADING -> showHideLoading(true)

                Status.ERROR -> {
                    setStatusAutoErrorListAdapter()
                    showHideLoading(false)
                }

                Status.SUCCESS -> {
                    showHideLoading(false)
                    if(it.data?.code == 200){

                        val bundle = bundleOf().apply {
                            putString(Define.BUNDLE_KEY.PARAM_DEVICE_SERIAL,serial)
                            putString(Define.BUNDLE_KEY.PARAM_DATA_ORDER_ID_VT_PAY_WALLET,it.data?.data?:"")
                            putString(Define.BUNDLE_KEY.PARAM_DATA_STATUS_PAYLINK,statusPayLink)

                        }
                        appNavigation.openConfigOTPOnOffPayLinkFragment(bundle)
                    }else{
                        setStatusAutoErrorListAdapter()
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


    }
    private fun setStatusAutoErrorListAdapter(){
        adapter.isAutomaticRegisterCloud = isAutomaticRegisterCloud
        adapter.submitList(adapter.currentList)
    }

    private fun goneUI() {
        binding.rcvPackage.gone()
        binding.btnBuyMore.gone()
        binding.btnExtend.gone()
        binding.btnRegister.gone()
        binding.btnRegister.gone()
        binding.clInformationCamera.gone()
        binding.llNoData.gone()
        binding.rcvPackage.gone()
        binding.toolbar.hideTextRight()
        viewModel.isShowExtendAutoCloud = true
    }

    private fun logicUIListRegister(listData: List<CloudStorageRegistered>) {
        goneUI()
        if (listData.isEmpty()) {
            binding.btnRegister.visible()
            binding.btnRegister.enable()
            binding.llNoData.visible()
            binding.toolbar.hideTextRight()
        } else {
            binding.toolbar.showTextRight()
            binding.toolbar.setSrcRight(R.drawable.ic_history_cloud)
            // danh sách có tất cả các gói cước
            val datas = listData.sortedByDescending {
                it.numberSort()
            } ?: listOf()
            //danh sách trừ gói cước hết hạn
            val resultSkipExpired = datas.filter {
                it.serviceStatus != 0
            }

            if (resultSkipExpired.isEmpty()) {
                val cloudExpired = datas.first { it.serviceStatus == 0 } ?: null
                // không có gói cước hết hạn nào
                if (cloudExpired == null) {
                    binding.btnRegister.visible()
                    binding.btnRegister.enable()
                    binding.llNoData.visible()
                } else {
                    // gói cước hết hạn đó là gói trả sau
                    if (cloudExpired.isPostpaid()) {
//                        binding.rcvPackage.visible()
//                        adapter.submitList(listOf(cloudExpired))
                        // ẩn các thông tin đăng ký và hiển thị text.
                        // là gói cước trả sau nên k hiển thị thông tin gì cả chỉ thiển text
                        //Nếu bạn muốn  thay đổi gói cước. Vui lòng liên hệ tổng đài 18008168
                     //   showTvIsPostpaidContent()
                        // hiện đăng ký mới
                        binding.btnRegister.visible()
                        binding.btnRegister.enable()
                        binding.llNoData.visible()
                    } else {// gói cước hết hạn là gói trả trước. nên có thể gia hạn thêm.
                        // Cần kiểm tra thêm có tự động gia hạn hay k
                        if(isAutomaticRegisterCloud){// có đăng ký gia hạn cho camera. có gói hết hạn/ kiểm tra thời gian trong vòng 30 ngày
                            if(isCheckTimelessThan30Day(cloudExpired.endDateTime?.times(1000L)?:0L)){
                                // hiện gói hết hạn và hiển thị gia hạn
                                binding.rcvPackage.visible()
                                adapter.isExtendCloud = true
                                adapter.submitList(listOf(cloudExpired))
                                binding.clInformationCamera.visible()
                                binding.btnExtend.visible()
                            }else{
                                // hiện đăng ký mới
                                binding.btnRegister.visible()
                                binding.btnRegister.enable()
                                binding.llNoData.visible()
                            }
                        }else{
                            // chưa gia hạn và sẽ k hiển thị gói hết hạn khi k bật tự động gia hạn
                            binding.btnRegister.visible()
                            binding.btnRegister.enable()
                            binding.llNoData.visible()
                        }

                    }
                }

            } else {
                resultSkipExpired.forEach { item ->
                    if (item.isPostpaid()) {
                        isPostpaid = true
                        return@forEach
                    }
                }

                if (!isPostpaid) {
                    // là gói cước trả trước và còn hạn thì có thể mua thêm, nhưng nó sẽ thêm thông tin tự động gia hạn thì phải kiểm tra thêm.

                    if(isAutomaticRegisterCloud){
                        binding.btnBuyMore.gone()
                    }else{
                        // hiện button mua thêm thì sẽ ẩn gia hạn.
                        binding.btnBuyMore.visible()
                    }
                } else {
                    // là gói cước trả sau nên k hiển thị thông tin gì cả chỉ thiển text
                    //Nếu bạn muốn  thay đổi gói cước. Vui lòng liên hệ tổng đài 18008168
                    showTvIsPostpaidContent()

                }
                adapter.submitList(resultSkipExpired)
                binding.rcvPackage.visible()
                binding.clInformationCamera.visible()
            }

        }
    }

    private fun isCheckTimelessThan30Day(time: Long):Boolean{
        val instant = Instant.ofEpochMilli(time)
        // Lấy thời gian hiện tại
        val now = Instant.now()
        // Tính khoảng thời gian giữa hai Instant
        val duration = Duration.between(instant, now)
        // Kiểm tra nếu khoảng thời gian nhỏ hơn 30 ngày
        return duration.toDays() < 30
    }

    override fun setOnClick() {
        super.setOnClick()
        binding.toolbar.setOnLeftClickListener {
            appNavigation.navigateUp()
        }
        binding.btnRegister.setOnClickListener {
            appNavigation.openCloudRegisterJFCamera()
        }
        binding.btnBuyMore.setOnClickListener {
            viewModel.isShowExtendAutoCloud = false
            appNavigation.openCloudRegisterJFCamera()
        }

        binding.btnExtend.setOnClickListener {
            appNavigation.openCloudRegisterJFCamera()
        }

        binding.toolbar.setOnRightClickListener {

            val bundle = Bundle().apply {
                putString(
                    Define.BUNDLE_KEY.PARAM_ID,
                    arguments?.getString(Define.BUNDLE_KEY.PARAM_ID) ?: ""
                )
                putString(Define.BUNDLE_KEY.PARAM_NAME, viewModel.deviceName)
                putString(Define.BUNDLE_KEY.PARAM_ID, viewModel.deviceId)
                putString(Define.BUNDLE_KEY.PARAM_DEVICE_SERIAL, viewModel.deviceSerial)
                putString(Define.BUNDLE_KEY.PARAM_DEVICE_TYPE, Define.TYPE_DEVICE.CAMERA_JF)
                putInt(Define.BUNDLE_KEY.PARAM_TYPE_SCREEN_HISTORY_CLOUD, TYPE_SCREEN_CAMERA)
            }
            appNavigation.openHistoryByCloudStorage(bundle)
        }

    }

    private fun showTvIsPostpaidContent(){
        binding.tvPostpaidContent.visible()
        binding.tvPostpaidContent.setOnClickListener {
            performPhoneCall("18008168")
        }
    }

    private fun performPhoneCall(phoneNumber: String) {
        val intent = Intent(Intent.ACTION_DIAL)
        intent.data = Uri.parse("tel:$phoneNumber")
        try {
            requireContext().startActivity(intent)
        } catch (ex: Exception) {
            Log.e("TAG"," error performPhoneCall")
        }
    }


}