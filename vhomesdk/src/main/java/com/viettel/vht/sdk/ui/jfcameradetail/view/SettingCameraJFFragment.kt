package com.viettel.vht.sdk.ui.jfcameradetail.view

import android.annotation.SuppressLint
import android.graphics.Paint
import android.os.Bundle
import android.text.TextUtils
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import com.basic.G
import com.lib.EFUN_ERROR
import com.lib.SDKCONST
import com.lib.SDKCONST.SDK_FileSystemDriverTypes
import com.lib.sdk.bean.StorageInfoBean
import com.vht.sdkcore.base.BaseFragment
import com.vht.sdkcore.network.Status
import com.vht.sdkcore.utils.Define
import com.vht.sdkcore.utils.dialog.CommonAlertDialogNotification
import com.vht.sdkcore.utils.dialog.DialogType
import com.vht.sdkcore.utils.gone
import com.vht.sdkcore.utils.loadImage
import com.vht.sdkcore.utils.showCustomToast
import com.viettel.vht.sdk.R
import com.viettel.vht.sdk.databinding.FragmentSettingCameraJfBinding
import com.viettel.vht.sdk.eventbus.EventBusPushInfo
import com.viettel.vht.sdk.funtionsdk.VHomeSDKManager
import com.viettel.vht.sdk.jfmanager.CommandJF
import com.viettel.vht.sdk.jfmanager.JFCameraManager
import com.viettel.vht.sdk.model.camera_cloud.CloudStorageRegistered
import com.viettel.vht.sdk.navigation.AppNavigation
import com.viettel.vht.sdk.ui.jfcameradetail.smart_feature.IAlarmPushSetView
import com.viettel.vht.sdk.ui.jfcameradetail.smart_feature.IHumanDetectView
import com.viettel.vht.sdk.ui.jfcameradetail.update_firmware.UpdateFirmwareCameraJFViewModel
import com.viettel.vht.sdk.ui.jfcameradetail.view.dialog.BottomSheetSettingCameraJFFragment
import com.viettel.vht.sdk.ui.jfcameradetail.view.dialog.BottomSheetSettingDateFormatJFFragment
import com.viettel.vht.sdk.ui.jfcameradetail.view.dialog.BottomSheetSettingVolumeCameraJFFragment
import com.viettel.vht.sdk.ui.jfcameradetail.view.dialog.BottomSheetSmartSettingNotificationCameraJFFragment
import com.viettel.vht.sdk.ui.jfcameradetail.view.dialog.SmartSettingNotificationCameraJF
import com.viettel.vht.sdk.ui.jfcameradetail.view.dialog.WhiteLight
import com.viettel.vht.sdk.utils.Config
import com.viettel.vht.sdk.utils.DebugConfig
import com.viettel.vht.sdk.utils.showCustomNotificationDialog
import com.viettel.vht.sdk.utils.showDialogEditNameCameraJF
import com.viettel.vht.sdk.utils.visible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class SettingCameraJFFragment :
    BaseFragment<FragmentSettingCameraJfBinding, SettingCameraJFViewModel>(), IHumanDetectView,
    IAlarmPushSetView {

    @Inject
    lateinit var appNavigation: AppNavigation

    @Inject
    lateinit var vHomeSDKManager: VHomeSDKManager


    override val layoutId = R.layout.fragment_setting_camera_jf

    private val viewModel: SettingCameraJFViewModel by activityViewModels()

    private val cardSettingViewModel: CardSettingJFViewModel by viewModels()
    private val updateFirmwareViewModel: UpdateFirmwareCameraJFViewModel by viewModels()

    override fun getVM() = viewModel

    private var nameCamera: String = ""

//    private var listCloudPackage : List<CloudRegistered>? = null

    override fun initView() {
        super.initView()
        viewModel.setIHumanDetectView(this)
        viewModel.setIAlarmPushSetView(this)
        initData()
        binding.tvSettingNotification.paintFlags = Paint.UNDERLINE_TEXT_FLAG
        binding.btnChangePasswordImageEncoding.paintFlags = Paint.UNDERLINE_TEXT_FLAG
    }

    private fun initData() {
        arguments?.let {
            if (it.containsKey(Define.BUNDLE_KEY.PARAM_ID)) {
                viewModel.deviceId = it.getString(Define.BUNDLE_KEY.PARAM_ID) ?: ""
            }
            if (it.containsKey(Define.BUNDLE_KEY.PARAM_NAME)) {
                nameCamera = it.getString(Define.BUNDLE_KEY.PARAM_NAME) ?: ""
                binding.tvNameCamera.text = nameCamera
            }
            if (it.containsKey(Define.BUNDLE_KEY.PARAM_DEVICE_SERIAL)) {
                viewModel.devId = it.getString(Define.BUNDLE_KEY.PARAM_DEVICE_SERIAL) ?: ""
            }
            if (it.containsKey(Define.BUNDLE_KEY.PARAM_MODEL_CAMERA)) {
                if (it.getString(Define.BUNDLE_KEY.PARAM_MODEL_CAMERA) == Define.CAMERA_MODEL.CAMERA_JF_INDOOR) {
                    binding.imvSettingMore.loadImage(com.vht.sdkcore.R.drawable.ic_product_camera_hc23)
                    binding.tvFollowAction.isVisible = true
                    binding.swTurnOffFollowAction.isVisible = true
                } else if (it.getString(Define.BUNDLE_KEY.PARAM_MODEL_CAMERA) == Define.CAMERA_MODEL.CAMERA_JF_OUTDOOR) {
                    binding.imvSettingMore.loadImage(com.vht.sdkcore.R.drawable.ic_product_camera_hc33)
                    binding.tvFollowAction.isVisible = false
                    binding.swTurnOffFollowAction.isVisible = false
                }
            }
        }

        viewModel.getSystemFunction()
    }

    private fun getConfigDev() {
        arguments?.let {
            if (it.containsKey(Define.BUNDLE_KEY.PARAM_DEVICE_SERIAL)) {
                viewModel.devId = it.getString(Define.BUNDLE_KEY.PARAM_DEVICE_SERIAL) ?: ""
                cardSettingViewModel.devId =
                    it.getString(Define.BUNDLE_KEY.PARAM_DEVICE_SERIAL) ?: ""
//                nameCamera = DevDataCenter.getInstance()?.getDevInfo(viewModel.devId)?.devName.toString()
                if (
                    JFCameraManager.isSupportStatusLed(viewModel.devId) &&
                    it.getString(Define.BUNDLE_KEY.PARAM_MODEL_CAMERA) == Define.CAMERA_MODEL.CAMERA_JF_INDOOR
                ) {
                    binding.tvSettingOnOffLed.isVisible = true
                    binding.swTurnOffLED.isVisible = true
                    JFCameraManager.getStatusLED(viewModel.devId) { fb ->
                        viewModel.currentStatusLEDBean = fb
                        lifecycleScope.launchWhenStarted {
                            binding.swTurnOffLED.isChecked = fb?.ison == SDKCONST.Switch.Open
                        }
                    }
                } else {
                    binding.tvSettingOnOffLed.isVisible = false
                    binding.swTurnOffLED.isVisible = false
                }
                if (JFCameraManager.isSupportOneKeyMaskVideo(viewModel.devId)) {
                    binding.tvPrivateMode.isVisible = true
                    binding.swStatusPrivateMode.isVisible = true
                    JFCameraManager.getStatusMask(viewModel.devId) { isEnable ->
                        lifecycleScope.launchWhenStarted {
                            binding.swStatusPrivateMode.isChecked = isEnable
                        }
                    }
                } else {
                    binding.tvPrivateMode.isVisible = false
                    binding.swStatusPrivateMode.isVisible = false
                }
                JFCameraManager.getDetectTrack(viewModel.devId) { result ->
                    lifecycleScope.launchWhenStarted {
                        viewModel.currentDetectTrackBean = result
                        binding.swTurnOffFollowAction.isChecked = result?.enable == 1
                    }
                }
            }

//            if (it.containsKey(Define.BUNDLE_KEY.PARAM_LIST_CLOUD)) {
//                listCloudPackage = it.getParcelableArrayList(Define.BUNDLE_KEY.PARAM_LIST_CLOUD)
//            }
        }
        setUpSdCard()
        viewModel.getConfig()
        cardSettingViewModel.getConfig()
        showHideLoading(true)
        viewModel.getListCloudStorageRegistered(viewModel.devId)

        viewModel.getCheckInvitationCode()
//        setUpCloudStatus()
        updateFirmwareViewModel.checkHasNewFirmware()
    }

    private fun setUpCloudStatus(listCloudPackage: List<CloudStorageRegistered>?) {
        if (!listCloudPackage.isNullOrEmpty()) {
            binding.tvInforCloud.text = getString(com.vht.sdkcore.R.string.expired_cloud)
            binding.tvInforCloud.setTextColor(requireContext().getColor(R.color.text_EC0D3A))
            listCloudPackage.forEach {
                if (it.serviceStatus == 1) {
                    binding.tvInforCloud.text = getString(com.vht.sdkcore.R.string.active_cloud)
                    binding.tvInforCloud.setTextColor(requireContext().getColor(R.color.color_active_cloud))
                    return@forEach
                }
            }
        } else {
            binding.tvInforCloud.text = getString(com.vht.sdkcore.R.string.dont_register)
            binding.tvInforCloud.setTextColor(requireContext().getColor(R.color.text_EC0D3A))
        }
    }

    private fun setUpSdCard() {
        binding.tvInforCard.setOnClickListener {
            CommonAlertDialogNotification
                .getInstanceCommonAlertdialog(requireContext())
                .showDialog()
                .showCenterImage(DialogType.ERROR)
                .setDialogTitleWithString("Không có thẻ nhớ hoặc thẻ nhớ lỗi.")
                .setTextPositiveButtonWithString("ĐÃ HIỂU")
                .setOnPositivePressed { dialog ->
                    dialog.dismiss()
                }
        }
        cardSettingViewModel.storageInfoBeanListLiveData.observe(viewLifecycleOwner) {
            lifecycleScope.launchWhenStarted {
                if (it?.isNotEmpty() == true) {
                    val storageInfoBeans: List<StorageInfoBean> = it
                    var sumRemainSize: Long = 0
                    var sumTotalSize: Long = 0
                    for (storageInfo in storageInfoBeans) {
                        if (storageInfo.Partition.isNullOrEmpty()) {
                            continue
                        }
                        for (partition in storageInfo.Partition) {
                            if (null == partition) {
                                continue
                            }
                            val remainSize = G.getLongFromHex(partition.RemainSpace)
                            val totalSize = G.getLongFromHex(partition.TotalSpace)
                            if (partition.DirverType == SDK_FileSystemDriverTypes.SDK_DRIVER_READ_WRITE.toLong()
                                || partition.DirverType == SDK_FileSystemDriverTypes.SDK_DRIVER_IMPRCD.toLong()
                            ) {
                                sumRemainSize += remainSize
                                sumTotalSize += totalSize
                            } else if (partition.DirverType == SDK_FileSystemDriverTypes.SDK_DRIVER_SNAPSHOT.toLong()) {
                                sumRemainSize += remainSize
                                sumTotalSize += totalSize
                            }
                        }
                    }
                    if (sumTotalSize > 0) {
                        binding.tvInforCard.text = getString(com.vht.sdkcore.R.string.used) + " " +
                                String.format(
                                    "%.1f",
                                    (sumTotalSize - sumRemainSize) / 1024.0
                                ) + "/" + String.format(
                            "%.1f GB",
                            sumTotalSize / 1024.0
                        )
                        binding.tvInforCard.setTextColor(requireContext().getColor(com.vht.sdkcore.R.color.black_sdk))
//                        if (sumRemainSize < (sumTotalSize * 20 / 100)) {
//                            binding.tvInforCard.text = getText(com.vht.sdkcore.R.string.string_full_memory_card)
//                            binding.tvInforCard.setTextColor(requireContext().getColor(R.color.text_EC0D3A))
//                        } else {
//                            binding.tvInforCard.text =
//                                String.format("%.1f GB", sumRemainSize / 1024.0)
//                            binding.tvInforCard.setTextColor(requireContext().getColor(R.color.black))
//                        }
                        binding.tvInforCard.setOnClickListener {
                            val bundle = Bundle()
                            bundle.putString(Define.BUNDLE_KEY.PARAM_ID, viewModel.devId)
                            //todo open setting card
                          //  appNavigation.openCardSettingJFCamera(bundle)
                        }
                    } else {
                        binding.tvInforCard.text = getText(com.vht.sdkcore.R.string.string_no_memory_card)
                        binding.tvInforCard.setTextColor(requireContext().getColor(R.color.text_EC0D3A))
                    }
                } else {
                    binding.tvInforCard.text = getText(com.vht.sdkcore.R.string.string_no_memory_card)
                    binding.tvInforCard.setTextColor(requireContext().getColor(R.color.text_EC0D3A))
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun bindingStateView() {
        super.bindingStateView()
        viewModel.errorSDKId.observe(viewLifecycleOwner) {
            when {
                it == EFUN_ERROR.EE_DVR_CONNECT_DEVICE_ERROR -> {
                    binding.groupEncrypt.gone()
                    binding.clError.visible()
                    binding.ivError.setImageResource(R.drawable.ic_device_offline_camera_jf)
                    binding.tvError.text = "Camera đang ngoại tuyến"
                    binding.tvErrorDicription.text = ""
                    binding.tvErrorDicription.gone()
                }

                it == EFUN_ERROR.EE_DVR_PASSWORD_NOT_VALID -> {
                    binding.groupEncrypt.gone()
                    binding.clError.visible()
                    binding.ivError.setImageResource(R.drawable.ic_device_encrypt_camera_jf)
                    binding.tvError.text = "Camera đã được mã hóa"
                    binding.tvErrorDicription.text = ""
                    binding.tvErrorDicription.gone()
                }

                it == EFUN_ERROR.EE_DVR_USER_LOCKED -> {
                    binding.groupEncrypt.gone()
                    binding.clError.visible()
                    binding.ivError.setImageResource(R.drawable.ic_device_lock_camera_jf)
                    binding.tvError.text = "Thiết bị của bạn đang bị khóa"
                    binding.tvErrorDicription.text =
                        "Vui lòng thực hiện Tắt - Bật nguồn thiết bị để mở khóa."
                    binding.tvErrorDicription.visible()
                }

                it < 0 -> {
                    binding.groupEncrypt.gone()
                    binding.clError.visible()
                    binding.ivError.setImageResource(R.drawable.ic_device_error_camera_jf)
                    binding.tvError.text = "Không thể lấy thông tin thiết bị"
                    binding.tvErrorDicription.text = "$it"
                    binding.tvErrorDicription.visible()
                }

                else -> {
                    binding.groupEncrypt.visible()
                    binding.clError.gone()
                    binding.clWhiteLight.isVisible = viewModel.isSupportWhiteLight()
                    getConfigDev()
                }
            }
        }
        viewModel.openSettingLiveEvent.observe(viewLifecycleOwner, observer = {
            when (it.keyId) {
                Config.EventKey.EVENT_UPDATE_FW_CAMERA -> {
                    Bundle().apply {
                        putString(Define.BUNDLE_KEY.PARAM_DEVICE_SERIAL, viewModel.devId)
                    }.let { bundle ->

                        //todo open mowr FirmareJF
                        //appNavigation.openUpdateFirmwareJFCamera(bundle)
                    }
                }

                Config.EventKey.EVENT_SETTING_CAMERA_SETTING_EDIT_NAME -> {
                    showDialogEditNameCameraJF(nameCamera, positiveListener = { nameEdit ->
                        if (TextUtils.isEmpty(nameEdit)) {
                            lifecycleScope.launchWhenStarted {
                                CommonAlertDialogNotification
                                    .getInstanceCommonAlertdialog(requireContext())
                                    .showDialog()
                                    .showCenterImage(DialogType.ERROR)
                                    .setDialogTitleWithString("Tên thiết bị không được để trống.")
                                    .hideOptionButton()
                                delay(3000)
                                CommonAlertDialogNotification
                                    .getInstanceCommonAlertdialog(requireContext())
                                    .dismiss()
                            }
                        } else if (nameEdit == nameCamera) {
                            lifecycleScope.launchWhenStarted {
                                CommonAlertDialogNotification
                                    .getInstanceCommonAlertdialog(requireContext())
                                    .showDialog()
                                    .showCenterImage(DialogType.ERROR)
                                    .setDialogTitleWithString("Tên thiết bị bạn vừa đặt đã tồn tại trong hệ thống.")
                                    .hideOptionButton()
                                delay(3000)
                                CommonAlertDialogNotification
                                    .getInstanceCommonAlertdialog(requireContext())
                                    .dismiss()
                            }
                        } else {
                            viewModel.setEditNameDevice(nameEdit) { state ->
                                if (state) {
                                    CommonAlertDialogNotification
                                        .getInstanceCommonAlertdialog(requireContext())
                                        .showDialog()
                                        .showCenterImage(DialogType.SUCCESS)
                                        .setDialogTitleWithString("Đổi tên camera thành công")
                                        .hideOptionButton()
                                    nameCamera = nameEdit
                                    arguments?.putString(Define.BUNDLE_KEY.PARAM_NAME, nameCamera)
                                    binding.tvNameCamera.text = nameCamera
                                } else {
                                    CommonAlertDialogNotification
                                        .getInstanceCommonAlertdialog(requireContext())
                                        .showDialog()
                                        .showCenterImage(DialogType.ERROR)
                                        .setDialogTitleWithString("Đổi tên camera thất bại")
                                        .hideOptionButton()
                                }
                                lifecycleScope.launchWhenStarted {
                                    delay(3000)
                                    CommonAlertDialogNotification
                                        .getInstanceCommonAlertdialog(requireContext())
                                        .dismiss()
                                }
                            }
                        }
                    })
                }

                Config.EventKey.EVENT_SETTING_CAMERA_OPEN_INFORMATION_CAMERA -> {
                    val bundle = Bundle()
                    bundle.putString(Define.BUNDLE_KEY.PARAM_ID, viewModel.devId)
                    bundle.putString(Define.BUNDLE_KEY.PARAM_NAME, nameCamera)
                    if (arguments?.containsKey(Define.BUNDLE_KEY.PARAM_MODEL_CAMERA) == true) {
                        bundle.putString(
                            Define.BUNDLE_KEY.PARAM_MODEL_CAMERA,
                            arguments?.getString(Define.BUNDLE_KEY.PARAM_MODEL_CAMERA)
                        )
                    }

                    //todo open info setting camera
                   // appNavigation.openInfoSettingCameraJF(bundle)
                }

                Config.EventKey.EVENT_SETTING_CAMERA_REBOOT_DEVICE -> {
                    Timber.tag(TAG).d("EVENT_SETTING_CAMERA_REBOOT_DEVICE")
                    CommonAlertDialogNotification.getInstanceCommonAlertdialog(requireContext())
                        .showDialog()
                        .setDialogTitleWithString("Bạn có chắc muốn khởi động lại camera")
                        .setTextPositiveButtonWithString("XÁC NHẬN")
                        .setTextNegativeButtonWithString("HỦY")
                        .showCenterImage(DialogType.CONFIRM)
                        .setOnNegativePressed { dialog ->
                            dialog.dismiss()
                        }
                        .setOnPositivePressed { dialog ->
                            dialog.dismiss()
                            lifecycleScope.launch {
                                try {
                                    showHideLoading(true)
                                    viewModel.rebootDev { isSuccess ->
                                        showHideLoading(false)
                                        CommonAlertDialogNotification
                                            .getInstanceCommonAlertdialog(requireContext())
                                            .showDialog()
                                            .showCenterImage(if (isSuccess) DialogType.SUCCESS else DialogType.ERROR)
                                            .setDialogTitleWithString(if (isSuccess) "Khởi động lại thành công" else "Khởi động lại thất bại")
                                            .setTextPositiveButtonWithString("OK")
                                            .setOnPositivePressed { dialog ->
                                                dialog.dismiss()
                                            }
                                    }
                                } catch (e: Exception) {
                                    showHideLoading(false)
                                    showCustomNotificationDialog(
                                        "Đang mất kết nối, bạn vui lòng kiểm tra lại kết nối và thử lại",
                                        DialogType.ERROR,
                                        titleBtnConfirm = com.vht.sdkcore.R.string.notification_understand,
                                    ) {}
                                }
                            }
                        }
                }

                Config.EventKey.EVENT_SETTING_CAMERA_RESET_DEFAULT -> {
                    showCustomNotificationDialog(
                        getString(com.vht.sdkcore.R.string.confirm_continue),
                        DialogType.CONFIRM,
                        com.vht.sdkcore.R.string.confirm_reset_default,
                        com.vht.sdkcore.R.string.regiter_dialog_button_continue,
                        com.vht.sdkcore.R.string.dialog_button_cancel_2,
                    ) {
                        lifecycleScope.launch {
                            try {
                                showHideLoading(true)
                                viewModel.rebootOrReset(CommandJF.RESET.cmd) { isSuccess ->
                                    showHideLoading(false)
                                    if (!isSuccess) {
                                        showCustomNotificationDialog(
                                            getString(com.vht.sdkcore.R.string.disconnected_camera),
                                            DialogType.ERROR,
                                            titleBtnConfirm = com.vht.sdkcore.R.string.notification_understand,
                                        ) {
                                        }
                                    }
                                }
                            } catch (e: Exception) {
                                showHideLoading(false)
                                showCustomNotificationDialog(
                                    getString(com.vht.sdkcore.R.string.disconnected_camera),
                                    DialogType.ERROR,
                                    titleBtnConfirm = com.vht.sdkcore.R.string.notification_understand,
                                ) {
                                }
                            }
                        }
                    }
                }

                Config.EventKey.EVENT_SETTING_CAMERA_FACTORY_RESET -> {
                    showCustomNotificationDialog(
                        getString(com.vht.sdkcore.R.string.confirm_continue),
                        DialogType.CONFIRM,
                        com.vht.sdkcore.R.string.confirm_factory_reset,
                        com.vht.sdkcore.R.string.regiter_dialog_button_continue,
                        com.vht.sdkcore.R.string.dialog_button_cancel_2,
                    ) {
                        lifecycleScope.launch {
                            try {
                                showHideLoading(true)
                                viewModel.rebootOrReset(CommandJF.RESET.cmd) { isSuccess ->
                                    showHideLoading(false)
                                    if (!isSuccess) {
                                        showCustomNotificationDialog(
                                            getString(com.vht.sdkcore.R.string.disconnected_camera),
                                            DialogType.ERROR,
                                            titleBtnConfirm = com.vht.sdkcore.R.string.notification_understand,
                                        ) {
                                        }
                                    }
                                }
                            } catch (e: Exception) {
                                showHideLoading(false)
                                showCustomNotificationDialog(
                                    getString(com.vht.sdkcore.R.string.disconnected_camera),
                                    DialogType.ERROR,
                                    titleBtnConfirm = com.vht.sdkcore.R.string.notification_understand,
                                ) {
                                }
                            }
                        }

                    }
                }
            }
        })

        viewModel.deleteResponse.observe(viewLifecycleOwner, observer = {
            when (it.status) {
                Status.LOADING -> {
                    showHideLoading(true)
                }

                Status.SUCCESS -> {
//                    showDeviceList()
                    showHideLoading(false)
                    EventBus.getDefault().post(
                        EventBusPushInfo(
                            viewModel.devId,
                            EventBusPushInfo.PUSH_OPERATION.REMOVE_DEV,
                            null
                        )
                    )
                    showCustomToast(
                        resTitleString = "Xóa thiết bị thành công",
                        showImage = true,
                        onFinish = {
                           // appNavigation.popBackStackToHome()
                            requireActivity().finish()
                            vHomeSDKManager.vHomeDetailCameraJFSDKListener?.onDeleteCameraJF(true)
                        })
                }

                Status.ERROR -> {
                    showHideLoading(false)
                    showAlertDialog("Lỗi đồng bộ thiết bị")
                    Timber.tag("ERROR").e("${it.exception}")
                    vHomeSDKManager.vHomeDetailCameraJFSDKListener?.onDeleteCameraJF(false)
                }
            }
        })

        viewModel.dataCheckInvitationCode.observe(viewLifecycleOwner) {
            when (it.status) {
                Status.SUCCESS -> {
                    it.data?.spreadCamera?.status.let { data ->
                        if (data == "enable") {
                            viewModel.getCheckSpreadCamera(viewModel.deviceId)
                        }else{
                            binding.clSettingSpreadCamera.gone()
                        }
                    }
                }
                Status.ERROR ->{
                    binding.clSettingSpreadCamera.gone()
                }
                else -> {

                }
            }
        }

        viewModel.dataResponseCheckSpreadCamera.observe(viewLifecycleOwner) {

            when (it.status) {
                Status.LOADING -> showHideLoading(false)
                Status.ERROR -> {
                    showHideLoading(false)
                    binding.clSettingSpreadCamera.gone()

                }

                Status.SUCCESS -> {
                    it.data?.let { response ->
                        if (response.code == 200) {
                            binding.clSettingSpreadCamera.visible()
                            if (response.data.isNullOrEmpty()) {
                                binding.tvSettingSpreadCamera.text = "Chưa nhập"
                                binding.tvSettingSpreadCamera.setCompoundDrawablesWithIntrinsicBounds(
                                    0,
                                    0,
                                    R.drawable.ic_back_left,
                                    0
                                )
                                binding.clSettingSpreadCamera.setOnClickListener {
                                    if (isDoubleClick) return@setOnClickListener

                                    val bundle = bundleOf().apply {
                                        putString(Define.BUNDLE_KEY.PARAM_ID, viewModel.deviceId)
                                    }

                                    //todo
                                    //appNavigation.openSpreadCameraFragment(bundle)
                                }
                            } else {
                                binding.clSettingSpreadCamera.gone()
                                binding.tvSettingSpreadCamera.text = response.data
                                binding.tvSettingSpreadCamera.setCompoundDrawablesWithIntrinsicBounds(
                                    0,
                                    0,
                                    0,
                                    0
                                )
                            }

                        } else {
                            binding.clSettingSpreadCamera.gone()
                        }

                    }

                }
            }

        }

        viewModel.isDeleteSuccessDeviceFromJF.observe(viewLifecycleOwner) {
            if (!it) {
                showCustomToast(title = "Đã có lỗi xảy ra khi xóa thiết bị. Vui lòng thử lại.")
            }
        }

        viewModel.notificationAlarm.observe(viewLifecycleOwner) { result ->
            showSettingSchedule(result)
        }
        viewModel.updateAlarmInfoState.observe(viewLifecycleOwner) {
            if (!it) {
                showCustomToast(title = "Cập nhật thất bại", onFinish = {})
            }
        }
        viewModel.systemTimezone.observe(viewLifecycleOwner) {
            binding.tvStatuTimeZone.text = it
        }
        viewModel.getWhiteLightState.observe(viewLifecycleOwner) {
            binding.tvStatusBlackLight.text =
                when (it) {
                    WhiteLight.CLOSE -> "Đen trắng"
                    WhiteLight.AUTO -> "Có màu"
                    WhiteLight.INTELLIGENT -> "Thông minh"
                    WhiteLight.OFF -> "Tắt"
                    else -> ""
                }
        }
        viewModel.setWhiteLightState.observe(viewLifecycleOwner) {
            if (!it) {
                showCustomToast(getString(com.vht.sdkcore.R.string.update_firmware_setting_update_fail))
            }
        }
        viewModel.syncSystemTimezoneState.observe(viewLifecycleOwner) {
            showHideLoading(false)
            binding.imStatusFlipTimeZone.clearAnimation()
            showDialogSyncTimezone(it)
        }
        viewModel.cloudRegistered.observe(viewLifecycleOwner) {
            setUpCloudStatus(it)
        }
        viewModel.currentVolumeBeanState.observe(viewLifecycleOwner) {
            binding.tvValueVolume.text = (it?.leftVolume ?: 0).toString()
        }
        viewModel.currentMicVolumeBeanState.observe(viewLifecycleOwner) {
            binding.tvValueMicSensitivity.text = (it?.leftVolume ?: 0).toString()
        }
        viewModel.dateFormatState.observe(viewLifecycleOwner) {
            binding.tvValueDateFormat.text = it.valueInApp
        }
        viewModel.updateDateFormatState.observe(viewLifecycleOwner) {
            if (!it) {
                showCustomToast(title = "Cập nhật thất bại", onFinish = {})
            }
        }
        updateFirmwareViewModel.isCanUpdateFirmware.observe(viewLifecycleOwner) {
            binding.hasNewFirmWareNotification.isVisible = it
        }
    }

    override fun setOnClick() {
        super.setOnClick()
        activity?.onBackPressedDispatcher?.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    setFragmentResult(RESULT_NAME_DEVICE, nameCamera)
                    appNavigation.navigateUp()
                }
            }
        )
        binding.toolBar.setOnLeftClickListener {
            setFragmentResult(RESULT_NAME_DEVICE, nameCamera)
            appNavigation.navigateUp()
        }
        binding.settingMore.setOnClickListener {
            BottomSheetSettingCameraJFFragment().show(
                childFragmentManager,
                "BottomSheetSettingCameraJFFragment"
            )
        }
        binding.imStatusFlipImage.setOnClickListener {
            flipImageCamera()
        }
        binding.imStatusFlipImageLeftRight.setOnClickListener {
            flipImageCameraLeftRight()
        }
        binding.clDateFormat.setOnClickListener {
            if (viewModel.dateFormatObject == null) return@setOnClickListener
            BottomSheetSettingDateFormatJFFragment.newInstance().show(
                childFragmentManager,
                BottomSheetSettingDateFormatJFFragment.TAG
            )
        }
        binding.clVolume.setOnClickListener {
            if (viewModel.currentVolumeBean == null) return@setOnClickListener
            BottomSheetSettingVolumeCameraJFFragment.newInstance(
                BottomSheetSettingVolumeCameraJFFragment.TYPE_VOLUME
            ).show(
                childFragmentManager,
                BottomSheetSettingVolumeCameraJFFragment.TAG
            )
        }
        binding.clMicSensitivity.setOnClickListener {
            if (viewModel.currentMicVolumeBean == null) return@setOnClickListener
            BottomSheetSettingVolumeCameraJFFragment.newInstance(
                BottomSheetSettingVolumeCameraJFFragment.TYPE_MIC
            ).show(
                childFragmentManager,
                BottomSheetSettingVolumeCameraJFFragment.TAG
            )
        }
        binding.swTurnOffLED.setOnCheckedChangeListener { button, state ->
            if (button.isPressed) {
                showHideLoading(true)
                viewModel.setStatusLED(state) { isSuccess ->
                    lifecycleScope.launchWhenStarted {
                        showHideLoading(false)
                        if (!isSuccess) {
                            binding.swTurnOffLED.isChecked = !state
                            showCustomToast("Cập nhật thất bại")
                        }
                    }
                }
            }
        }
        binding.swStatusPrivateMode.setOnCheckedChangeListener { button, state ->
            if (button.isPressed) {
                showHideLoading(true)
                viewModel.setStatusMask(state) { isSuccess ->
                    lifecycleScope.launchWhenStarted {
                        showHideLoading(false)
                        if (!isSuccess) {
                            binding.swStatusPrivateMode.isChecked = !state
                            showCustomToast("Cập nhật thất bại")
                        }
                    }
                }
            }
        }
        binding.swTurnOffFollowAction.setOnCheckedChangeListener { button, state ->
            if (button.isPressed) {
                showHideLoading(true)
                viewModel.setDetectTrack(state) { isSuccess ->
                    lifecycleScope.launchWhenStarted {
                        showHideLoading(false)
                        if (!isSuccess) {
                            binding.swTurnOffFollowAction.isChecked = !state
                            showCustomToast("Cập nhật thất bại")
                        }
                    }
                }
            }
        }

        binding.tvInforCloud.setOnClickListener {
            val bundle = Bundle().apply {
                putString(Define.BUNDLE_KEY.PARAM_ID, viewModel.deviceId)
                putString(Define.BUNDLE_KEY.PARAM_NAME, nameCamera)
                putString(Define.BUNDLE_KEY.PARAM_DEVICE_SERIAL, viewModel.devId)
                putString(Define.BUNDLE_KEY.PARAM_DEVICE_TYPE, Define.TYPE_DEVICE.CAMERA_JF)
            }
//            listCloudPackage?.let {
//                bundle.putParcelableArrayList(Define.BUNDLE_KEY.PARAM_LIST_CLOUD, ArrayList(it))
//            }
            //todo  open cloud JF
          //  appNavigation.openCloudStorageJFCamera(bundle)
        }

        binding.btDelete.setOnClickListener {
            DebugConfig.log(TAG, "DevId: ${viewModel.devId}")
            CommonAlertDialogNotification.getInstanceCommonAlertdialog(requireContext())
                .showDialog()
                .setDialogTitleWithString("Bạn có chắc muốn xoá?")
                .setContent("Dữ liệu đã xoá không thể khôi phục lại được.")
                .showCenterImage(DialogType.CONFIRM)
                .setTextPositiveButtonWithString("OK")
                .setOnPositivePressed {
                    it.dismiss()
                    viewModel.deleteDevice()
                }
                .setTextNegativeButtonWithString("HỦY")
                .setOnNegativePressed {
                    it.dismiss()
                }
        }

        binding.clTimeZone.setOnClickListener {
            try {
                showHideLoading(true)
                binding.imStatusFlipTimeZone.startAnimation(
                    RotateAnimation(
                        0f, 360f,
                        Animation.RELATIVE_TO_SELF, 0.5f,
                        Animation.RELATIVE_TO_SELF, 0.5f
                    ).apply {
                        duration = 300
                        repeatCount = Animation.INFINITE
                    }
                )
                viewModel.syncDevTimeZone()
                viewModel.syncSystemTimezone()
            } catch (e: Exception) {
                showHideLoading(false)
                binding.imStatusFlipTimeZone.clearAnimation()
                showDialogSyncTimezone(false)
            }
        }

        binding.clNotificationSettingAlarm.setOnClickListener {
            BottomSheetSmartSettingNotificationCameraJFFragment.newInstance().show(
                childFragmentManager,
                "BottomSheetSmartSettingNotificationFragment"
            )
        }
//        binding.tvSettingNotification.setOnClickListener {
//            val bundle = Bundle().apply {
//                putString(Define.BUNDLE_KEY.PARAM_ID, viewModel.devId)
//            }
//            appNavigation.openListScheduleAlarmJFCamera(bundle)
//        }
//
//        binding.clSettingAlarmFeature.setOnClickListener {
//            val bundle = Bundle().apply {
//                putString(Define.BUNDLE_KEY.PARAM_ID, viewModel.devId)
//            }
//            appNavigation.openSettingAlarmFeatureCameraJFFragment(bundle)
//        }
//
//        binding.clWhiteLight.setOnClickListener {
//            if (viewModel.whiteLight == null || viewModel.cameraParam == null) {
//                return@setOnClickListener
//            }
//            BottomSheetSettingWhiteLightCameraJFFragment.newInstance().show(
//                childFragmentManager,
//                BottomSheetSettingWhiteLightCameraJFFragment.TAG
//            )
//        }
//        binding.btnChangePasswordImageEncoding.setOnClickListener {
//            val bundle = Bundle().apply {
//                putString(Define.BUNDLE_KEY.PARAM_ID, viewModel.devId)
//            }
//            appNavigation.openChangePasswordImageEncodingFragment(bundle)
//        }

//        binding.clSettingSpreadCamera.setOnClickListener {
//            appNavigation.openSpreadCameraFragment()
//        }
    }

    private fun flipImageCamera() {
        lifecycleScope.launchWhenResumed {
            try {
                showHideLoading(true)
                startFlipVideoAnimation()
                viewModel.flipCamera {
                    showHideLoading(false)
                    showDialogFlipCamera(it >= 0)
                    binding.imStatusFlipImage.clearAnimation()
                }
            } catch (e: Exception) {
                DebugConfig.loge(TAG, "flipImageCamera e ${e.message}")
                showHideLoading(false)
                showDialogFlipCamera(false)
                binding.imStatusFlipImage.clearAnimation()
            }
        }
    }

    private fun startFlipVideoAnimation() {
        val rotate = RotateAnimation(
            0f, 360f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        )

        rotate.duration = 300
        rotate.repeatCount = Animation.INFINITE
        binding.imStatusFlipImage.startAnimation(rotate)
    }

    private fun flipImageCameraLeftRight() {
        lifecycleScope.launchWhenResumed {
            try {
                showHideLoading(true)
                startFlipVideoAnimationLeftRight()
                viewModel.flipCameraLeftRight {
                    showHideLoading(false)
                    showDialogFlipCamera(it)
                    binding.imStatusFlipImageLeftRight.clearAnimation()
                }
            } catch (e: Exception) {
                DebugConfig.loge(TAG, "flipImageCameraLeftRight e ${e.message}")
                showHideLoading(false)
                showDialogFlipCamera(false)
                binding.imStatusFlipImageLeftRight.clearAnimation()
            }
        }
    }

    private fun startFlipVideoAnimationLeftRight() {
        val rotate = RotateAnimation(
            0f, 360f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        )

        rotate.duration = 300
        rotate.repeatCount = Animation.INFINITE
        binding.imStatusFlipImageLeftRight.startAnimation(rotate)
    }

    private fun showDialogFlipCamera(state: Boolean) {
        if (state) {
            CommonAlertDialogNotification.getInstanceCommonAlertdialog(requireContext())
                .showDialog()
                .showCenterImage(DialogType.SUCCESS)
                .setDialogTitleWithString(getString(com.vht.sdkcore.R.string.String_flip_image_success))
                .hideOptionButton()
        } else {
            CommonAlertDialogNotification.getInstanceCommonAlertdialog(requireContext())
                .showDialog()
                .showCenterImage(DialogType.ERROR)
                .setDialogTitleWithString(getString(com.vht.sdkcore.R.string.format_error))
                .hideOptionButton()
        }
        lifecycleScope.launchWhenStarted {
            delay(3000)
            CommonAlertDialogNotification.getInstanceCommonAlertdialog(requireContext()).dismiss()
        }
    }

    override fun updateHumanDetectResult(isSuccess: Boolean) {
        showHideLoading(false)
    }

    override fun saveHumanDetectResult(isSuccess: Boolean) {
        showHideLoading(false)
    }

    override fun onGetMotionDetectResult(isSuccess: Boolean) {
        showHideLoading(false)
    }

    override fun onSaveConfigResult(isSuccess: Boolean) {
        showHideLoading(false)
    }

    override fun onGetConfigFailed() {
        showHideLoading(false)
    }

    private fun showSettingSchedule(value: SmartSettingNotificationCameraJF) {
        binding.apply {
            when (value) {
                SmartSettingNotificationCameraJF.ALL_DAY_24H -> {
                    tvNotificationSettingAlarmContent.text =
                        getString(com.vht.sdkcore.R.string.notification_smart_setting_24h_title)
                    tvSettingNotification.gone()
                }

                SmartSettingNotificationCameraJF.SCHEDULE -> {
                    tvNotificationSettingAlarmContent.text =
                        getString(com.vht.sdkcore.R.string.notification_smart_setting_scheduled_title)
                    tvSettingNotification.visible()
                }

                SmartSettingNotificationCameraJF.OFF -> {
                    tvNotificationSettingAlarmContent.text =
                        getString(com.vht.sdkcore.R.string.notification_smart_setting_turn_off_title)
                    tvSettingNotification.gone()
                }
            }
        }
    }

    private fun showDialogSyncTimezone(state: Boolean) {
        CommonAlertDialogNotification.getInstanceCommonAlertdialog(requireContext())
            .showDialog()
            .showCenterImage(if (state) DialogType.SUCCESS else DialogType.ERROR)
            .setDialogTitleWithString(
                if (state) "Đồng bộ múi giờ thành công" else "Đồng bộ múi giờ thất bại"
            )
            .hideOptionButton()
        lifecycleScope.launchWhenStarted {
            delay(3000)
            CommonAlertDialogNotification.getInstanceCommonAlertdialog(requireContext()).dismiss()
        }
    }

    companion object {
        private val TAG = SettingCameraJFFragment::class.simpleName.toString()
        const val RESULT_NAME_DEVICE = "RESULT_NAME_DEVICE"
    }
}