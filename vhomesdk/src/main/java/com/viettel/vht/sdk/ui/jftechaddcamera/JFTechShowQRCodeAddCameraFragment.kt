package com.viettel.vht.sdk.ui.jftechaddcamera

import android.os.Bundle
import android.os.CountDownTimer
import android.os.Message
import android.text.TextUtils
import android.view.Window
import android.view.WindowManager.LayoutParams
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.lifecycleScope
import androidx.navigation.navGraphViewModels
import com.alibaba.fastjson.JSON
import com.lib.*
import com.lib.sdk.bean.SysDevAbilityInfoBean
import com.vht.sdkcore.base.BaseFragment
import com.vht.sdkcore.utils.Constants
import com.vht.sdkcore.utils.Define
import com.vht.sdkcore.utils.dialog.CommonAlertDialog
import com.vht.sdkcore.utils.dialog.DialogType
import com.vht.sdkcore.utils.gone
import com.vht.sdkcore.utils.showCustomToast
import com.vht.sdkcore.utils.toastMessage
import com.vht.sdkcore.utils.visible
import com.viettel.vht.sdk.jfmanager.JFCameraManager
import com.viettel.vht.sdk.R
import com.viettel.vht.sdk.databinding.FragmentShowQrcodeAddCameraIftechBinding
import com.viettel.vht.sdk.eventbus.EventBusPushInfo
import com.viettel.vht.sdk.navigation.AppNavigation
import com.viettel.vht.sdk.utils.showCustomNotificationDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import org.greenrobot.eventbus.EventBus
import org.json.JSONObject
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext


