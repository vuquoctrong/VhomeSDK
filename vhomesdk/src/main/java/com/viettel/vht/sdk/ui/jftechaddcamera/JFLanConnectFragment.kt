package com.viettel.vht.sdk.ui.jftechaddcamera

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import androidx.navigation.navGraphViewModels
import com.google.gson.Gson
import com.lib.EFUN_ERROR
import com.manager.db.XMDevInfo
import com.vht.sdkcore.base.BaseFragment
import com.vht.sdkcore.utils.Define
import com.vht.sdkcore.utils.dialog.CommonAlertDialog
import com.vht.sdkcore.utils.gone
import com.vht.sdkcore.utils.showCustomToast
import com.vht.sdkcore.utils.visible
import com.viettel.vht.sdk.jfmanager.JFCameraManager
import com.viettel.vht.sdk.R
import com.viettel.vht.sdk.databinding.FragmentJfLanConnectBinding
import com.viettel.vht.sdk.eventbus.EventBusPushInfo
import com.viettel.vht.sdk.funtionsdk.VHomeSDKManager
import com.viettel.vht.sdk.navigation.AppNavigation
import dagger.hilt.android.AndroidEntryPoint
import org.greenrobot.eventbus.EventBus
import org.json.JSONObject
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class JFLanConnectFragment : BaseFragment<FragmentJfLanConnectBinding, JFTechAddCameraModel>(),
    JFLanDeviceAdapter.Callback {

    private val TAG = JFLanConnectFragment::class.java.simpleName

    @Inject
    lateinit var appNavigation: AppNavigation

    @Inject
    lateinit var vHomeSDKManager: VHomeSDKManager


    override val layoutId: Int
        get() = R.layout.fragment_jf_lan_connect

    private val viewModel: JFTechAddCameraModel by navGraphViewModels(R.id.add_camera_jftech_graph) {
        defaultViewModelProviderFactory
    }

    override fun getVM(): JFTechAddCameraModel = viewModel

    private var loadingAddDeviceDialog: LoadingAddDeviceDialog? = null


    private lateinit var lanDeviceAdapter : JFLanDeviceAdapter


    override fun initView() {
        super.initView()
//        showHideLoading(true)
        JFCameraManager.isAdding = true
        lanDeviceAdapter = JFLanDeviceAdapter()
        lanDeviceAdapter.setCallback(this)
        binding.rcvDevice.adapter = lanDeviceAdapter
        viewModel.searchLanDevice()
        binding.imgSearch.visible()
        binding.llSearch.visible()
    }

    override fun setOnClick() {
        super.setOnClick()
        binding.toolBar.setOnLeftClickListener {
            appNavigation.navigateUp()
        }
        binding.btnRetry.setOnClickListener {
            binding.llNotFound.gone()
            binding.btnRetry.gone()
            binding.llResult.gone()
            binding.tvTitle.gone()
            binding.rcvDevice.gone()
            binding.imgSearch.visible()
            binding.llSearch.visible()
            viewModel.searchLanDevice()
        }
        binding.toolBar.setOnRightClickListener {
//            showHideLoading(true)
            binding.llNotFound.gone()
            binding.btnRetry.gone()
            binding.llResult.gone()
            binding.tvTitle.gone()
            binding.rcvDevice.gone()
            binding.imgSearch.visible()
            binding.llSearch.visible()
            viewModel.searchLanDevice()
        }
    }

    override fun bindingStateView() {
        super.bindingStateView()
        viewModel.localDevList.observe(viewLifecycleOwner) {
//            showHideLoading(false)
            binding.imgSearch.gone()
            binding.llSearch.gone()
            if (it != null && it.isNotEmpty()) {
                lanDeviceAdapter.submitList(it)
                Log.d(TAG, "localDevList: ${Gson().toJson(it)}")
                binding.llResult.visible()
                binding.tvResult.text =
                    String.format("Đã tìm thấy %s thiết bị, chọn thiết bị để thêm", it.size)
                binding.tvTitle.visible()
                binding.rcvDevice.visible()
                if (viewModel.listDeviceInfo.value?.isEmpty() == true) {
                    showHideLoading(true)
                }
            } else {
                binding.llNotFound.visible()
                binding.btnRetry.visible()
            }

        }

        viewModel.listDeviceInfo.observe(viewLifecycleOwner) {
            if (it != null) {
                showHideLoading(false)
            }
        }

        viewModel.uistate.observe(viewLifecycleOwner) { state ->
            isAdding = false
            showHideLoading(false)
            when (state) {
                is JFTechAddCameraModel.UIState.CreateDeviceIOTPlatformSuccess -> {
                    showCustomToast(
                        resTitle = com.vht.sdkcore.R.string.string_add_camera_success,
                        onFinish = {
                            val bundle = Bundle().apply {
                                putSerializable(Define.BUNDLE_KEY.PARAM_DEVICE_ITEM, state.device)
                            }
                            EventBus.getDefault().post(
                                EventBusPushInfo(
                                    viewModel.devID,
                                    EventBusPushInfo.PUSH_OPERATION.ADD_DEV,
                                    null
                                )
                            )
                            vHomeSDKManager.vHomeSDKAddCameraJFListener?.onSuccess(state.device)
                            requireActivity().finish()
                        },
                        showImage = true
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
        JFCameraManager.isAdding = false
    }

    var isAdding = false
    override fun onItemClick(xmDevInfo: XMDevInfo) {
        if (isDoubleClick || isAdding) {
            return
        }
        isAdding = true
        Log.d(TAG, "deviceInfo: ${Gson().toJson(xmDevInfo)}")
        loadingAddDeviceDialog =
            LoadingAddDeviceDialog(getString(com.vht.sdkcore.R.string.string_connect_device_hotspot_retry_error))
        loadingAddDeviceDialog?.isCancelable = false
        if (!loadingAddDeviceDialog!!.isVisible && !loadingAddDeviceDialog!!.isAdded) {
            loadingAddDeviceDialog?.show(childFragmentManager, LoadingAddDeviceDialog.TAG)
        }
        viewModel.addDeviceToAccount(xmDevInfo, callback = {isSuccess, errorId ->
            loadingAddDeviceDialog?.dismiss()
            if (errorId == EFUN_ERROR.EE_ACCOUNT_DEVICE_ALREADY_EXSIT) {
                showCustomToast("Thiết bị đã được thêm. Vui lòng chọn thiết bị khác!")
                isAdding = false
                return@addDeviceToAccount
            } else if (errorId == EFUN_ERROR.EE_ACCOUNT_DEVICE_ADD_NOT_UNIQUE) {
                var message = "Thiết bị đã được thêm bởi tài khoản khác. Bạn vui lòng xóa thiết bị trên tài khoản cũ."
                if (!TextUtils.isEmpty(viewModel.phoneNumberAccountOther)) {
                    message = getString(com.vht.sdkcore.R.string.camera_added, viewModel.phoneNumberAccountOther?.takeLast(3))
                }
                showCustomToast(message)
                isAdding = false
                return@addDeviceToAccount
            }
            if (isSuccess) {
                if (viewModel.devInfo != null && viewModel.devInfo?.systemInfoBean != null) {
                    viewModel.createCameraIOTPlatform(viewModel.devID, viewModel.devInfo?.systemInfoBean?.deviceModel == "R80XV20") // R80XV20 - indoor, 80X20 - outdoor
                } else {
                    JFCameraManager.retryDevInfo = 0
                    JFCameraManager.getDevInfo(viewModel.devID) { result ->
                        if (result.isNotEmpty()) {
                            try {
                                val jsonObj = JSONObject(result)
                                val infoObj =
                                    JSONObject(jsonObj.getString("SystemInfo"))
                                val deviceModel =
                                    infoObj.getString("DeviceModel") // R80XV20 - indoor, 80X20 - outdoor
                                Timber.d("DeviceModel: $deviceModel")
                                showHideLoading(true)
                                viewModel.createCameraIOTPlatform(viewModel.devID, deviceModel == "R80XV20")
                            } catch (e: Exception) {
                                Timber.d("error_json: ${e.message}")
                            }
                        } else {
                            isAdding = false
                            showCustomToast("Có lỗi xảy ra khi lấy thông tin thiết bị. Bạn vui lòng thêm lại thiết bị.") {
                                JFCameraManager.deleteDevice(viewModel.devID) {
                                    showCustomToast("Lỗi đồng bộ thiết bị: $it")
                                }
                            }
                        }
                    }
                }
            } else {

                Timber.e("Lỗi thêm thiết bị: $errorId")
                showCustomToast("Thêm thiết bị thất bại: $errorId")
                isAdding = false
            }
        }, bindingFail = { phoneNumber ->
            loadingAddDeviceDialog?.dismiss()
            var mes = "Thiết bị đã được thêm bởi tài khoản khác. Bạn vui lòng xóa thiết bị trên tài khoản cũ."
            if (!TextUtils.isEmpty(phoneNumber)) {
                mes = getString(
                    com.vht.sdkcore.R.string.camera_binding,
                    phoneNumber?.take(3), phoneNumber?.takeLast(3)
                )
            }
            AddDeviceErrorDialog(message = mes, okAction = {
//                    appNavigation.navigateUp()
            }).show(childFragmentManager, AddDeviceErrorDialog.TAG)
            isAdding = false
        })
    }


}