@AndroidEntryPoint
class JFTechShowQRCodeAddCameraFragment :
    BaseFragment<FragmentShowQrcodeAddCameraIftechBinding, JFTechAddCameraModel>(),
    CoroutineScope, IFunSDKResult {
    val TAG = JFTechShowQRCodeAddCameraFragment::class.java.simpleName

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO

    private val viewModel: JFTechAddCameraModel by navGraphViewModels(R.id.add_camera_jftech_graph) {
        defaultViewModelProviderFactory
    }
    private lateinit var timer: CountDownTimer

    @Inject
    lateinit var appNavigation: AppNavigation
    

    private var isCameraConnect = false

    private var loadingAddDeviceDialog: LoadingAddDeviceDialog? = null

    // Window object, that will store a reference to the current window
    private var window: Window? = null

    private var userId = 0


    override val layoutId = R.layout.fragment_show_qrcode_add_camera_iftech

    override fun getVM(): JFTechAddCameraModel = viewModel

    override fun setOnClick() {
        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    showCustomNotificationDialog(
                        title = getString(com.vht.sdkcore.R.string.string_confirm_back_add_camera),
                        type = DialogType.CONFIRM,
                        message = Constants.ERROR_NUMBER,
                        titleBtnConfirm = com.vht.sdkcore.R.string.verification_code_confirm,
                        negativeTitle = com.vht.sdkcore.R.string.string_cancel
                    ) {
                        appNavigation.navigateUp()
                    }
                }
            }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
        binding.btnSubmit.setOnClickListener {
            showCustomNotificationDialog(
                title = getString(com.vht.sdkcore.R.string.string_confirm_back_add_camera),
                type = DialogType.CONFIRM,
                message = Constants.ERROR_NUMBER,
                titleBtnConfirm = com.vht.sdkcore.R.string.verification_code_confirm,
                negativeTitle = com.vht.sdkcore.R.string.string_cancel
            ) {
                appNavigation.navigateUp()
            }
        }

        binding.toolbar.setOnLeftClickListener {
            showCustomNotificationDialog(
                title = getString(com.vht.sdkcore.R.string.string_confirm_back_add_camera),
                type = DialogType.CONFIRM,
                message = Constants.ERROR_NUMBER,
                titleBtnConfirm = com.vht.sdkcore.R.string.verification_code_confirm,
                negativeTitle = com.vht.sdkcore.R.string.string_cancel
            ) {
                appNavigation.navigateUp()
            }
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Get the current window
        window = activity?.window
    }

    override fun onResume() {
        super.onResume()
        turnUpScreenBrightness()
    }

    private fun turnUpScreenBrightness() {
        val params: LayoutParams = window?.attributes!!
        params.screenBrightness = 1.0f
        window?.attributes = params
    }

    private fun turnDownScreenBrightness() {
        val params: LayoutParams = window?.attributes!!
        params.screenBrightness = -1.0f
        window?.attributes = params
    }


    override fun initView() {
        userId = FunSDK.GetId(userId, this)
        JFCameraManager.isAdding = true
        isCameraConnect = false
        binding.btnSubmit.visible()
        val imageQRCode = viewModel.startSetDevToRouterByQrCode(successAdd = { state, errorCode ->
            if (state) {
                isCameraConnect = true
                timer.cancel()
                // devType: 7 - indoor, 22 - outdoor
                if (viewModel.devInfo != null && viewModel.devInfo?.devType == 7 || viewModel.devInfo?.devType == 22) {
                    viewModel.createCameraIOTPlatform(
                        viewModel.devID,
                        viewModel.devInfo?.devType == 7
                    )
                } else {
                    val bean =  SysDevAbilityInfoBean(viewModel.devID)
                    FunSDK.SysGetDevCapabilitySet(
                        userId,
                        bean.getSendJson(context, SysDevAbilityInfoBean.SYS_ABILITY_SERVICE), -1
                    )
                }

            } else {
                loadingAddDeviceDialog?.dismiss()
                when (errorCode) {
                    EFUN_ERROR.EE_ACCOUNT_DEVICE_ADD_NOT_UNIQUE -> {
                        var message =
                            "Thiết bị đã được thêm bởi tài khoản khác. Bạn vui lòng xóa thiết bị trên tài khoản cũ."
                        if (!TextUtils.isEmpty(viewModel.phoneNumberAccountOther)) {
                            message = getString(
                                com.vht.sdkcore.R.string.camera_added,
                                viewModel.phoneNumberAccountOther?.takeLast(3)
                            )
                        }
                        showCustomToast(message) {
                            appNavigation.navigateUp()
                        }
                    }
                    EFUN_ERROR.EE_ACCOUNT_DEVICE_ALREADY_EXSIT -> {
                        showCustomToast("Thiết bị đã được thêm. Vui lòng chọn thiết bị khác!") {
                            appNavigation.navigateUp()
                        }
                    }
                    else -> {
                        showCustomToast("Có lỗi xảy ra: $errorCode") {
                            appNavigation.navigateUp()
                        }
                    }
                }
            }
        }, failedAdd = { isFailed, message ->
            if (isFailed) {
                loadingAddDeviceDialog?.dismiss()
//                CommonAlertDialog.getInstanceCommonAlertdialog(requireContext())
//                    .showDialog()
//                    .setDialogTitleWithString(getString(R.string.camera_not_own))
//                    .showPositiveButton()
//                    .setOnPositivePressed {
//                        it.dismiss()
//                        appNavigation.navigateUp()
//                    }.setCanceledOnTouchOutside(false)
                var mes = "Thiết bị đã được thêm bởi tài khoản khác. Bạn vui lòng xóa thiết bị trên tài khoản cũ."
                if (!TextUtils.isEmpty(message)) {
                    mes = getString(
                        com.vht.sdkcore.R.string.camera_binding,
                        message?.take(3), message?.takeLast(3)
                    )
                }
                AddDeviceErrorDialog(message = mes, okAction = {
                    appNavigation.navigateUp()
                }).show(childFragmentManager, AddDeviceErrorDialog.TAG)
            }
        }, onDevWiFiState = {
            if (it) {
//                showHideLoading(true)
                binding.btnSubmit.gone()
                if (loadingAddDeviceDialog == null) {
                    loadingAddDeviceDialog =
                        LoadingAddDeviceDialog(getString(com.vht.sdkcore.R.string.string_connect_device_hotspot_retry_error))
                    loadingAddDeviceDialog?.isCancelable = false
                }
                if (!loadingAddDeviceDialog!!.isVisible && !loadingAddDeviceDialog!!.isAdded) {
                    loadingAddDeviceDialog?.show(childFragmentManager, LoadingAddDeviceDialog.TAG)
                }
                stopPingInternetCamera()
            } else {
                if (!isCameraConnect) {
                    lifecycleScope.launchWhenResumed {
                        withContext(Dispatchers.Main) {
                            if (isActive) {
                                if (!isCameraConnect) {
                                    delay(2000)
                                    viewModel.deviceManager.startDevToRouterByQrCode()
                                }

                            }

                        }

                    }
                }
            }

        })

        if (imageQRCode != null) {
            binding.imQRcode.setImageBitmap(imageQRCode)
        } else {
            toastMessage("Mạng phân phối không thành công")
        }
        timer = object : CountDownTimer(181000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                binding?.tvCountdown?.let {
                    it.text = "${millisUntilFinished / 1000}s"
                }
            }

            override fun onFinish() {
                if (!isCameraConnect) {
                    stopPingInternetCamera()
                    showCustomNotificationDialog(
                        title = getString(com.vht.sdkcore.R.string.string_connect_device_hotspot_retry_error),
                        type = DialogType.ERROR
                    ) {
                        appNavigation.navigateUp()
                    }

                }
            }
        }
        timer.start()

    }

    private fun stopPingInternetCamera() {
        viewModel.deviceManager.unInitDevToRouterByQrCode()
    }


    override fun bindingStateView() {
        super.bindingStateView()
        viewModel.uistate.observe(viewLifecycleOwner) { state ->
            loadingAddDeviceDialog?.dismiss()
            when (state) {
                is JFTechAddCameraModel.UIState.CreateDeviceIOTPlatformSuccess -> {
                    showCustomToast(resTitle = com.vht.sdkcore.R.string.string_add_camera_success, onFinish = {
                        val bundle = Bundle().apply {
                            putSerializable(Define.BUNDLE_KEY.PARAM_DEVICE_ITEM, state.device)
                        }
                        appNavigation.openSetPassWordCamera(bundle)
                    }, showImage = true)
                    EventBus.getDefault().post(
                        EventBusPushInfo(
                            viewModel.devID,
                            EventBusPushInfo.PUSH_OPERATION.ADD_DEV,
                            null
                        )
                    )
                }
                is JFTechAddCameraModel.UIState.Error -> {
                    if (!TextUtils.isEmpty(state.message) && state.message!!.contains("409")) {
                        onFail("Thiết bị đã tồn tại.")
                    } else {
                        onFail("Có lỗi xảy ra khi thêm camera.")
                        JFCameraManager.deleteDevice(viewModel.devID) {
                            showCustomToast("Lỗi đồng bộ thiết bị: $it")
                        }
                    }
                }
            }

        }
    }

    private fun onFail(errorMessage: String) {
        CommonAlertDialog.getInstanceCommonAlertdialog(requireContext())
            .showDialog()
            .setDialogTitleWithString(errorMessage)
            .showPositiveButton()
            .setOnPositivePressed {
                it.dismiss()
                appNavigation.navigateUp()
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        timer.cancel()
        JFCameraManager.isAdding = false
        if (!isCameraConnect) {
            stopPingInternetCamera()
        }
//        stopRepeatTask()
    }

    override fun onStop() {
        super.onStop()
        turnDownScreenBrightness()
    }

    override fun OnFunSDKResult(message: Message?, msgContent: MsgContent?): Int {
        if (message?.what == EUIMSG.SYS_GET_DEV_CAPABILITY_SET) {
            val jObj = JSON.parseObject(msgContent?.str)
            var hardware = ""
            if (jObj != null && jObj.containsKey("hw")) {
                hardware = jObj.getString("hw")
            }
            Timber.d("hardware: $hardware")
            if (message.arg1 < 0 || jObj == null || TextUtils.isEmpty(hardware)) {
                if (viewModel.devInfo != null && viewModel.devInfo?.systemInfoBean != null) {
                    viewModel.createCameraIOTPlatform(
                        viewModel.devID,
                        viewModel.devInfo?.systemInfoBean?.deviceModel == "R80XV20"
                    ) // R80XV20 - indoor, 80X20 - outdoor
                } else {
                    JFCameraManager.retryDevInfo = 0
                    JFCameraManager.getDevInfo(viewModel.devID) { result ->
                        if (result.isNotEmpty()) {
                            try {
                                val jsonObj = JSONObject(result)
                                val infoObj = JSONObject(jsonObj.getString("SystemInfo"))
                                val deviceModel =
                                    infoObj.getString("DeviceModel") // R80XV20 - indoor, 80X20 - outdoor
                                Timber.d("DeviceModel: $deviceModel")
                                if (!TextUtils.isEmpty(deviceModel)) {
                                    viewModel.createCameraIOTPlatform(
                                        viewModel.devID,
                                        deviceModel == "R80XV20"
                                    )
                                }
                            } catch (e: Exception) {
                                Timber.d("error_json: ${e.message}")
                            }
                        } else {
                            showCustomToast("Có lỗi xảy ra khi lấy thông tin thiết bị. Bạn vui lòng thêm lại thiết bị.") {
                                JFCameraManager.deleteDevice(viewModel.devID) {
                                    showCustomToast("Lỗi đồng bộ thiết bị: $it")
                                }
                                appNavigation.navigateUp()
                            }
                        }
                    }
                }
                return 0
            }

            if (!TextUtils.isEmpty(hardware)) {
                when (hardware) {
                    "XM530V200_R80XV20_8M" -> { // indoor
                        viewModel.createCameraIOTPlatform(
                            viewModel.devID,
                            true
                        )
                    }
                    "XM530V200_80X20", "IPC_GK7205V200_G4F" -> { //outdoor
                        viewModel.createCameraIOTPlatform(
                            viewModel.devID,
                            false
                        )
                    }
                    else -> {
                        viewModel.createCameraIOTPlatform(
                            viewModel.devID,
                            hardware.contains("R80XV20")
                        )
                    }
                }
            }

        }
        return 0
    }


}
