package com.viettel.vht.sdk.ui.jfcameradetail.view

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.ActionBar
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.net.ConnectivityManager
import android.os.*
import android.text.SpannableString
import android.text.style.StyleSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.AppCompatImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.lib.*
import com.lib.sdk.bean.preset.ConfigGetPreset
import com.manager.device.DeviceManager
import com.manager.device.media.MediaManager.OnMediaManagerListener
import com.manager.device.media.attribute.PlayerAttribute
import com.vht.sdkcore.base.BaseFragment
import com.vht.sdkcore.camera.DefinitionJF
import com.vht.sdkcore.network.Status
import com.vht.sdkcore.pref.RxPreferences
import com.vht.sdkcore.utils.Constants
import com.vht.sdkcore.utils.Define
import com.vht.sdkcore.utils.Define.CAMERA_MODEL.Companion.CAMERA_JF_INDOOR
import com.vht.sdkcore.utils.Utils
import com.vht.sdkcore.utils.UtilsJava
import com.vht.sdkcore.utils.dialog.CommonAlertDialog
import com.vht.sdkcore.utils.dialog.CommonAlertDialogNotification
import com.vht.sdkcore.utils.dialog.DialogType
import com.vht.sdkcore.utils.isNetworkAvailable
import com.vht.sdkcore.utils.margin
import com.vht.sdkcore.utils.showCustomToast
import com.vht.sdkcore.utils.toast
import com.vht.sdkcore.utils.toastMessage
import com.viettel.vht.sdk.R
import com.viettel.vht.sdk.databinding.FragmentJfcameraDetailBinding
import com.viettel.vht.sdk.eventbus.EventBusPushInfo
import com.viettel.vht.sdk.jfmanager.JFCameraManager
import com.viettel.vht.sdk.model.DeviceDataResponse
import com.viettel.vht.sdk.navigation.AppNavigation
import com.viettel.vht.sdk.notification.AlarmPushService
import com.viettel.vht.sdk.ui.jfcameradetail.call.IncomingCallDialog
import com.viettel.vht.sdk.ui.jfcameradetail.forgot_password.RetrievePasswordCameraJFViewModel
import com.viettel.vht.sdk.ui.jfcameradetail.update_firmware.UpdateFirmwareCameraJFViewModel
import com.viettel.vht.sdk.ui.jfcameradetail.view.dialog.BottomSheetPresetFragment
import com.viettel.vht.sdk.ui.jfcameradetail.view.dialog.ConfirmSaveMediaDialog
import com.viettel.vht.sdk.utils.AppEnum
import com.viettel.vht.sdk.utils.AppEnum.CLOUD_TYPE
import com.viettel.vht.sdk.utils.Config
import com.viettel.vht.sdk.utils.Config.EventKey.EVENT_DELETE_PRESET
import com.viettel.vht.sdk.utils.Config.EventKey.EVENT_EDIT_PRESET
import com.viettel.vht.sdk.utils.Config.EventKey.EVENT_MULTIPLE_PRESET
import com.viettel.vht.sdk.utils.Config.EventKey.EVENT_UPDATE_PRESET
import com.viettel.vht.sdk.utils.DebugConfig
import com.viettel.vht.sdk.utils.ImageUtils
import com.viettel.vht.sdk.utils.NetworkChangeReceiver
import com.viettel.vht.sdk.utils.NetworkManager
import com.viettel.vht.sdk.utils.custom.BottomSheetQualityFragment
import com.viettel.vht.sdk.utils.disable
import com.viettel.vht.sdk.utils.enable
import com.viettel.vht.sdk.utils.gone
import com.viettel.vht.sdk.utils.isNotEmpty
import com.viettel.vht.sdk.utils.showCustomNotificationDialog
import com.viettel.vht.sdk.utils.showCustomNotificationDialogPrivate
import com.viettel.vht.sdk.utils.showDialogAddPreset
import com.viettel.vht.sdk.utils.showDialogEditPreset
import com.viettel.vht.sdk.utils.showDialogGuildePreset
import com.viettel.vht.sdk.utils.visible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import timber.log.Timber
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject


@AndroidEntryPoint
class JFCameraDetailFragment : BaseFragment<FragmentJfcameraDetailBinding, JFCameraDetailModel>(),
    NetworkChangeReceiver.ConnectivityReceiverListener {

    val TAG = JFCameraDetailFragment::class.simpleName.toString()

    @Inject
    lateinit var rxPreferences: RxPreferences

    @Inject
    lateinit var appNavigation: AppNavigation
    private var panX = 0F
    private var panY = 0F
    val DEFAULT_SUM_ACTION_PTZ = 5
    var currentActionPTZ: ActionControlPTZ? = null
    private var isExpandQuickAccess = false

    var timer: CountDownTimer? = null

    var stopRecordMedia = false
    private val viewModel: JFCameraDetailModel by viewModels()

    private val retrievePasswordViewModel: RetrievePasswordCameraJFViewModel by viewModels()
    private val updateFirmwareViewModel: UpdateFirmwareCameraJFViewModel by viewModels()
    private var stopAudio = false

    private var mQualityPopupWindow: PopupWindow? = null

    private var isCall = false
    private var devId: String = ""
    private var devName: String = ""
    private var modelCamera: String = ""

    lateinit var adapterJF: JFEventAdapter2
    var currentTab: Int = R.id.tabEvent
    var currentQuality = 0
    private val tabFeatureAdapter by lazy {
        TabFeatureAdapter(
            requireContext(),
            this::onClickItemTabFeature,
            this::onActiveChangemTabFeature
        )
    }
    var sizePan = 0
    var containerX = 0F
    var containerY = 0F
    var sizeContainer = 0
    private var panRightX = 0F
    private var panRightY = 0F
    private var sumActionPTZ = 0
    private var sumActionRightPTZ = 0

    private var jobDefencing: Job? = null

    private var isShareCamera: Boolean = false
    private var permissionJf: String = ""
    private var statusShare: String = ""

    private var deviceJf: DeviceDataResponse? = null
    private var isEnteredPass = false

    private var dialogCallPhone: DialogFragment? = null
    private var jobCallPhone: Job? = null
    private val receiverNetwork = NetworkChangeReceiver()

    private var isActiveDefenceModeOn = false

    override val layoutId: Int
        get() = R.layout.fragment_jfcamera_detail

    override fun getVM(): JFCameraDetailModel {
        return viewModel
    }

    var needShowErrorLiveview = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            if (it.containsKey(Define.BUNDLE_KEY.PARAM_ACTION_CALL)) {
                isCall = it.getBoolean(Define.BUNDLE_KEY.PARAM_ACTION_CALL)
            }
        }
        if (JFCameraManager.isLoginFailed) {
            JFCameraManager.retryLogin = 0
            rxPreferences.getUserJF().let { phone ->
                if (phone.isNotEmpty()) {
                    JFCameraManager.loginAccountJF(
                        user = phone,
                        password = rxPreferences.getTokeJFTech()
                    ) {
                        stopPushServiceCameraJF()
                        startPushServiceCameraJF()
                    }
                }
            }
        }
    }

    private fun startPushServiceCameraJF() {
        Timber.d("startPushServiceCameraJF")
        try {
            val intent = Intent().apply {
                addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
                setClass(requireContext(), AlarmPushService::class.java)
            }
            context?.startService(intent)
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    private fun stopPushServiceCameraJF() {
        context?.stopService(Intent(context, AlarmPushService::class.java))
    }

    override fun onStart() {
        super.onStart()
        context?.registerReceiver(
            receiverNetwork,
            IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        )
        NetworkChangeReceiver.connectivityReceiverListener = this
        EventBus.getDefault().register(this)
    }

    override fun initView() {
        super.initView()
        arguments?.let {
            if (it.containsKey(Define.BUNDLE_KEY.PARAM_ID)) {
                viewModel.deviceId = it.getString(Define.BUNDLE_KEY.PARAM_ID) ?: ""
            }

            if (it.containsKey(Define.BUNDLE_KEY.PARAM_NAME)) {
                devName = it.getString(Define.BUNDLE_KEY.PARAM_NAME) ?: viewModel.deviceId
                binding.tvCameraName.text = devName
                // devId = it.getString(Define.BUNDLE_KEY.PARAM_ID) ?: ""
            }

            if (it.containsKey(Define.BUNDLE_KEY.PARAM_MODEL_CAMERA)) {
                modelCamera = it.getString(Define.BUNDLE_KEY.PARAM_MODEL_CAMERA) ?: ""
            }

            if (it.containsKey(Define.BUNDLE_KEY.PARAM_DEVICE_SERIAL)) {
                devId = it.getString(Define.BUNDLE_KEY.PARAM_DEVICE_SERIAL) ?: ""
                viewModel.devId = devId
                retrievePasswordViewModel.devId = devId
                JFCameraManager.getCameraParamex(viewModel.devId) {
                    viewModel.currentCameraParamBean = it
                }
            }

            if (it.containsKey(Define.BUNDLE_KEY.PARAM_STATUS_SHARE)) {
                statusShare = it.getString(Define.BUNDLE_KEY.PARAM_STATUS_SHARE) ?: ""
            }

            if (it.containsKey(Define.BUNDLE_KEY.PARAM_PERMISSIONS)) {
                permissionJf = it.getString(Define.BUNDLE_KEY.PARAM_PERMISSIONS) ?: ""
            }

            if (it.containsKey(Define.BUNDLE_KEY.PARAM_TYPE_SHARING_DEVICE)) {
                deviceJf =
                    it.getSerializable(Define.BUNDLE_KEY.PARAM_TYPE_SHARING_DEVICE) as DeviceDataResponse
                deviceJf?.let { deviceDataResponse ->
                    isShareCamera = deviceDataResponse.isDeviceShare(rxPreferences.getUserId())
                }
            }

        }
        if (!isShareCamera) {
            setUpPresets()
            retrievePasswordViewModel.registerRecoveryPhoneNumber()
        }
        enableBtnControl(false)
        binding.tvCameraName.setOnClickListener {
            toggleFullScreenMode()
        }
        binding.toolbar.setOnRightClickListener {
            if (checkStopRecord()) {
                return@setOnRightClickListener
            }
            val bundle = Bundle()
            bundle.putString(Define.BUNDLE_KEY.PARAM_ID, viewModel.deviceId)
            bundle.putString(Define.BUNDLE_KEY.PARAM_NAME, devName)
            bundle.putString(
                Define.BUNDLE_KEY.PARAM_DEVICE_SERIAL,
                devId
            )
            if (arguments?.containsKey(Define.BUNDLE_KEY.PARAM_MODEL_CAMERA) == true) {
                bundle.putString(
                    Define.BUNDLE_KEY.PARAM_MODEL_CAMERA,
                    arguments?.getString(Define.BUNDLE_KEY.PARAM_MODEL_CAMERA)
                )
            }
            if (isShareCamera) {
                if (arguments?.containsKey(Define.BUNDLE_KEY.PARAM_TYPE_SHARING_DEVICE) == true) {
                    val deviceJf =
                        arguments?.getSerializable(Define.BUNDLE_KEY.PARAM_TYPE_SHARING_DEVICE) as DeviceDataResponse
                    bundle.putSerializable(Define.BUNDLE_KEY.PARAM_TYPE_SHARING_DEVICE, deviceJf)
                    bundle.putString(
                        Define.BUNDLE_KEY.PARAM_TYPE_SCREEN_OPEN_CREATE_SHARE_CAMERA_JF,
                        Define.BUNDLE_KEY.PARAM_TYPE_SHARE_CAMERA_JF
                    )
                    //todo open setting  share camera
                   // appNavigation.openDetailShareAcceptedReceivedJF(bundle)
                }
            } else {
                // todo open setting camera
                appNavigation.openSettingCameraJF(bundle)
            }
        }
        viewModel.initMonitor(devId, binding.cameraView)
        viewModel.mediaManager.setOnMediaManagerListener(object : OnMediaManagerListener {
            override fun onMediaPlayState(p0: PlayerAttribute<*>?, p1: Int) {
                when (p1) {
                    PlayerAttribute.E_STATE_PlAY -> {
                        DebugConfig.log(TAG, "setOnMediaManagerListener:E_STATE_PlAY ")
                    }
                }
            }

            override fun onFailed(p0: PlayerAttribute<*>?, p1: Int, p2: Int) {

            }

            override fun onShowRateAndTime(
                p0: PlayerAttribute<*>?,
                p1: Boolean,
                p2: String?,
                p3: String?,
            ) {

            }

            override fun onVideoBufferEnd(p0: PlayerAttribute<*>?, p1: MsgContent?) {

            }

            override fun onPlayStateClick(p0: View?) {

            }

        })

        binding.recordTimer.onChronometerTickListener =
            Chronometer.OnChronometerTickListener {
                if (binding.recordTimer.text == "05:00") {
                    stopRecording(true)
                }
            }
        setDefinition(rxPreferences.getTypeDefinitionJF(devId))

        initTabFeature()

        lifecycleScope.launchWhenStarted {
            repeat(Int.MAX_VALUE - 1) {
                JFCameraManager.getWifiRouteSignalLevel(devId) { signalLevel, isLAN ->
                    lifecycleScope.launchWhenStarted {
                        setUIInformationWifiCam(signalLevel, isLAN)
                    }
                }
                delay(10_000L)
            }
        }

//        binding.tabFeature.featureDefence.enable(false)
//        binding.tabFeature.featureShare.enable(false)

        initVideoSize()
//        viewModel.isSupportPtz {
//            if (it) {
//                binding.panLayout.root.visible()
//            } else {
//                binding.panLayout.root.gone()
//            }
//        }

        if (isShareCamera && !permissionJf.contains(Config.FEATURE_EVENT_JF)
            && binding.bottomNavigation.selectedItemId == R.id.tabEvent
        ) {
            binding.ptzNotPermission.visible()
            binding.tvSeeMore.gone()
            binding.rcvEvent.gone()
        } else {
            binding.ptzNotPermission.gone()
            binding.tvSeeMore.visible()
            binding.rcvEvent.visible()
            initListVideo()
        }


        if (!checkCameraIndoor()) {
            binding.bottomNavigation.getMenu().removeItem(R.id.tabPTZ)
        }

        if ((isShareCamera && !permissionJf.contains(Config.FEATURE_PLAYBACK_JF)
                    && !permissionJf.contains(Config.FEATURE_EVENT_JF))
        ) {
            binding.layoutBtnControl.layoutHistory.isClickable = false
            binding.layoutBtnControl.layoutHistory.alpha = 0.5f
        } else {
            binding.layoutBtnControl.layoutHistory.isClickable = true
            binding.layoutBtnControl.layoutHistory.alpha = 1f
        }
        if (!isShareCamera) {
            viewModel.getListPricingPaymentCloud()
            setUITabServiceFreeCloud()
        }

        //  binding.layoutRightSide.rlMain.setPadding(0,0,0,0)
        viewModel.getSystemFunction()
        activity?.resources?.configuration?.orientation?.let { orientation ->
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                displayFullScreenVideo()
                initFullscreenModeJF()
                activity?.requestedOrientation =
                    ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                binding.containerToolbar.visibility = View.GONE
                binding.tvCameraName.visibility = View.VISIBLE
                binding.tvBitrate.visibility = View.GONE
                binding.layoutRightSide.root.visibility = View.VISIBLE
                binding.layoutToolbarFullscreen.root.visibility = View.GONE
                binding.layoutControl.btnTalk.visibility = View.VISIBLE
                panX = 0F
                if (isShareCamera) {
                    binding.layoutControl.btnDefinition.disable()
                } else {
                    binding.layoutControl.btnDefinition.enable()
                }
                if ((isShareCamera && !permissionJf.contains(Config.FEATURE_AUDIO_JF))) {
                    binding.layoutControl.btnTalk.disable()
                } else {
                    binding.layoutControl.btnTalk.enable()
                }
                binding.layoutControl.btnZoomControl.setImageResource(R.drawable.ic_live_view_exit_full_screen)
                binding.quickAccessLayout.root.gone()
                binding.bottomNavigation.gone()
                binding.layoutControl.btnZoomControl.margin(left = 10F, right = 50F)
                if (checkCameraIndoor()) {
                    binding.layoutControl.btnPan.visibility = View.VISIBLE
                    if (!(isShareCamera && !permissionJf.contains(Config.FEATURE_PTZ_JF))) {
                        binding.layoutControl.btnPan.enable()
                    } else {
                        binding.layoutControl.btnPan.disable()
                    }
                }
            } else {
                displayMinimalScreenVideo()
                exitFullScreenModeJF()
                activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                binding.containerToolbar.visibility = View.VISIBLE
                binding.tvCameraName.visibility = View.GONE
                binding.tvBitrate.visibility = View.VISIBLE
                binding.layoutToolbarFullscreen.root.visibility = View.GONE
                binding.layoutRightSide.root.visibility = View.GONE
                binding.layoutControl.btnPan.visibility = View.GONE
                binding.layoutControl.btnTalk.visibility = View.GONE
                panX = 0F
                binding.bottomNavigation.visible()
                binding.quickAccessLayout.root.gone()
                binding.layoutControl.btnZoomControl.setImageResource(R.drawable.ic_live_view_full_screen)
                binding.layoutControl.btnZoomControl.margin(left = 10F, right = 0F)
            }
        }

    }

    private fun setUpPresets() {
        binding.panLayout.containerFeature.visible()
        binding.panLayout.layoutPreset.tvBack.paintFlags = Paint.UNDERLINE_TEXT_FLAG
        binding.panLayout.layoutPreset.tvCount.text = getString(com.vht.sdkcore.R.string.count_selected, "0")
        binding.panLayout.tvGuide.setOnClickListener {
            showDialogGuildePreset(getString(com.vht.sdkcore.R.string.guide_message_control))
        }
        binding.panLayout.layoutPreset.tvGuide.setOnClickListener {
            if (binding.panLayout.layoutPreset.vpPreset.isStatePatrol) {
                showDialogGuildePreset(getString(com.vht.sdkcore.R.string.guide_message_patrol))
            } else {
                showDialogGuildePreset(getString(com.vht.sdkcore.R.string.guide_message_preset))
            }
        }
        binding.panLayout.layoutPreset.tvBack.setOnClickListener {
            if (binding.panLayout.layoutPreset.vpPreset.isCheckMode()) {
                binding.panLayout.layoutPreset.vpPreset.setCheckMode(false)
            } else {
                binding.panLayout.groupPan.visible()
                binding.panLayout.containerFeature.visible()
                binding.panLayout.layoutPreset.root.gone()
            }
        }
        binding.panLayout.layoutPreset.imvBack.setOnClickListener {
            if (binding.panLayout.layoutPreset.vpPreset.isCheckMode()) {
                binding.panLayout.layoutPreset.vpPreset.setCheckMode(false)
            } else {
                binding.panLayout.groupPan.visible()
                binding.panLayout.containerFeature.visible()
                binding.panLayout.layoutPreset.root.gone()
            }
        }
        binding.panLayout.tvSaved.setOnClickListener {
            binding.panLayout.groupPan.gone()
            binding.panLayout.containerFeature.gone()
            binding.panLayout.layoutPreset.root.visible()
            binding.panLayout.layoutPreset.btnTour.gone()
            binding.panLayout.layoutPreset.vpPreset.setState(false)
            viewModel.getTourAndPresets { presets ->
                lifecycleScope.launchWhenStarted {
                    val tour = viewModel.tour.Tour
                    binding.panLayout.layoutPreset.btnTour.isEnabled = tour.size == 3
                    binding.panLayout.layoutPreset.vpPreset.presetsTour = tour
                    binding.panLayout.layoutPreset.vpPreset.setUpList(devId, presets, rxPreferences)
                }
            }
        }
        binding.panLayout.tvPatrol.setOnClickListener {
            binding.panLayout.groupPan.gone()
            binding.panLayout.containerFeature.gone()
            binding.panLayout.layoutPreset.root.visible()
            binding.panLayout.layoutPreset.btnTour.visible()
            binding.panLayout.layoutPreset.vpPreset.setState(true)
            viewModel.getTourAndPresets { presets ->
                lifecycleScope.launchWhenStarted {
                    val tour = viewModel.tour.Tour
                    binding.panLayout.layoutPreset.btnTour.isEnabled = tour.size == 3
                    binding.panLayout.layoutPreset.vpPreset.presetsTour = tour
                    binding.panLayout.layoutPreset.vpPreset.setUpList(devId, presets, rxPreferences)
                }
            }
        }
        binding.panLayout.tvSavePreset.setOnClickListener {
            addPreset()
        }
        binding.panLayout.layoutPreset.tvDelete.setOnClickListener {
            deletePresetChecked()
        }

        binding.panLayout.layoutPreset.btnTour.setOnClickListener {
            showHideLoading(true)
            viewModel.startOrStopTour { isTouring ->
                showHideLoading(false)
                if (isTouring) {
                    binding.panLayout.layoutPreset.btnTour.text = "Kết thúc tuần tra"
                } else {
                    binding.panLayout.layoutPreset.btnTour.text = "Bắt đầu tuần tra"
                }
            }
        }

        viewModel.openOptionLiveEvent.observe(viewLifecycleOwner) {
            when (it.keyId) {
                EVENT_UPDATE_PRESET -> {
                    it.value?.let {
                        updatePreset(it)
                    }
                }

                EVENT_EDIT_PRESET -> {
                    it.value?.let {
                        editPreset(it)
                    }
                }

                EVENT_DELETE_PRESET -> {
//                    it.value?.let {
//                        deletePreset(mutableListOf(it))
//                    }
                    binding.panLayout.layoutPreset.vpPreset.setCheckMode(true)
                }

                EVENT_MULTIPLE_PRESET -> {
                    binding.panLayout.layoutPreset.vpPreset.setCheckMode(true)
                }
            }
        }
        // Preset view pager
        binding.panLayout.layoutPreset.vpPreset.addPresetCallback = {
            addPreset()
        }
        binding.panLayout.layoutPreset.vpPreset.onLongClickItem = { item ->
            item?.let {
                BottomSheetPresetFragment.newInstance(it).show(childFragmentManager)
            }
        }
        binding.panLayout.layoutPreset.vpPreset.onPatrolFull = {
            showCustomNotificationDialog(
                title = "Chỉ được chọn tối đa 3 góc tuần tra",
                type = DialogType.ERROR,
                titleBtnConfirm = com.vht.sdkcore.R.string.ok,
            ) {}
        }
        binding.panLayout.layoutPreset.vpPreset.onClickItem = { item ->
            viewModel.turnPreset(item.item.Id) {}
        }
        binding.panLayout.layoutPreset.vpPreset.changeModeCallback = { isCheckMode ->
            if (isCheckMode) {
                binding.panLayout.layoutPreset.groupDelete.visible()
                binding.panLayout.layoutPreset.tvBack.text = "Góc quay đã lưu"
            } else {
                binding.panLayout.layoutPreset.groupDelete.gone()
                binding.panLayout.layoutPreset.tvBack.text = "Điều khiển"
            }
        }
        binding.panLayout.layoutPreset.vpPreset.onItemCheckedChange = {
            val listItem = binding.panLayout.layoutPreset.vpPreset.getListItem()
                .filter { it.item.Id != -1 && it.item.Id != -2 }
            val countChecked = listItem.count { it.isChecked }
            if (countChecked == listItem.size) {
                binding.panLayout.layoutPreset.tvSelectAll.isChecked = true
                binding.panLayout.layoutPreset.tvDelete.enable()
            } else if (countChecked < listItem.size && countChecked > 0) {
                binding.panLayout.layoutPreset.tvSelectAll.isChecked = false
                binding.panLayout.layoutPreset.tvDelete.enable()
            } else {
                binding.panLayout.layoutPreset.tvSelectAll.isChecked = false
                binding.panLayout.layoutPreset.tvDelete.disable()
            }
            binding.panLayout.layoutPreset.tvCount.text =
                getString(com.vht.sdkcore.R.string.count_selected, countChecked.toString())
        }
        binding.panLayout.layoutPreset.vpPreset.onSelectItemPatrol = {
            showHideLoading(true)
            viewModel.selectItemPatrol(it.item.Id) {
                lifecycleScope.launchWhenStarted {
                    showHideLoading(false)
                    binding.panLayout.layoutPreset.btnTour.isEnabled = it.size == 3
                    binding.panLayout.layoutPreset.vpPreset.updateDataTour(it)
                }
            }
        }
        binding.panLayout.layoutPreset.tvSelectAll.setOnCheckedChangeListener { view, isChecked ->
            if (!view.isPressed) return@setOnCheckedChangeListener
            if (isChecked) {
                binding.panLayout.layoutPreset.vpPreset.selectAll()
            } else {
                binding.panLayout.layoutPreset.vpPreset.unSelectAll()
            }
        }
    }

    private fun editPreset(itemPreset: ItemPreset) {
        showDialogEditPreset(itemPreset.item.PresetName, positiveListener = { presetName ->
            if (viewModel.checkExistPreset(presetName)) {
                toastMessage("Tên góc quay đã được sử dụng")
                return@showDialogEditPreset
            }
            showHideLoading(true)
            viewModel.editNamePreset(
                itemPreset.item.Id,
                presetName
            ) { isSuccess, preset ->
                showHideLoading(false)
                if (isSuccess) {
                    preset?.let {
                        viewModel.updateItemPresetInList(
                            it.Parameter.Preset,
                            it.Parameter.PresetName
                        )
                        viewModel.syncPTZ()
                        binding.panLayout.layoutPreset.vpPreset.updateItem(
                            ItemPreset(
                                ConfigGetPreset().apply {
                                    Id = preset.Parameter.Preset
                                    PresetName = preset.Parameter.PresetName
                                },
                                rxPreferences.getImagePreset("$devId-${preset.Parameter.Preset}")
                            )
                        )
                        showCustomToast(resTitle = com.vht.sdkcore.R.string.edit_preset_success, showImage = true)
                    }
                }
            }
        })
    }

    private fun updatePreset(itemPreset: ItemPreset) {
        viewModel.updatePreset(
            itemPreset.item.Id,
            itemPreset.item.PresetName
        ) { isSuccess, preset ->
            if (isSuccess) {
                preset?.let {
                    viewModel.updateImgPreset(preset.Parameter.Preset)
                    binding.panLayout.layoutPreset.vpPreset.updateItem(
                        ItemPreset(
                            ConfigGetPreset().apply {
                                Id = preset.Parameter.Preset
                                PresetName = preset.Parameter.PresetName
                            },
                            rxPreferences.getImagePreset("$devId-${preset.Parameter.Preset}")
                        )
                    )
                }
            }
        }
    }

    private fun deletePresetChecked() {
        val listChecked = binding.panLayout.layoutPreset.vpPreset.getListItemChecked()
        deletePreset(listChecked)
    }

    private fun deletePreset(list: MutableList<ItemPreset>) {
        val textTitle =
            if (list.size == 1) getString(com.vht.sdkcore.R.string.confirm_delete_preset) else getString(
                com.vht.sdkcore.R.string.confirm_delete_preset_1,
                list.size.toString()
            )
        showCustomNotificationDialog(
            title = textTitle,
            type = DialogType.CONFIRM,
            message = com.vht.sdkcore.R.string.note_delete_preset,
            titleBtnConfirm = com.vht.sdkcore.R.string.string_delete,
            negativeTitle = com.vht.sdkcore.R.string.string_cancel
        ) {
            lifecycleScope.launchWhenStarted {
                try {
                    withTimeout(5000) {
                        showHideLoading(true)
                        viewModel.deletePresets(list.map { it.item.Id }) { listId ->
                            if (listId.isNotEmpty()) {
                                listId.forEach { id ->
                                    viewModel.deleteImgPreset(id)
                                    viewModel.updateWhendeletePresets(id)
                                }
                                viewModel.syncPTZ()
                                val titleDeleteSuccess =
                                    if (list.size == 1) getString(com.vht.sdkcore.R.string.delete_preset_success) else getString(
                                        com.vht.sdkcore.R.string.delete_preset_success_1,
                                        listId.size.toString()
                                    )
                                showCustomToast(
                                    resTitleString = titleDeleteSuccess,
                                    showImage = true
                                )
                                binding.panLayout.layoutPreset.vpPreset.updateDeleted(listId)
                            } else {
                                val titleDeleteFail =
                                    if (list.size == 1) getString(com.vht.sdkcore.R.string.delete_preset_fail) else getString(
                                        com.vht.sdkcore.R.string.delete_preset_success_1,
                                        listId.size.toString()
                                    )
                                showCustomNotificationDialog(
                                    title = titleDeleteFail,
                                    type = DialogType.ERROR,
                                    titleBtnConfirm = com.vht.sdkcore.R.string.ok,
                                ) {}
                            }
                        }
                        showHideLoading(false)
                    }
                } catch (e: Exception) {
                    Log.d(TAG, "deletePresets: delete fail with e = ${e.message}")
                }
            }
        }
    }

    private fun addPreset() {
        if (viewModel.presets.size >= 8) {
            toastMessage("Số góc quay có thể thiết lập tối đa là 8")
            return
        }
        showDialogAddPreset(positiveListener = { presetName ->
            if (viewModel.checkExistPreset(presetName)) {
                toastMessage("Tên góc quay đã được sử dụng")
                return@showDialogAddPreset
            }
            showHideLoading(true)
            viewModel.addPreset(presetName) { isSuccess, preset ->
                lifecycleScope.launchWhenStarted {
                    showHideLoading(false)
                    if (isSuccess) {
                        showCustomToast(resTitle = com.vht.sdkcore.R.string.add_preset_success, showImage = true)
                        preset?.let {
                            viewModel.saveImgPreset(preset.Parameter.Preset)
                            viewModel.updateWhenAddPresets(preset)
                            viewModel.syncPTZ()
                            binding.panLayout.layoutPreset.vpPreset.updateAdded(
                                ItemPreset(
                                    ConfigGetPreset().apply {
                                        Id = preset.Parameter.Preset
                                        PresetName = preset.Parameter.PresetName
                                    },
                                    rxPreferences.getImagePreset("$devId-${preset.Parameter.Preset}")
                                )
                            )
                        }
                    } else {
                        showCustomNotificationDialog(
                            title = "Thông báo",
                            type = DialogType.ERROR,
                            message = com.vht.sdkcore.R.string.add_preset_failed,
                            titleBtnConfirm = com.vht.sdkcore.R.string.ok,
                        ) {}
                    }
                }
            }
        })
    }

    private fun initAdapter() {
        adapterJF = JFEventAdapter2(binding.rcvEvent, requireContext(), devId) {
            if (checkStopRecord()) {
                return@JFEventAdapter2
            }
            lifecycleScope.launch {
                try {
                    val isRegisted = withTimeout(2000L) {
                        viewModel.checkCloudStorageRegistered(devId) ?: false
                    }
                    val bundle = Bundle()
                    bundle.putString(Define.BUNDLE_KEY.PARAM_DEVICE_SERIAL, devId)
                    bundle.putString(Define.BUNDLE_KEY.PARAM_ID, viewModel.deviceId)
                    bundle.putBoolean(Define.BUNDLE_KEY.PARAM_VALUE, isRegisted)
                    bundle.putString(Define.BUNDLE_KEY.PARAM_NAME, devName)
                    val timeStart =
                        SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(it.startTime).time
                    bundle.putLong(Define.BUNDLE_KEY.PARAM_START_TIME, timeStart)
                    appNavigation.openQuickPlaybackJFFragment(bundle)
                } catch (e: Exception) {
                    DebugConfig.log(TAG, "Error checkIsSupportCloud: $e")
                }
            }
        }
        binding.rcvEvent.adapter = adapterJF
    }

    override fun bindingStateView() {
        super.bindingStateView()
        doOnFragmentResult<String>(SettingCameraJFFragment.RESULT_NAME_DEVICE) {
            devName = it
            arguments?.putString(Define.BUNDLE_KEY.PARAM_NAME, devName)
            binding.toolbar.setStringTitle(devName)
        }

        viewModel.actionReloadEvent.observe(viewLifecycleOwner) {
            searchEventJF(CameraJFRequest2(arrayListOf(devId), Calendar.getInstance().timeInMillis))
        }

        // Dieu huong sau khi click vao chia se camera jf
        viewModel.listCameraLiveData.observe(viewLifecycleOwner) {
            when (it.status) {

                Status.LOADING -> showHideLoading(true)

                Status.ERROR -> showHideLoading(false)
                Status.SUCCESS -> {

                    showHideLoading(false)
                    it.data?.let { deviceResponse ->
                        deviceResponse.devices.filter { it.isCamera() }?.let { listDevice ->
                            val list =
                                listDevice.filter { it.createdBy == rxPreferences.getUserId() }
                            val isShareCamera = list.any { it.id == viewModel.deviceId }
                            if (isShareCamera) {
                                arguments?.let {
                                    if (it.containsKey(Define.BUNDLE_KEY.PARAM_LIST_FEATURE)) {
                                        showCustomToast(
                                            title = getString(com.vht.sdkcore.R.string.dont_have_permission_share),
                                            onFinish = {

                                            })
                                    } else {
                                        val bundle = Bundle()
                                        bundle.putString(
                                            Define.BUNDLE_KEY.PARAM_ID,
                                            viewModel.deviceId
                                        )
                                        bundle.putString(
                                            Define.BUNDLE_KEY.PARAM_DEVICE_SERIAL,
                                            viewModel.devId
                                        )

                                        bundle.putString(Define.BUNDLE_KEY.PARAM_NAME, devName)
                                        bundle.putString(
                                            Define.BUNDLE_KEY.PARAM_MODEL_CAMERA,
                                            modelCamera
                                        )

                                        if (it.containsKey(Define.BUNDLE_KEY.PARAM_STATUS_SHARE)) {

                                            bundle.putString(
                                                Define.BUNDLE_KEY.PARAM_STATUS_SHARE,
                                                it.getString(Define.BUNDLE_KEY.PARAM_STATUS_SHARE)
                                            )

                                            bundle.putString(
                                                Define.BUNDLE_KEY.PARAM_PERMISSIONS,
                                                permissionJf
                                            )
                                        }

                                        bundle.putString(
                                            Define.BUNDLE_KEY.PARAM_TYPE_SCREEN_OPEN_CREATE_SHARE_CAMERA_JF,
                                            Define.BUNDLE_KEY.PARAM_TYPE_SHARE_CAMERA_JF
                                        )
                                        //todo open create camera share
                                     //   appNavigation.openCreateShareCameraJF(bundle)

                                    }
                                }
                            } else {
                                showCustomToast(
                                    title = getString(com.vht.sdkcore.R.string.dont_have_permission_share),
                                    onFinish = {

                                    })
                            }
                        }

                    }
                }
            }
        }
        viewModel.responseSystemFunction.observe(viewLifecycleOwner) {
            initTabFeature()
        }
        retrievePasswordViewModel.requestOTPStatus.observe(viewLifecycleOwner) {
            when (it.status) {
                Status.LOADING -> {
                    showHideLoading(true)
                }

                Status.SUCCESS -> {
                    showHideLoading(false)
                    appNavigation.openVerificationCodeCameraJF(
                        bundleOf(Define.BUNDLE_KEY.PARAM_MODEL_CAMERA to retrievePasswordViewModel.devId)
                    )
                }

                Status.ERROR -> {
                    showHideLoading(false)
                    when (it.exception) {
                        RetrievePasswordCameraJFViewModel.ERROR_NOT_SET_CONTACT -> {
                            CommonAlertDialogNotification
                                .getInstanceCommonAlertdialog(requireContext())
                                .showDialog()
                                .showCenterImage(DialogType.ERROR)
                                .setDialogTitleWithString("Không thể sử dụng chức năng do Camera chưa từng xem trực tiếp với mật khẩu đã thiết lập.")
                                .setContent("Nếu bạn quên mật khẩu vui lòng xóa thiết bị trên ứng dụng và thực hiện thêm lại thiết bị này.")
                                .setTextPositiveButtonWithString("OK")
                                .setOnPositivePressed { dialog ->
                                    dialog.dismiss()
                                }
                                .hideOptionButton()
                                .showPositiveButton()
                        }

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
        updateFirmwareViewModel.isCanUpdateFirmware.observe(viewLifecycleOwner) {
            if (it) {
                binding.toolbar.setSrcRight(R.drawable.ic_setting_cam_live_has_notification)
                binding.hasNewFirmWareNotification.visible()
                updateFirmwareViewModel.checkShowRecommendUpdateDialog()
            } else {
                binding.toolbar.setSrcRight(R.drawable.ic_setting_cam_live)
                binding.hasNewFirmWareNotification.gone()
            }
        }
        updateFirmwareViewModel.isShowDialogRecommendUpdateFirmware.observe(viewLifecycleOwner) {
            if (it) {
                showRecommendUpdateFirmwareDialog()
            }
        }
    }

    private fun showRecommendUpdateFirmwareDialog() {
        if (CommonAlertDialogNotification.instance?.isShowing == true) {
            return
        }
        CommonAlertDialogNotification
            .getInstanceCommonAlertdialog(requireContext())
            .showDialog()
            .showCenterImage(DialogType.NOTIFICATION)
            .setDialogTitleWithString(getString(com.vht.sdkcore.R.string.upgrade_firmware_recommend))
            .setTextPositiveButtonWithString(getString(com.vht.sdkcore.R.string.update))
            .setOnPositivePressed { dialog ->
                dialog.dismiss()
                appNavigation.openUpdateFirmwareJFCamera(
                    bundleOf(
                        Define.BUNDLE_KEY.PARAM_DEVICE_SERIAL to viewModel.devId,
                        Define.BUNDLE_KEY.PARAM_IS_UPDATE_FIRMWARE_IMMEDIATELY to true
                    )
                )
//                )
            }
            .setTextNegativeButtonWithString(getString(com.vht.sdkcore.R.string.later))
            .setOnNegativePressed { dialog ->
                dialog.dismiss()
                updateFirmwareViewModel.saveThisTimeShowDialogRecommendUpdate()
            }
            .showNegativeAndPositiveButton()
    }

    private fun setUIPTZCamera() {
        binding.panLayout.root.visible()
        if (isShareCamera && !permissionJf.contains(Config.FEATURE_PTZ_JF)
            && currentTab == R.id.tabPTZ
        ) {
            binding.panLayout.root.disable()
            binding.tvPTZ.visibility = View.GONE
            binding.ptzNotPermission.gone()

        } else {
            binding.panLayout.root.enable()
            binding.tvPTZ.visibility = View.GONE
            binding.ptzNotPermission.gone()
        }
    }

    private fun initVideoSize() {
//        DebugConfig.log(TAG, "initVideoSize")
//        val width = getMinimalScreenWidth()
//        val height = getMinimalScreenHeight()
//        val textureView = binding.cameraView
//        val params = textureView.layoutParams.also {
//            it.width = width
//            it.height = height
//        }
//        textureView.layoutParams = params
//        binding.cameraLayout.layoutParams.let {
//            it.width = width
//            it.height = height
//        }
    }

    private fun initListVideo() {
        binding.swipeLayout.setOnRefreshListener {
            binding.swipeLayout.isRefreshing = false
            searchEventJF(CameraJFRequest2(arrayListOf(devId), Calendar.getInstance().timeInMillis))
        }
        initAdapter()
        searchEventJF(CameraJFRequest2(arrayListOf(devId), Calendar.getInstance().timeInMillis))
    }

    private fun searchEventJF(request: CameraJFRequest2) {
        if (request.devId.isNullOrEmpty()) {
            return
        }
        Log.d(TAG, "searchEventJF: ${request.devId[0]}")
        lifecycleScope.launchWhenStarted {
            val startSearchDay = Calendar.getInstance()
            startSearchDay.time = Date(request.date)
            startSearchDay[Calendar.HOUR_OF_DAY] = 0
            startSearchDay[Calendar.MINUTE] = 0
            startSearchDay[Calendar.SECOND] = 0
            val stopSearchDay = Calendar.getInstance()
            stopSearchDay.time = Date(request.date)
            stopSearchDay[Calendar.HOUR_OF_DAY] = 23
            stopSearchDay[Calendar.MINUTE] = 59
            stopSearchDay[Calendar.SECOND] = 59
            val list = JFCameraManager.getAllAlarmInfoByTime(
                request.devId[0],
                startSearchDay,
                stopSearchDay
            ).filter {
                it.event == AppEnum.EventJFType.HI.type || it.event == AppEnum.EventJFType.MOTION_DETECTION.type
            }

            if (isShareCamera && !permissionJf.contains(Config.FEATURE_EVENT_JF)
                && binding.bottomNavigation.selectedItemId == R.id.tabEvent
            ) {
                binding.ptzNotPermission.visible()
                binding.tvSeeMore.gone()
                binding.rcvEvent.gone()
            } else {
                binding.ptzNotPermission.gone()
                binding.tvSeeMore.visible()
                binding.rcvEvent.visible()
                lifecycleScope.launchWhenStarted {
                    if (list.isEmpty()) {
                        binding.rcvEvent.gone()
                    } else {
                        binding.rcvEvent.visible()
                    }
                }
                adapterJF.submitList(list.sortedByDescending { it.startTime })
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun setOnClick() {
        super.setOnClick()

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            currentTab = item.itemId
            when (item.itemId) {
                R.id.tabEvent -> {
                    binding.containerEvent.visible()
                    binding.panLayout.root.gone()
                    binding.tabFeature.root.gone()
                    binding.tabService.root.gone()

                    if ((isShareCamera && !permissionJf.contains(Config.FEATURE_EVENT_JF))) {
                        binding.ptzNotPermission.visible()
                    } else {
                        binding.ptzNotPermission.gone()
                    }

                }

                R.id.tabPTZ -> {
                    binding.containerEvent.gone()
                    binding.tabFeature.root.gone()
                    binding.tabService.root.gone()
                    setUIPTZCamera()
                }

                R.id.tabFeature -> {
                    binding.ptzNotPermission.gone()
                    binding.containerEvent.gone()
                    binding.panLayout.root.gone()
                    binding.tabFeature.root.visible()
                    binding.tabService.root.gone()
                }

                R.id.tabService -> {
                    binding.ptzNotPermission.gone()
                    binding.containerEvent.gone()
                    binding.panLayout.root.gone()
                    binding.tabFeature.root.gone()
                    binding.tabService.root.visible()
                }

                else -> {}
            }
            true
        }
        binding.bottomNavigation.selectedItemId = currentTab

        binding.tvSeeMore.setOnClickListener {
            if (checkStopRecord()) return@setOnClickListener
            val bundle = bundleOf().apply {
                putString(Define.BUNDLE_KEY.PARAM_DEVICE_ID, devId)
                putString(Define.BUNDLE_KEY.PARAM_ID, viewModel.deviceId)
                putInt(Define.BUNDLE_KEY.PARAM_TYPE, CLOUD_TYPE)
                putBoolean(Define.BUNDLE_KEY.PARAM_IS_SHARE_CAMERA_JF, isShareCamera)
                putString(Define.BUNDLE_KEY.PARAM_NAME, devName)
            }
            //todo open liveview cloud
           // appNavigation.openListVideoCloudFragment(bundle)
        }

        if (isShareCamera) {
            binding.tabService.btnBuy.gone()
            binding.toolbar.setDrawableTitle(R.drawable.ic_share_cam)
            binding.toolbar.setStringTitle(devId)
            binding.layoutControl.btnDefinition.disable()
        } else {
            binding.tabService.btnBuy.visible()
            binding.toolbar.setStringTitle(devName)
            binding.layoutControl.btnDefinition.enable()
        }

        binding.tabService.btnBuy.setOnClickListener {
            if (isShareCamera) return@setOnClickListener
            val bundle = Bundle().apply {
                putString(Define.BUNDLE_KEY.PARAM_ID, viewModel.deviceId)
                putString(Define.BUNDLE_KEY.PARAM_NAME, devName ?: devId)
                putString(Define.BUNDLE_KEY.PARAM_DEVICE_SERIAL, devId)
                putString(Define.BUNDLE_KEY.PARAM_DEVICE_TYPE, Define.TYPE_DEVICE.CAMERA_JF)
            }
            appNavigation.openCloudStorageCamera(bundle)
        }

        handlePanControl()
        binding.layoutControl.btnVolumeControl.setOnClickListener {
            viewModel.isDisableVolume.value = !viewModel.isDisableVolume.value!!
            if (viewModel.isDisableVolume.value == false) {
                viewModel.openVoice(stopAudio)
                binding.layoutControl.btnVolumeControl.setImageResource(R.drawable.ic_volume_on)
            } else {
                viewModel.closeVoice(stopAudio)
                binding.layoutControl.btnVolumeControl.setImageResource(R.drawable.ic_volume_off)
            }
        }
        viewModel.isDisableVolume.observe(viewLifecycleOwner) {
            if (!it) {
                binding.layoutControl.btnVolumeControl.setImageResource(R.drawable.ic_volume_on)
            } else {
                binding.layoutControl.btnVolumeControl.setImageResource(R.drawable.ic_volume_off)
            }
        }
        binding.btOffPrivateMode.setOnClickListener {
            showHideLoading(true)
            JFCameraManager.setStatusMask(devId, false) { isSuccess ->
                showHideLoading(false)
                if (isSuccess) {
                    requestStream()
                    binding.ivViewPrivateMode.gone()
                    binding.groupPrivateMode.gone()
                    setShowHideControlPrivateMode(false)

                    tabFeatureAdapter.updateStateItem(TAB_ID.FEATURE_PRIVATE, false)
                }
            }
        }
        binding.toolbar.setOnLeftClickListener {
            if (checkStopRecord()) return@setOnLeftClickListener
            requireActivity().finish()
        }

        binding.layoutControl.btnTalk.setOnClickListener {
            if (checkAudioPermission(true)) {
                if (!stopAudio) {
                    startTalk()
                } else {
                    destroyTalk()
                }
            }
        }
        var dX = 0F
        var dY = 0F
        binding.panLayout.btnAuto.setOnTouchListener(View.OnTouchListener { view, event ->

            if (panX == 0F) {
                panX = binding.panLayout.btnAuto.x
                panY = binding.panLayout.btnAuto.y
                containerX = binding.panLayout.panBackground.x
                containerY = binding.panLayout.panBackground.y
                sizeContainer = binding.panLayout.panBackground.width
                sizePan = binding.panLayout.btnAuto.width
            }
            val newX: Float
            val newY: Float
            when (event?.action) {
                MotionEvent.ACTION_DOWN -> {
                    dX = view.x - event.rawX
                    dY = view.y - event.rawY
                }

                MotionEvent.ACTION_MOVE -> {

                    newX = event.rawX + dX
                    newY = event.rawY + dY
                    DebugConfig.log(
                        "Trong",
                        "event.rawX: ${event.rawX} -- event.rawY: ${event.rawY}"
                    )
                    DebugConfig.log("Trong", "newX: $newX -- newY: $newY")
                    if (containerX > newX || (containerX + sizeContainer - sizePan) < newX || newY < containerY || newY > containerY + sizeContainer - sizePan) {
                        DebugConfig.log("Trong", "Error:")
                    } else {
                        view.animate()
                            .x(newX)
                            .y(newY)
                            .setDuration(0)
                            .start()
                        sumActionPTZ += 1
                        handlePTZ(newX, newY, sumActionPTZ)
                        if (sumActionPTZ == DEFAULT_SUM_ACTION_PTZ) {
                            sumActionPTZ = 0
                        }
                    }


                }

                MotionEvent.ACTION_UP -> {
                    view.animate()
                        .x(panX)
                        .y(panY)
                        .setDuration(200)
                        .start()
                    Handler().postDelayed({
                        currentActionPTZ = ActionControlPTZ.STOP
                        controlPTZRequest()
                        ObjectAnimator.ofFloat(view, "translationY", 0f).apply {
                            duration = 200
                            start()
                        }
                        sumActionPTZ = 0
                    }, 200)
                }

                else -> { // Note the block
                    return@OnTouchListener false
                }
            }
            true
        })
        binding.layoutRightSide.panLayout.btnAuto.setOnTouchListener(View.OnTouchListener { view, event ->
            if (panRightX == 0F) {
                panRightX = binding.layoutRightSide.panLayout.btnAuto.x
                panRightY = binding.layoutRightSide.panLayout.btnAuto.y
            }
            val newX: Float
            val newY: Float
            when (event?.action) {
                MotionEvent.ACTION_DOWN -> {
                    dX = view.x - event.rawX
                    dY = view.y - event.rawY
                }

                MotionEvent.ACTION_MOVE -> {

                    newX = event.rawX + dX
                    newY = event.rawY + dY
                    DebugConfig.log(
                        "Trong",
                        "event.rawX: ${event.rawX} -- event.rawY: ${event.rawY}"
                    )
                    DebugConfig.log("Trong", "newX: $newX -- newY: $newY")
                    if ((panRightX - panRightY) > newX || (panRightX + panRightY) < newX || newY < 10 || newY > (panRightY * 2 - 20)) {
                        DebugConfig.log("Trong", "Error:")
                    } else {
                        view.animate()
                            .x(newX)
                            .y(newY)
                            .setDuration(0)
                            .start()
                        sumActionRightPTZ += 1
                        handleRightPTZ(newX, newY, sumActionRightPTZ)
                        if (sumActionRightPTZ == DEFAULT_SUM_ACTION_PTZ) {
                            sumActionRightPTZ = 0
                        }
                    }


                }

                MotionEvent.ACTION_UP -> {
                    view.animate()
                        .x(panRightX)
                        .y(panRightY)
                        .setDuration(200)
                        .start()
                    currentActionPTZ = ActionControlPTZ.STOP
                    controlPTZRequest()
                    ObjectAnimator.ofFloat(view, "translationY", 0f).apply {
                        duration = 200
                        start()
                    }
                    sumActionRightPTZ = 0
                }

                else -> { // Note the block
                    return@OnTouchListener false
                }
            }
            true
        })
        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (isScreenOrientationLandscape()) {
                        toggleFullScreenMode()
                    } else {
                        if (checkStopRecord()) return
                        appNavigation.navigateUp()
                    }
                }
            }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)

        binding.layoutToolbarFullscreen.btnLeft.setOnClickListener {
            toggleFullScreenMode()
        }

        binding.layoutControl.btnZoomControl.setOnClickListener {
            toggleFullScreenMode()
        }

        binding.layoutBtnControl.btnAudioPTT.setOnClickListener {
            if (isDoubleClick) return@setOnClickListener
            if (checkAudioPermission(true)
                && !(isShareCamera && !permissionJf.contains(Config.FEATURE_AUDIO_JF))
            ) {
                if (!stopAudio) {
                    startTalk()
                } else {
                    destroyTalk()
                }
            }
        }
        binding.layoutControl.btnPan.setOnClickListener {
            setRightSideControlVisible(true)
            binding.layoutRightSide.llControlRightSide.visibility = View.GONE
            binding.layoutControl.root.visibility = View.GONE
        }

        binding.layoutBtnControl.btnSnapShot.setOnClickListener {
            if (isDoubleClick) return@setOnClickListener
            if (checkReadStoragePermission(true)) {
                snapShotLiveView()
            }
        }
        binding.layoutRightSide.btnCaptureImage.setOnClickListener {
            if (checkReadStoragePermission(true)) {
                snapShotLiveView()
            }
        }

        binding.layoutRightSide.btnCaptureImagePan.setOnClickListener {
            if (checkReadStoragePermission(true)) {
                snapShotLiveView()
            }
        }


        binding.layoutBtnControl.btnRecord.setOnClickListener {
            if (isDoubleClick) return@setOnClickListener
            if (stopRecordMedia) {
                stopRecording(true)
            } else {
                recordingStart()
            }
        }
        binding.layoutRightSide.btnRecordVideo.setOnClickListener {
            if (stopRecordMedia) {
                stopRecording(true)
            } else {
                recordingStart()
            }
        }
        binding.layoutRightSide.btnRecordVideoPan.setOnClickListener {
            if (stopRecordMedia) {
                stopRecording(true)
            } else {
                recordingStart()
            }
        }

        binding.layoutControl.btnDefinition.setOnClickListener {

            activity?.resources?.configuration?.orientation?.let { orientation ->
                if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    openQualityPopupWindow(it)
                } else {
                    BottomSheetQualityFragment.newInstance(
                        true,
                        false,
                        childFragmentManager,
                        currentQuality,
                        true
                    ) { level ->
                        when (level) {
                            0 -> {
                                setDefinition(SDKCONST.StreamType.Extra)
                                checkVoice()
                            }

                            1 -> {
                                setDefinition(SDKCONST.StreamType.Main)
                                checkVoice()
                            }
                        }
                    }
                }
            }

        }

        binding.layoutBtnControl.btnHistory.setOnClickListener {
            if (isDoubleClick || (isShareCamera
                        && !permissionJf.contains(Config.FEATURE_PLAYBACK_JF)
                        && !permissionJf.contains(Config.FEATURE_EVENT_JF))
            ) {
                return@setOnClickListener
            }
            if (checkStopRecord()) return@setOnClickListener
            lifecycleScope.launch {
                val isRegisted = withTimeout(2000L) {
                    viewModel.checkCloudStorageRegistered(devId) ?: false
                }
                val bundle = Bundle()
                bundle.putString(Define.BUNDLE_KEY.PARAM_DEVICE_SERIAL, devId)
                bundle.putString(Define.BUNDLE_KEY.PARAM_ID, viewModel.deviceId)
                bundle.putBoolean(Define.BUNDLE_KEY.PARAM_VALUE, isRegisted)
                bundle.putString(Define.BUNDLE_KEY.PARAM_NAME, devName)
                bundle.putBoolean(Define.BUNDLE_KEY.PARAM_IS_SHARE_CAMERA_JF, isShareCamera)
                appNavigation.openQuickPlaybackJFFragment(bundle)
            }
        }

        binding.quickAccessLayout.btnExpand.setOnClickListener() {
            binding.quickAccessLayout.btngQuickAccess.visibility =
                if (isExpandQuickAccess) View.GONE else View.VISIBLE
            binding.quickAccessLayout.btnExpand.animate()
                .rotationX(if (isExpandQuickAccess) 0.0F else 180.0F).setDuration(500).start()
            isExpandQuickAccess = !isExpandQuickAccess
        }

        binding.ivRetryStreaming.setOnClickListener {
            retryStreaming()
        }

        binding.layoutVideoEncrypted.root.setOnClickListener {
            showDialogEncrypted()
        }

        binding.view.setOnTouchListener { v, event ->
            when (event?.action) {
                MotionEvent.ACTION_DOWN -> {
                    val current = System.currentTimeMillis()
                    if (current - timeTouchScale < 100) {
                        isRunningSingleTouch = false
                    } else {
                        lifecycleScope.launchWhenStarted {
                            Handler().postDelayed(Runnable {
                                // Check xem nếu không phải là phóng to/thu nhỏ thì cho ẩn hiện các button
                                if (isRunningSingleTouch) {
                                    setRightSideControlVisible(false)
                                    setUIControlFullScreen()
                                    closeQualityPopupWindow()
                                }
                            }, 100)
                        }
                        isRunningSingleTouch = true

                    }
                    timeTouchScale = current
                }
            }
            false
        }

    }

    var timeTouchScale = 0L
    var isRunningSingleTouch = true

    private fun checkVoice() {
        if (viewModel.isDisableVolume.value == false) {
            viewModel.openVoice(stopAudio)
        } else {
            viewModel.closeVoice(stopAudio)
        }
    }

    private fun handleRightPTZ(x: Float, y: Float, sumAction: Int) {
        DebugConfig.log("Test", "handlePTZ")
        DebugConfig.log(
            "Trong handlePTZ",
            "x:$x-- y: $y"
        )
        if (sumActionRightPTZ < DEFAULT_SUM_ACTION_PTZ) {
            return
        }
        if (x > (panRightX + 10) && y > (panRightY / 2) && y < (panRightY + panRightY / 3)) {
            DebugConfig.log(
                "Trong handlePTZ",
                "RIGHT"
            )

            currentActionPTZ = ActionControlPTZ.RIGHT
            controlPTZRequest()
        } else if (x < (panRightX - 10) && y > (panRightY / 2) && y < (panRightY + panRightY / 3)) {
            DebugConfig.log(
                "Trong handlePTZ",
                "LEFT"
            )
            currentActionPTZ = ActionControlPTZ.LEFT
            controlPTZRequest()

        } else if ((y > 0) && (y <= panRightY / 2) && (x >= (panRightX - (panRightY / 2))) && (x <= (panRightX + (panRightY / 2)))) {
            DebugConfig.log(
                "Trong handlePTZ",
                "TOP"
            )
            currentActionPTZ = ActionControlPTZ.UP
            controlPTZRequest()
        } else if ((y >= panRightY) && (x >= (panRightX - (panRightY / 2))) && (x <= (panRightX + (panRightY / 2)))) {
            DebugConfig.log(
                "Trong handlePTZ",
                "BOTTM"
            )
            currentActionPTZ = ActionControlPTZ.DOWN
            controlPTZRequest()

        }
    }

    private fun setRightSideControlVisible(visble: Boolean) {
        lifecycleScope.launchWhenStarted {
            if (visble) {
                val offsetX =
                    -resources.getDimension(R.dimen.right_side_control_width) - 70
                binding.layoutRightSide.panExpand.animate().translationX(offsetX).setDuration(200)
                    .start()
            } else {
                binding.layoutRightSide.panExpand.animate().translationX(0.0F).setDuration(200)
                    .start()
            }
        }
    }


    private fun setUIControlFullScreen() {
        lifecycleScope.launchWhenStarted {
            if (binding.layoutControl.root.isVisible) {
                binding.layoutRightSide.llControlRightSide.visibility = View.GONE
                binding.layoutControl.root.visibility = View.GONE
                binding.tvBitrate.gone()
                binding.tvCameraName.gone()
            } else {
                binding.layoutRightSide.llControlRightSide.visibility = View.VISIBLE
                binding.layoutControl.root.visibility = View.VISIBLE
                activity?.resources?.configuration?.orientation?.let { orientation ->
                    if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                        binding.tvCameraName.visible()
                        binding.tvBitrate.gone()
                    } else {
                        binding.tvCameraName.gone()
                        binding.tvBitrate.visible()
                    }
                }
            }

        }
    }

    private fun flipImageCamera() {
        lifecycleScope.launchWhenResumed {
            try {
                showHideLoading(true)
                DebugConfig.log(TAG, "flipImageCamera start: $devId")
                var response = 0
                withTimeout(15_000L) {
                    viewModel.flipCamera {
                        response = it
                    }
                }
                DebugConfig.log(TAG, "flipImageCamera response: $response")
                if (response >= 0) {
                    showCustomToast(com.vht.sdkcore.R.string.String_flip_image_success)
                }
                showHideLoading(false)
            } catch (e: Exception) {
                DebugConfig.loge(TAG, "flipImageCamera e ${e.message}")
                showHideLoading(false)
            }
        }
    }

    private fun settingAlarmControlCamera() {
        if (context?.isNetworkAvailable() == true) {
            lifecycleScope.launchWhenResumed {
                if (!binding.quickAccessLayout.ivDefence.isSelected) {
                    try {
                        binding.quickAccessLayout.ivDefence.isSelected = true
                        binding.quickAccessLayout.tvDefendence.setTextColor(Color.parseColor("#EC0D3A"))
                        binding.clShowDefinition.visible()
                        timer = object : CountDownTimer(61_000L, 1000L) {
                            override fun onTick(millisUntilFinished: Long) {
                                try {
                                    binding?.let {
                                        it.tvContentDefinition.text =
                                            (millisUntilFinished / 1000).toString() + "s"
                                    }

                                } catch (e: Exception) {
                                    DebugConfig.log(TAG, e.message ?: "")
                                }

                            }

                            override fun onFinish() {
                                try {
                                    binding?.let {
                                        it.quickAccessLayout.ivDefence.isSelected = false
                                        it.quickAccessLayout.tvDefendence.setTextColor(
                                            Color.parseColor(
                                                "#323336"
                                            )
                                        )
                                        it.clShowDefinition.gone()
                                    }

                                } catch (e: Exception) {
                                    DebugConfig.log(TAG, e.message ?: "")
                                }
                            }
                        }
                        timer?.start()
                        DebugConfig.log(
                            TAG,
                            "settingAlarmControlCamera ON start: $devId"
                        )
                        val response = withTimeout(30_000L) {
                            //Todo: bo sung code
//                            viewModel.settingAlarmControlCamera(serialCamera, 1, 1)
                        }
                        DebugConfig.log(TAG, "settingAlarmControlCamera ON response: $response")

                    } catch (e: Exception) {
                        DebugConfig.loge(TAG, "settingAlarmControlCamera ON e ${e.message}")
                        binding.quickAccessLayout.ivDefence.isSelected = false
                        binding.quickAccessLayout.tvDefendence.setTextColor(Color.parseColor("#323336"))
                        binding.clShowDefinition.gone()
                        requireContext().toast(getString(com.vht.sdkcore.R.string.no_connection))
                    }
                } else {
                    try {
                        binding.quickAccessLayout.ivDefence.isSelected = false
                        binding.quickAccessLayout.tvDefendence.setTextColor(Color.parseColor("#323336"))
                        binding.clShowDefinition.gone()
                        timer?.cancel()
                        DebugConfig.log(
                            TAG,
                            "settingAlarmControlCamera OFF start: $devId"
                        )
                        val response = withTimeout(30_000L) {
                            //Todo: bo sung code
//                            viewModel.settingAlarmControlCamera(serialCamera, 0, 0)
                        }
                        DebugConfig.log(
                            TAG,
                            "settingAlarmControlCamera OFF response: $response"
                        )

                    } catch (e: Exception) {
                        DebugConfig.loge(TAG, "settingAlarmControlCamera OFF e ${e.message}")
                        binding.quickAccessLayout.ivDefence.isSelected = false
                        binding.quickAccessLayout.tvDefendence.setTextColor(Color.parseColor("#323336"))
                        binding.clShowDefinition.gone()
                    }

                }

            }
        } else {
            CommonAlertDialog.getInstanceCommonAlertdialog(requireContext())
                .showDialog()
                .setDialogTitleWithString(getString(com.vht.sdkcore.R.string.no_connection))
                .setTextPositiveButton(com.vht.sdkcore.R.string.text_close)
                .showPositiveButton()
                .setOnPositivePressed {
                    it.dismiss()
                }
        }

    }

    private fun handlePTZ(x: Float, y: Float, sumAction: Int) {
        DebugConfig.log("Test", "handlePTZ")
        DebugConfig.log(
            "Trong handlePTZ",
            "x:$x-- y: $y"
        )
        if (sumActionPTZ < DEFAULT_SUM_ACTION_PTZ) {
            return
        }
        if (x > (panX + sizePan / 3) && y >= (panY - sizePan / 3) && y <= (panY + sizePan / 3)) {
            DebugConfig.log(
                "Trong handlePTZ",
                "RIGHT"
            )

            currentActionPTZ = ActionControlPTZ.RIGHT
            controlPTZRequest()
        } else if (x < (panX - sizePan / 3) && y >= (panY - sizePan / 3) && y <= (panY + sizePan / 3)) {
            DebugConfig.log(
                "Trong handlePTZ",
                "LEFT"
            )
            currentActionPTZ = ActionControlPTZ.LEFT
            controlPTZRequest()

        } else if ((y < panY - sizePan / 3) && (x >= (panX - sizePan / 3)) && (x <= (panX + sizePan / 3))) {
            DebugConfig.log(
                "Trong handlePTZ",
                "TOP"
            )
            currentActionPTZ = ActionControlPTZ.UP
            controlPTZRequest()
        } else if ((y > panY + sizePan / 3) && (x >= (panX - sizePan / 3)) && (x <= (panX + sizePan / 3))) {
            DebugConfig.log(
                "Trong handlePTZ",
                "BOTTM"
            )
            currentActionPTZ = ActionControlPTZ.DOWN
            controlPTZRequest()

        }
    }

    private fun handlePanControl() {

        with(binding) {
            panLayout.btnPanLeft.setOnTouchListener(mOnPanLeftTouchListener)
            layoutRightSide.panLayout.btnPanLeft.setOnTouchListener(mOnPanLeftTouchListener)

            panLayout.btnPanRight.setOnTouchListener(mOnPanRightTouchListener)
            layoutRightSide.panLayout.btnPanRight.setOnTouchListener(mOnPanRightTouchListener)

            panLayout.btnPanUp.setOnTouchListener(mOnPanUpTouchListener)
            layoutRightSide.panLayout.btnPanUp.setOnTouchListener(mOnPanUpTouchListener)

            panLayout.btnPanDown.setOnTouchListener(mOnPanDownTouchListener)
            layoutRightSide.panLayout.btnPanDown.setOnTouchListener(mOnPanDownTouchListener)


        }
    }

    @SuppressLint("ClickableViewAccessibility")
    val mOnPanLeftTouchListener = View.OnTouchListener { _, motionEvent ->
        DebugConfig.log("Test", "OnTouchListener")

        when (motionEvent.action) {
            MotionEvent.ACTION_DOWN -> {
                currentActionPTZ = ActionControlPTZ.LEFT
                controlPTZRequest()
                ObjectAnimator.ofFloat(binding.panLayout.btnAuto, "translationX", -100f).apply {
                    duration = 200
                    start()
                }
                ObjectAnimator.ofFloat(
                    binding.layoutRightSide.panLayout.btnAuto,
                    "translationX",
                    -100f
                ).apply {
                    duration = 200
                    start()
                }
            }

            MotionEvent.ACTION_UP -> {
                currentActionPTZ = ActionControlPTZ.STOP
                controlPTZRequest()
                ObjectAnimator.ofFloat(binding.panLayout.btnAuto, "translationX", 0f).apply {
                    duration = 200
                    start()
                }
                ObjectAnimator.ofFloat(
                    binding.layoutRightSide.panLayout.btnAuto,
                    "translationX",
                    0f
                ).apply {
                    duration = 200
                    start()
                }

            }

        }
        false
    }

    @SuppressLint("ClickableViewAccessibility")
    val mOnPanRightTouchListener = View.OnTouchListener { _, motionEvent ->
        DebugConfig.log("Test", "OnTouchListener")

        when (motionEvent.action) {
            MotionEvent.ACTION_DOWN -> {

                currentActionPTZ = ActionControlPTZ.RIGHT
                controlPTZRequest()
                ObjectAnimator.ofFloat(binding.panLayout.btnAuto, "translationX", 100f).apply {
                    duration = 200
                    start()
                }
                ObjectAnimator.ofFloat(
                    binding.layoutRightSide.panLayout.btnAuto,
                    "translationX",
                    100f
                ).apply {
                    duration = 200
                    start()
                }
            }

            MotionEvent.ACTION_UP -> {
                currentActionPTZ = ActionControlPTZ.STOP
                controlPTZRequest()
                ObjectAnimator.ofFloat(binding.panLayout.btnAuto, "translationX", 0f).apply {
                    duration = 200
                    start()
                }
                ObjectAnimator.ofFloat(
                    binding.layoutRightSide.panLayout.btnAuto,
                    "translationX",
                    0f
                ).apply {
                    duration = 200
                    start()
                }
            }

        }
        false
    }

    @SuppressLint("ClickableViewAccessibility")
    val mOnPanUpTouchListener = View.OnTouchListener { _, motionEvent ->
        DebugConfig.log("Test", "OnTouchListener")

        when (motionEvent.action) {
            MotionEvent.ACTION_DOWN -> {

                currentActionPTZ = ActionControlPTZ.UP
                controlPTZRequest()
                ObjectAnimator.ofFloat(binding.panLayout.btnAuto, "translationY", -100f).apply {
                    duration = 200
                    start()
                }
                ObjectAnimator.ofFloat(
                    binding.layoutRightSide.panLayout.btnAuto,
                    "translationY",
                    -100f
                ).apply {
                    duration = 200
                    start()
                }

            }

            MotionEvent.ACTION_UP -> {
                currentActionPTZ = ActionControlPTZ.STOP
                controlPTZRequest()
                ObjectAnimator.ofFloat(binding.panLayout.btnAuto, "translationY", 0f).apply {
                    duration = 200
                    start()
                }
                ObjectAnimator.ofFloat(
                    binding.layoutRightSide.panLayout.btnAuto,
                    "translationY",
                    0f
                ).apply {
                    duration = 200
                    start()
                }
            }

        }
        false
    }

    @SuppressLint("ClickableViewAccessibility")
    val mOnPanDownTouchListener = View.OnTouchListener { _, motionEvent ->
        DebugConfig.log("Test", "OnTouchListener")

        when (motionEvent.action) {
            MotionEvent.ACTION_DOWN -> {
                currentActionPTZ = ActionControlPTZ.DOWN
                controlPTZRequest()
                ObjectAnimator.ofFloat(binding.panLayout.btnAuto, "translationY", 100f).apply {
                    duration = 200
                    start()
                }
                ObjectAnimator.ofFloat(
                    binding.layoutRightSide.panLayout.btnAuto,
                    "translationY",
                    100f
                ).apply {
                    duration = 200
                    start()
                }
            }

            MotionEvent.ACTION_UP -> {
                currentActionPTZ = ActionControlPTZ.STOP
                controlPTZRequest()
                ObjectAnimator.ofFloat(binding.panLayout.btnAuto, "translationY", 0f).apply {
                    duration = 200
                    start()
                }
                ObjectAnimator.ofFloat(
                    binding.layoutRightSide.panLayout.btnAuto,
                    "translationY",
                    0f
                ).apply {
                    duration = 200
                    start()
                }
            }

        }
        false
    }

    private fun controlPTZRequest() {
        currentActionPTZ?.let {
            viewModel.devicePTZControl(it)
        }

    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (newConfig.orientation === Configuration.ORIENTATION_LANDSCAPE) {
            DebugConfig.log(TAG, "new landscape")
            displayFullScreenVideo()

        } else if (newConfig.orientation === Configuration.ORIENTATION_PORTRAIT) {
            DebugConfig.log(TAG, "new portrait")
            displayMinimalScreenVideo()

        }
    }

    private fun displayFullScreenVideo() {
        val width = getFullScreenWidth()
        val height = getFullScreenHeight()
        DebugConfig.log(TAG, "metric width $width - height $height")
        val layoutVideoPlayer = binding.cameraLayout
        val textureView = binding.cameraView
        layoutVideoPlayer.layoutParams =
            ConstraintLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ActionBar.LayoutParams.MATCH_PARENT
            )

        textureView.layoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ActionBar.LayoutParams.MATCH_PARENT
        )
        binding.view.layoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ActionBar.LayoutParams.MATCH_PARENT
        )

        DebugConfig.log(
            TAG,
            "texture view width ${textureView.width} height ${textureView.height}"
        )
        DebugConfig.log(
            TAG,
            "frame layout width ${layoutVideoPlayer.width} height ${layoutVideoPlayer.height}"
        )
    }


    private fun displayMinimalScreenVideo() {
        val width = getMinimalScreenWidth()
        val height = getMinimalScreenHeight()
        val layoutVideoPlayer = binding.cameraLayout
        val textureView = binding.cameraView
        DebugConfig.log(
            TAG,
            "minimal width ${getMinimalScreenWidth()} - height ${getMinimalScreenHeight()}"
        )
        layoutVideoPlayer.layoutParams = ConstraintLayout.LayoutParams(width, height)

        textureView.layoutParams = FrameLayout.LayoutParams(width, height)
        binding.view.layoutParams = FrameLayout.LayoutParams(width, height)

        DebugConfig.log(
            TAG,
            "texture view width ${textureView.width} height ${textureView.height}"
        )
        DebugConfig.log(
            TAG,
            "frame layout width ${layoutVideoPlayer.width} height ${layoutVideoPlayer.height}"
        )
    }

    private fun toggleFullScreenMode() {
        //toggle full screen mode
        activity?.resources?.configuration?.orientation?.let { orientation ->
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                exitFullScreenModeJF()
                binding.containerToolbar.visibility = View.VISIBLE
                binding.tvCameraName.visibility = View.GONE
                binding.tvBitrate.visibility = View.VISIBLE
                binding.layoutToolbarFullscreen.root.visibility = View.GONE
                binding.layoutRightSide.root.visibility = View.GONE
                binding.layoutControl.btnPan.visibility = View.GONE
                binding.layoutControl.btnTalk.visibility = View.GONE
                activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                panX = 0F
                binding.bottomNavigation.visible()
                binding.quickAccessLayout.root.gone()
                binding.layoutControl.btnZoomControl.setImageResource(R.drawable.ic_live_view_full_screen)
                binding.layoutControl.btnZoomControl.margin(left = 10F, right = 0F)
            } else {
                initFullscreenModeJF()
                binding.containerToolbar.visibility = View.GONE
                binding.tvCameraName.visibility = View.VISIBLE
                binding.tvBitrate.visibility = View.GONE
                binding.layoutRightSide.root.visibility = View.VISIBLE
                binding.layoutToolbarFullscreen.root.visibility = View.GONE
                binding.layoutControl.btnTalk.visibility = View.VISIBLE
                panX = 0F
                if (isShareCamera) {
                    binding.layoutControl.btnDefinition.disable()
                } else {
                    binding.layoutControl.btnDefinition.enable()
                }
                if ((isShareCamera && !permissionJf.contains(Config.FEATURE_AUDIO_JF))) {
                    binding.layoutControl.btnTalk.disable()
                } else {
                    binding.layoutControl.btnTalk.enable()
                }
                activity?.requestedOrientation =
                    ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                binding.layoutControl.btnZoomControl.setImageResource(R.drawable.ic_live_view_exit_full_screen)
                binding.quickAccessLayout.root.gone()
                binding.bottomNavigation.gone()
                binding.layoutControl.btnZoomControl.margin(left = 10F, right = 50F)
                if (checkCameraIndoor()) {
                    binding.layoutControl.btnPan.visibility = View.VISIBLE
                    if (!(isShareCamera && !permissionJf.contains(Config.FEATURE_PTZ_JF))) {
                        binding.layoutControl.btnPan.enable()
                    } else {
                        binding.layoutControl.btnPan.disable()
                    }
                }
            }
        }
    }

    fun forceStopRecord(needShowPopup: Boolean = true) {
        if (stopRecordMedia) {
            val time = SystemClock.elapsedRealtime() - binding.recordTimer.base
            Log.d(TAG, "forceStopRecord: time = $time")
            stopRecordMedia = false
            binding.recordTimer.visibility = View.GONE
            viewModel.mediaManager.stopRecord()
            setUIRecording()
            val folder = File(requireContext().filesDir, "${Constants.PHOTO_FOLDER}/${devId}")
            if (!folder.isNotEmpty()) return
            val listFile = folder.listFiles().apply {
                sortBy {
                    it.lastModified()
                }
            }
            if (listFile.isNullOrEmpty()) return
            val fileVideo = listFile.last()
            if (time < 5000L) {
                fileVideo.delete()
            } else {
                val sizeFile = fileVideo.length() / 1024
                if (sizeFile > 0) {
                    if (needShowPopup) {
                        showCustomNotificationDialog(
                            getString(com.vht.sdkcore.R.string.recording_success),
                            DialogType.CONFIRM,
                            com.vht.sdkcore.R.string.save_video_success_content,
                            com.vht.sdkcore.R.string.dialog_btn_save,
                            com.vht.sdkcore.R.string.dialog_btn_cancel,
                        ) {
                            viewModel.saveVideoToGallery(fileVideo.path, devId)
                            context?.toast(getString(com.vht.sdkcore.R.string.string_recording_success))
                        }
                    } else {
                        viewModel.saveVideoToGallery(fileVideo.path, devId)
                    }
                } else {
                    fileVideo.delete()
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        Timber.d("onPause")
        needShowErrorLiveview = false
        saveScreenShot()
        forceStopRecord()
        if (isCall || stopAudio) {
            isCall = false
            destroyTalk()
        }
    }

    override fun onResume() {
        super.onResume()
        requestStream()
    }

    override fun onStop() {
        super.onStop()
        viewModel.stopMonitor()
        context?.unregisterReceiver(receiverNetwork)
        JFCameraManager.cameraWatching = ""
        EventBus.getDefault().unregister(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.d("onDestroy")
        isHasNotify = false
        rejectHandleCallPhone()
        viewModel.destroyMonitor()
        DeviceManager.getInstance()?.logoutDev(devId)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun receiveEventBusPushInfo(pushInfo: EventBusPushInfo?) {
        if (pushInfo != null && pushInfo.operationType == EventBusPushInfo.PUSH_OPERATION.CALL) {
            rejectHandleCallPhone()
            handleCallPhone()
        }
    }

    private fun rejectHandleCallPhone() {
        if (dialogCallPhone != null) {
            dialogCallPhone?.dismiss()
            dialogCallPhone = null
            jobCallPhone?.cancel()
            jobCallPhone = null
        }

    }

    private fun handleCallPhone() {
        dialogCallPhone = IncomingCallDialog(accept = {
            if (!stopAudio && checkAudioPermission()) {
                startTalk()
            }
        })
        dialogCallPhone?.show(childFragmentManager, IncomingCallDialog.TAG)
        if (isActiveDefenceModeOn) {
            stopActiveDefenceMode()
        }
        jobCallPhone = lifecycleScope.launch {
            delay(16000)
            if (isActive) {
                dialogCallPhone?.let {
                    it.dismiss()
                }
            }
        }
    }

    private fun showDialogEncrypted() {
        InputEncryptionPasswordCameraJFDialog(
            isShareCamera,
            isEnteredPass,
            forgotPasswordCallback = {
                retrievePasswordViewModel.requestOTP()
            }
        ) {
            if (it != null) {
                isEnteredPass = true
                // OK
                JFCameraManager.setPasswordCamera(devId, it)
                requestStream()
            } else {
                // Cancel
                isEnteredPass = false
            }
        }.show(childFragmentManager, "InputEncryptionPasswordCameraJFDialog")
    }

    fun checkStopRecord(): Boolean {
        if (stopRecordMedia) {
            showCustomNotificationDialog(
                title = "Cảnh báo dừng ghi hình",
                message = com.vht.sdkcore.R.string.content_check_stop_record,
                type = DialogType.CONFIRM,
                titleBtnConfirm = com.vht.sdkcore.R.string.btn_ok,
                negativeTitle = com.vht.sdkcore.R.string.string_cancel
            ) {
                forceStopRecording()
            }
        }
        return stopRecordMedia
    }

    private fun checkStopRecordPrivate(callbackPositive: () -> Unit, callbackNegative: () -> Unit) {
        if (stopRecordMedia) {
            showCustomNotificationDialogPrivate(
                title = "Cảnh báo dừng ghi hình",
                message = com.vht.sdkcore.R.string.content_check_stop_record,
                type = DialogType.CONFIRM,
                onPositiveClick = {
                    forceStopRecording()
                },
                onNegativeClick = {
                    callbackNegative.invoke()
                }
            )
        } else {
            callbackPositive.invoke()
        }

    }

    private fun forceStopRecording() {
        val time = SystemClock.elapsedRealtime() - binding.recordTimer.base
        if (time < 5000L) {
            toastMessage(getString(com.vht.sdkcore.R.string.string_error_recording_live))
            return
        }
        stopRecordMedia = false
        binding.recordTimer.visibility = View.GONE
        viewModel.mediaManager.stopRecord()
        setUIRecording()
        val folder = File(requireContext().filesDir, "${Constants.PHOTO_FOLDER}/${devId}")
        val listFile = folder.listFiles()
        val fileVideo = listFile?.last()
        val pathRename = fileVideo?.path
        pathRename?.let { path ->
            val file = File(path)
            file.let { file ->
                val sizeFile = file.length() / 1024
                if (sizeFile > 0) {
                    val dialog =
                        ConfirmSaveMediaDialog.newInstance(ConfirmSaveMediaDialog.MEDIA_TYPE_VIDEO)
                    dialog.setOnClickListener(object :
                        ConfirmSaveMediaDialog.OnConfirmSaveMediaListener {
                        override fun onSaveToLibrary() {
                            ImageUtils.saveVideoToGalleryJFCamera(
                                requireContext(),
                                path,
                                devId ?: ""
                            )
                            context?.toast(getString(com.vht.sdkcore.R.string.string_recording_success))
                        }

                        override fun onCancel() {
                        }
                    })
                    dialog.show(childFragmentManager, ConfirmSaveMediaDialog.TAG)
                } else {
                    context?.toast(getString(com.vht.sdkcore.R.string.record_fail))
                }
            }
        }
    }



    private fun requestStream(needRetry: Boolean = true) {
        binding.fragmentCameraStreamPgLoading.visible()
        DebugConfig.log(TAG, "devId = $devId")
        JFCameraManager.cameraWatching = devId
        viewModel.loginDev(devId, onSuccess = {
            viewModel.getTourAndPresets {
                // TODO
            }
            hideErrorLiveStream()
            if (isShareCamera && !permissionJf.contains(Config.FEATURE_EVENT_JF)) {
                binding.fragmentCameraStreamPgLoading.gone()
            }
            checkEncrypted(false)
            binding.layoutVideoEncrypted.root.gone()
            binding.fragmentCameraStreamPgLoading.gone()
            viewModel.startMonitor()
            eventLiveView()
            if (viewModel.isDisableVolume.value == false) {
                viewModel.openVoice(stopAudio)
                binding.layoutControl.btnVolumeControl.setImageResource(R.drawable.ic_volume_on)
            } else {
                viewModel.closeVoice(stopAudio)
                binding.layoutControl.btnVolumeControl.setImageResource(R.drawable.ic_volume_off)
            }
            if (isCall && !stopAudio && checkAudioPermission()) {
                startTalk()
            }
            if (!isShareCamera) {
                updateFirmwareViewModel.checkHasNewFirmware()
            }
            Timber.d("loginDev_success")
        }, onFailed = { errorId ->
            checkEncrypted(true)
            binding.fragmentCameraStreamPgLoading.gone()
            DebugConfig.log(TAG, "requestStream error = $errorId")
            if (errorId == EFUN_ERROR.EE_DVR_PASSWORD_NOT_VALID) {
                showDialogEncrypted()
                binding.layoutVideoEncrypted.root.visible()
                DebugConfig.log(TAG, "devId = $devId")
                adapterJF.notifyDataSetChanged()
            } else if (errorId == EFUN_ERROR.EE_DVR_USER_LOCKED) {
                showCustomNotificationDialog(
                    title = "Thiết bị đã bị khóa. Vui lòng khởi động lại thiết bị.",
                    type = DialogType.ERROR
                ) {
                    appNavigation.navigateUp()
                }
            } else if (errorId < 0) {
                if (needRetry) {
                    Log.d(TAG, "requestStream: retry")
                    requestStream(false)
                } else {
                    showErrorLiveStream()
                }
            }
        })
    }

    fun checkEncrypted(isEncrypted: Boolean) {
        if (isEncrypted) {
            tabFeatureAdapter.updateEnableItem(TAB_ID.FEATURE_HI, false)
            tabFeatureAdapter.updateEnableItem(TAB_ID.FEATURE_MOTION_TRACKING, false)
            if (JFCameraManager.isSupportOneKeyMaskVideo(devId)) {
                tabFeatureAdapter.updateEnableItem(TAB_ID.FEATURE_PRIVATE, false)
            }
            if (JFCameraManager.isSupportVoiceLightAlarm(devId)) {
                tabFeatureAdapter.updateEnableItem(TAB_ID.FEATURE_DEFENCE, false)
            }
            enableBtnControl(false)
        } else {
            tabFeatureAdapter.updateEnableItem(TAB_ID.FEATURE_HI, true)
            tabFeatureAdapter.updateEnableItem(TAB_ID.FEATURE_MOTION_TRACKING, true)
            if (JFCameraManager.isSupportOneKeyMaskVideo(devId)) {
                tabFeatureAdapter.updateEnableItem(TAB_ID.FEATURE_PRIVATE, true)
            }
            if (JFCameraManager.isSupportVoiceLightAlarm(devId)) {
                tabFeatureAdapter.updateEnableItem(TAB_ID.FEATURE_DEFENCE, true)
            }
            enableBtnControl(true)
        }
    }

    fun enableBtnControl(isEnable: Boolean) {
        lifecycleScope.launchWhenStarted {
            if (isEnable) {
                if ((isShareCamera && !permissionJf.contains(Config.FEATURE_AUDIO_JF))) {
                    binding.layoutBtnControl.btnAudioPTT.isEnabled = false
                    binding.layoutBtnControl.layoutAudio.alpha = 0.5f
                } else {
                    binding.layoutBtnControl.btnAudioPTT.isEnabled = true
                    binding.layoutBtnControl.layoutAudio.alpha = 1f
                }
                binding.layoutBtnControl.btnSnapShot.isEnabled = true
                binding.layoutBtnControl.btnRecord.isEnabled = true
                binding.layoutBtnControl.layoutSnapshot.alpha = 1f
                binding.layoutBtnControl.layoutRecord.alpha = 1f

                if (isShareCamera && !permissionJf.contains(Config.FEATURE_PTZ_JF)
                ) {
                    binding.panLayout.viewDisable.visible()
                    binding.panLayout.root.alpha = 0.5f
                } else {
                    binding.panLayout.viewDisable.gone()
                    binding.panLayout.root.alpha = 1f
                }

                // Preset
                binding.panLayout.tvSavePreset.enable()
                binding.panLayout.tvPatrol.enable()
                binding.panLayout.tvSaved.enable()
            } else {
                binding.layoutBtnControl.btnAudioPTT.isEnabled = false
                binding.layoutBtnControl.btnSnapShot.isEnabled = false
                binding.layoutBtnControl.btnRecord.isEnabled = false
                binding.layoutBtnControl.layoutAudio.alpha = 0.5f
                binding.layoutBtnControl.layoutSnapshot.alpha = 0.5f
                binding.layoutBtnControl.layoutRecord.alpha = 0.5f

                binding.panLayout.viewDisable.visible()
                binding.panLayout.root.alpha = 0.5f

                // Preset
                binding.panLayout.tvSavePreset.disable()
                binding.panLayout.tvPatrol.disable()
                binding.panLayout.tvSaved.disable()
            }

        }
    }

    private fun retryStreaming() {
        if (NetworkManager.isOnline(requireContext())) {
            binding.ivRetryStreaming.visibility = View.INVISIBLE
            binding.fragmentCameraStreamPgLoading.visible()
//            binding.tvLoading.visible()
//            binding.tvShowStatusConnect.text = getString(R.string.string_play_stream_fail)
            binding.tvShowStatusConnect.gone()
            DebugConfig.log(TAG, "retry edge streaming")
            requestStream()
        } else {
            showCustomNotificationDialog(
                title = getString(com.vht.sdkcore.R.string.no_connection),
                type = DialogType.ERROR
            ) {

            }
            showErrorLiveStream()
        }

    }

    private fun showErrorLiveStream() {
//        retryPrivateMode = 0
        binding.ivRetryStreaming.visible()
        binding.tvShowStatusConnect.visible()
        binding.fragmentCameraStreamPgLoading.gone()
//        binding.tvLoading.gone()
//        binding.tvLoading.text = "0%"
    }

    private fun hideErrorLiveStream() {
//        retryPrivateMode = 0
        binding.ivRetryStreaming.gone()
        binding.tvShowStatusConnect.gone()
        binding.fragmentCameraStreamPgLoading.gone()
//        binding.tvLoading.gone()
//        binding.tvLoading.text = "0%"
    }

    private fun startTalk() {
        if (!NetworkManager.isOnline(requireContext())) {
            toastMessage("Đang mất kết nối, bạn vui lòng kiểm tra lại kết nối và thử lại.")
            return
        }
//        audioManager?.mode = AudioManager.MODE_IN_COMMUNICATION
//        audioManager?.isSpeakerphoneOn = true
        settingUIAudioPTT(true)
        stopAudio = true
        viewModel.startDoubleIntercom()
        binding.layoutControl.btnVolumeControl.isClickable = false
        binding.layoutControl.btnVolumeControl.isEnabled = false
        binding.layoutControl.btnVolumeControl.alpha = 0.5f

        if (isActiveDefenceModeOn) {
            stopActiveDefenceMode()
        }
    }

    private fun destroyTalk() {
        settingUIAudioPTT(false)
        stopAudio = false
//        audioManager?.let {
//            it.mode = AudioManager.MODE_NORMAL
//        }
        viewModel.stopIntercom()
        binding.layoutControl.btnVolumeControl.isClickable = true
        binding.layoutControl.btnVolumeControl.isEnabled = true
        binding.layoutControl.btnVolumeControl.alpha = 1f
    }

    private fun settingUIAudioPTT(startAudio: Boolean = false) {
        if (startAudio) {
            binding.layoutBtnControl.btnAudioPTT.setBackgroundResource(R.drawable.icon_bgr_button_actived)
            binding.layoutBtnControl.btnAudioPTT.setImageResource(R.drawable.ic_ptt_audio_off)

            binding.layoutControl.btnTalk.setBackgroundResource(R.drawable.bg_camera_control_inside_3)
            binding.layoutControl.btnTalk.setImageResource(R.drawable.ic_mic_on_full)

        } else {
            binding.layoutBtnControl.btnAudioPTT.setBackgroundResource(R.drawable.icon_bgr_button_normal)
            binding.layoutBtnControl.btnAudioPTT.setImageResource(R.drawable.ic_ptt_audio)
            binding.layoutControl.btnTalk.setBackgroundResource(R.drawable.bg_control_live_view)
            binding.layoutControl.btnTalk.setImageResource(R.drawable.ic_mig_full_screen)


        }
    }

    private fun snapShotLiveView() {
        if (!NetworkManager.isOnline(requireContext())) {
            toastMessage("Đang mất kết nối. Mời kiểm tra lại")
            return
        }

        try {
            DebugConfig.log(TAG, "saveScreenShot")
            val folder = File(requireContext().filesDir, "${Constants.PHOTO_FOLDER}/${devId}")
            if (!folder.exists()) folder.mkdirs()
            val path = folder.path
            viewModel.mediaManager.capture(path)

            Handler().postDelayed({
                try {
                    val listFile = folder.listFiles()
                    val file = listFile?.last()
                    showCustomNotificationDialog(
                        getString(com.vht.sdkcore.R.string.string_setting_notificaiton_alarm),
                        DialogType.CONFIRM,
                        com.vht.sdkcore.R.string.dialog_confirm_save_photo,
                        com.vht.sdkcore.R.string.dialog_btn_save,
                        com.vht.sdkcore.R.string.dialog_btn_cancel,
                    ) {
                        file?.let {
                            viewModel.saveImageToGallery(it.path, devId)
                        }
                    }
                } catch (e: Exception) {
                    toastMessage("Thao tác không thành công")
                }

            }, 200)

        } catch (e: Exception) {
            toastMessage("Thao tác không thành công")
            DebugConfig.log(TAG, "saveScreenShot e ${e.message}")
        }
    }


    @SuppressLint("CheckResult")
    fun recordingStart() {
        if (checkReadStoragePermission(true)) {
            startRecording()
        }
    }

    private fun startRecording() {
        if (!NetworkManager.isOnline(requireContext())) {
            toastMessage("Đang mất kết nối. Mời kiểm tra lại")
            return
        }

        requireContext().toast(resources.getString(com.vht.sdkcore.R.string.toast_record_video))
        stopRecordMedia = true
        binding.recordTimer.base = SystemClock.elapsedRealtime()
        binding.recordTimer.start()
        binding.recordTimer.visibility = View.VISIBLE
        setUIRecording(true)
        val folder = File(requireContext().filesDir, "${Constants.PHOTO_FOLDER}/${devId}")
        if (!folder.exists()) folder.mkdirs()
        val path = folder.path
        viewModel.mediaManager.startRecord(path)
    }

    private fun stopRecording(clickStop: Boolean = false) {
        if (stopRecordMedia) {
            if (clickStop) {
                val time = SystemClock.elapsedRealtime() - binding.recordTimer.base
                if (time < 5000L) {
                    toastMessage(getString(com.vht.sdkcore.R.string.string_error_recording_live))
                    return
                }
            }
            stopRecordMedia = false
            binding.recordTimer.visibility = View.GONE
            viewModel.mediaManager.stopRecord()
            setUIRecording()
            if (!clickStop) {
                context?.toast(getString(com.vht.sdkcore.R.string.string_recording_loading))
            } else {
                val folder = File(requireContext().filesDir, "${Constants.PHOTO_FOLDER}/${devId}")
                if (!folder.isNotEmpty()) return
                val listFile = folder.listFiles()
                val fileVideo = listFile?.last()
                val pathRename = fileVideo?.path
                pathRename?.let { path ->
                    val file = File(path)
                    file.let { file ->
                        val sizeFile = file.length() / 1024
                        if (sizeFile > 0) {
                            showCustomNotificationDialog(
                                getString(com.vht.sdkcore.R.string.recording_success),
                                DialogType.CONFIRM,
                                com.vht.sdkcore.R.string.save_video_success_content,
                                com.vht.sdkcore.R.string.dialog_btn_save,
                                com.vht.sdkcore.R.string.dialog_btn_cancel,
                            ) {
                                viewModel.saveVideoToGallery(path, devId)
                                context?.toast(getString(com.vht.sdkcore.R.string.string_recording_success))
                            }
//                            val dialog =
//                                ConfirmSaveMediaDialog.newInstance(ConfirmSaveMediaDialog.MEDIA_TYPE_VIDEO)
//                            dialog.setOnClickListener(object :
//                                ConfirmSaveMediaDialog.OnConfirmSaveMediaListener {
//                                override fun onSaveToLibrary() {
//                                    viewModel.saveVideoToGallery(path, devId)
//                                    context?.toast(getString(R.string.string_recording_success))
//                                }
//
//                                override fun onCancel() {
//                                }
//                            })
//                            dialog.show(childFragmentManager, ConfirmSaveMediaDialog.TAG)

                        } else {
                            context?.toast(getString(com.vht.sdkcore.R.string.record_fail))
                        }
                    }
                }
            }
        }
    }

    private fun setUIRecording(startRecording: Boolean = false) {
        if (startRecording) {
            binding.layoutBtnControl.btnRecord.setBackgroundResource(R.drawable.icon_bgr_button_actived)
            binding.layoutBtnControl.btnRecord.setImageResource(R.drawable.ic_stop_record)
            binding.layoutRightSide.btnRecordVideo.setBackgroundResource(R.drawable.bg_camera_control_inside_3)
            binding.layoutRightSide.btnRecordVideo.setImageResource(R.drawable.ic_stop_record)
            binding.layoutRightSide.btnRecordVideoPan.setBackgroundResource(R.drawable.bg_camera_control_inside_3)
            binding.layoutRightSide.btnRecordVideoPan.setImageResource(R.drawable.ic_stop_record)
        } else {
            binding.layoutBtnControl.btnRecord.setBackgroundResource(R.drawable.icon_bgr_button_normal)
            binding.layoutBtnControl.btnRecord.setImageResource(R.drawable.ic_camera_record_selector)
            binding.layoutRightSide.btnRecordVideo.setBackgroundResource(R.drawable.bg_control_live_view)
            binding.layoutRightSide.btnRecordVideo.setImageResource(R.drawable.ic_stop_record)
            binding.layoutRightSide.btnRecordVideoPan.setBackgroundResource(R.drawable.bg_control_live_view)
            binding.layoutRightSide.btnRecordVideoPan.setImageResource(R.drawable.ic_stop_record)
        }
    }

    private fun openQualityPopupWindow(anchor: View) {

        closeQualityPopupWindow()
        val layoutInflater =
            requireActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val layoutView =
            layoutInflater.inflate(R.layout.realplay_quality_items, null, true) as ViewGroup
        val qualitySD = layoutView.findViewById<View>(R.id.containerSD)
        val qualityHD = layoutView.findViewById<View>(R.id.containerHD)
        val qualityFhd = layoutView.findViewById<View>(R.id.containerFHD)

        val tvSD = layoutView.findViewById<View>(R.id.tvSD) as TextView
        val imvHD = layoutView.findViewById<View>(R.id.imvHD) as AppCompatImageView
        val tvHD = layoutView.findViewById<View>(R.id.tvHD) as TextView
        imvHD.setImageResource(R.drawable.ic_quality_fhd)
        tvHD.text = getString(com.vht.sdkcore.R.string.quality_fhd_new)
        qualityFhd.gone()
        when (currentQuality) {
            0 -> tvSD.setCompoundDrawablesWithIntrinsicBounds(
                0,
                0,
                R.drawable.ic_checked,
                0
            )

            1 -> tvHD.setCompoundDrawablesWithIntrinsicBounds(
                0,
                0,
                R.drawable.ic_checked,
                0
            )

            else -> tvHD.setCompoundDrawablesWithIntrinsicBounds(
                0,
                0,
              R.drawable.ic_checked,
                0
            )
        }
        qualitySD.setOnClickListener {
            setDefinition(SDKCONST.StreamType.Extra)
            closeQualityPopupWindow()
            checkVoice()
        }
        qualityHD.setOnClickListener {
            setDefinition(SDKCONST.StreamType.Main)
            closeQualityPopupWindow()
            checkVoice()
        }



        mQualityPopupWindow = PopupWindow(
            layoutView,
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            true
        )
        mQualityPopupWindow?.setBackgroundDrawable(BitmapDrawable())
        mQualityPopupWindow?.setOnDismissListener(PopupWindow.OnDismissListener {
            mQualityPopupWindow = null
            closeQualityPopupWindow()
        })
        try {
            val widthMode = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            val heightMode = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            mQualityPopupWindow?.contentView?.measure(widthMode, heightMode)
            val yOffset: Int =
                -(anchor.height + 24 + mQualityPopupWindow?.contentView?.measuredHeight!!)
            mQualityPopupWindow?.showAsDropDown(anchor, 0, yOffset)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            closeQualityPopupWindow()
        }
    }

    private fun setDefinition(type: Int) {
        if (checkStopRecord()) return
        rxPreferences.setDefinitionJF(DefinitionJF(devId, type))
        when (type) {
            SDKCONST.StreamType.Main -> {
                binding.layoutControl.btnDefinition.text = getString(com.vht.sdkcore.R.string.quality_fhd)
                currentQuality = 1
            }

            SDKCONST.StreamType.Extra -> {
                binding.layoutControl.btnDefinition.text = getString(com.vht.sdkcore.R.string.quality_sd)
                currentQuality = 0
            }

            else -> {
                binding.layoutControl.btnDefinition.text = getString(com.vht.sdkcore.R.string.quality_fhd)
                currentQuality = 1
            }
        }
        needShowErrorLiveview = false
        viewModel.mediaManager.streamType = type
        viewModel.mediaManager.stopPlay()
        viewModel.mediaManager.startMonitor()
    }

    private fun closeQualityPopupWindow() {
        if (mQualityPopupWindow != null) {
            if (mQualityPopupWindow != null && !requireActivity().isFinishing) {
                try {
                    mQualityPopupWindow?.dismiss()
                } catch (e: java.lang.Exception) {
                    // TODO: handle exception
                }
            }
            mQualityPopupWindow = null
        }
    }

    private fun saveScreenShot() {
        if (checkReadStoragePermission()) {
            try {
                DebugConfig.log(TAG, "saveScreenShot: start")
                val folder =
                    requireContext().getExternalFilesDir(null).toString() + "screenshot"
                val dir = File(folder)
                if (!dir.exists()) {
                    dir.mkdirs()
                }
                val path = dir.path
                val nameFileSave = viewModel.mediaManager.capture(path)
                DebugConfig.log(TAG, "saveScreenShot: path: $path")
                GlobalScope.launch(Dispatchers.IO) {
                    try {
                        delay(200)
                        val file = File(nameFileSave)
                        if (file.exists()) {
                            val newFile = File(file.parent, "${devId}.jpeg")
                            if (newFile.exists()) {
                                newFile.delete()
                            }
                            if (file.renameTo(newFile)) {
                                DebugConfig.log(TAG, "saveScreenShot: Đổi tên tệp tin thành công.")
                            } else {
                                DebugConfig.log(TAG, "saveScreenShot:Không thể đổi tên tệp tin.")
                            }
                        } else {
                            DebugConfig.log(TAG, "saveScreenShot: file not file.exists()")
                        }
                    } catch (e: Exception) {
                        DebugConfig.log(TAG, "saveScreenShot e ${e.message}")
                    }

                }

            } catch (e: Exception) {
                DebugConfig.log(TAG, "saveScreenShot e ${e.message}")
            }
        }
    }


    private fun initTabFeature() {
        val listFeature = mutableListOf<TabFeature>()
        val featureHI = TabFeature(
            TAB_ID.FEATURE_HI,
            R.drawable.ic_feature_hi,
            getString(com.vht.sdkcore.R.string.human_intrustion),
            true
        )
        val featureMotion = TabFeature(
            TAB_ID.FEATURE_MOTION_TRACKING, R.drawable.ic_feature_motion,
            getString(com.vht.sdkcore.R.string.motion_tracking),
            true
        )
        val featurePrivate = TabFeature(
            TAB_ID.FEATURE_PRIVATE,
            R.drawable.ic_feature_privacy,
            getString(com.vht.sdkcore.R.string.private_mode),
            true
        )
        val featureDefence = TabFeature(
            TAB_ID.FEATURE_DEFENCE,
            R.drawable.ic_feature_defence,
            getString(com.vht.sdkcore.R.string.defence_feature),
            true,
            false,
            false
        )
        val featureMultiple = TabFeature(
            TAB_ID.FEATURE_MULTIPLE,
            R.drawable.ic_feature_multiple,
            getString(com.vht.sdkcore.R.string.label_camera_multi_screen),
            false
        )
        val featureShare = TabFeature(
            TAB_ID.FEATURE_SHARE,
            R.drawable.ic_feature_share,
            getString(com.vht.sdkcore.R.string.share_device),
            false
        )
        val featureGallery = TabFeature(
            TAB_ID.FEATURE_GALLERY,
            R.drawable.ic_feature_gallery_ffffff,
            getString(com.vht.sdkcore.R.string.label_camera_library),
            false
        )
        if (!isShareCamera && checkCameraIndoor()) {
            listFeature.add(featureMotion)
        }
        if (!isShareCamera) {
            listFeature.add(featureHI)
            listFeature.add(featureShare)
        }
        if (!isShareCamera && JFCameraManager.isSupportOneKeyMaskVideo(devId)) {
            listFeature.add(featurePrivate)
        }
        if (!isShareCamera && JFCameraManager.isSupportVoiceLightAlarm(devId)) {
            listFeature.add(featureDefence)
        }
        listFeature.add(featureMultiple)
        listFeature.add(featureGallery)
        binding.tabFeature.rcv.adapter = tabFeatureAdapter
        tabFeatureAdapter.setDataList(listFeature)

        initStateTabFeature()
    }

    private fun initStateTabFeature() {
        if (JFCameraManager.isSupportOneKeyMaskVideo(devId)) {
            tabFeatureAdapter.updateEnableItem(TAB_ID.FEATURE_PRIVATE, true)
            JFCameraManager.getStatusMask(devId) { isEnable ->
                lifecycleScope.launchWhenStarted {
                    if (isEnable) {
                        enableBtnControl(false)
                        binding.ivViewPrivateMode.visible()
                        binding.groupPrivateMode.visible()
                        setShowHideControlPrivateMode(true)
                        tabFeatureAdapter.updateStateItem(TAB_ID.FEATURE_PRIVATE, true)
                    } else {
                        binding.ivViewPrivateMode.gone()
                        binding.groupPrivateMode.gone()
                        setShowHideControlPrivateMode(false)
                        tabFeatureAdapter.updateStateItem(TAB_ID.FEATURE_PRIVATE, false)
                    }
                }
            }
        } else {
            tabFeatureAdapter.updateEnableItem(TAB_ID.FEATURE_PRIVATE, false)
        }

        JFCameraManager.getHumanDetection(viewModel.devId) { result ->
            lifecycleScope.launchWhenStarted {
                viewModel.currentHumanDetectionBean = result

                tabFeatureAdapter.updateStateItem(TAB_ID.FEATURE_HI, result?.isEnable ?: false)
            }
        }

        JFCameraManager.getDetectTrack(viewModel.devId) { result ->
            lifecycleScope.launchWhenStarted {
                viewModel.currentDetectTrackBean = result

                tabFeatureAdapter.updateStateItem(
                    TAB_ID.FEATURE_MOTION_TRACKING,
                    result?.enable == 1
                )
            }
        }
    }


    fun onClickItemTabFeature(item: TabFeature) {
        when (item.id) {
            TAB_ID.FEATURE_GALLERY -> {
                if (checkStopRecord()) return
                //todo mowr gallery
               // appNavigation.openGalleryFragment()
            }

            TAB_ID.FEATURE_SHARE -> {
                if (checkStopRecord()) return
                viewModel.getListCamera()
            }

            TAB_ID.FEATURE_MULTIPLE -> {
                if (checkStopRecord()) return
//                arguments?.let {
//                    if (typeScreen == 1) {
//                        // implement logic later
//                        val bundle = arguments
//                        bundle?.putBoolean(
//                            Define.BUNDLE_KEY.PARAM_IS_SHARED_HOME,
//                            isShareCamera
//                        )
//                        appNavigation.openMultipleLiveFragment(bundle)
//                    } else {
//                        appNavigation.openPageLiveViewVerticalScreen()
//                    }
//                }
                //todo mo liview xem nhieu man hinh
              //  appNavigation.openPageLiveViewVerticalScreen()
                viewModel.destroyMonitor()
            }

            else -> {}
        }
    }

    fun onActiveChangemTabFeature(tabId: TAB_ID, isActived: Boolean) {
        when (tabId) {
            TAB_ID.FEATURE_PRIVATE -> {
                tabFeatureAdapter.updateStateItem(tabId, isActived)
                if (checkStopRecord()) return
                checkPrivateButtonFeature(tabId, isActived)
            }

            TAB_ID.FEATURE_MOTION_TRACKING -> {
                showHideLoading(true)
                viewModel.setDetectTrack(!isActived) {
                    lifecycleScope.launchWhenStarted {
                        showHideLoading(false)
                        if (it) {
                            tabFeatureAdapter.updateStateItem(tabId, !isActived)
                        } else {
                            tabFeatureAdapter.updateStateItem(tabId, isActived)
                            showCustomToast("Cập nhật thất bại")
                        }
                    }
                }
            }

            TAB_ID.FEATURE_HI -> {
                showHideLoading(true)
                viewModel.setHumanDetection(!isActived) {
                    lifecycleScope.launchWhenStarted {
                        showHideLoading(false)
                        if (it) {
                            tabFeatureAdapter.updateStateItem(tabId, !isActived)
                        } else {
                            tabFeatureAdapter.updateStateItem(tabId, isActived)
                            showCustomToast("Cập nhật thất bại")
                        }
                    }
                }
            }

            TAB_ID.FEATURE_DEFENCE -> {
                if (!stopAudio) {
                    showHideLoading(true)
                    if (isActived) {
                        JFCameraManager.setStatusDefence(devId, "Stop") {
                            lifecycleScope.launchWhenStarted {
                                showHideLoading(false)
                                if (it) {
                                    jobDefencing?.cancel()
                                    tabFeatureAdapter.updateStateItem(TAB_ID.FEATURE_DEFENCE, false)
                                    binding.clShowDefinition.gone()
                                    binding.tvContentDefinition.text = "60s"
                                    isActiveDefenceModeOn = false
                                } else {
                                    tabFeatureAdapter.updateStateItem(tabId, true)
                                }
                            }
                        }
                    } else {
                        JFCameraManager.setStatusDefence(devId, "Start") {
                            lifecycleScope.launchWhenStarted {
                                showHideLoading(false)
                                if (it) {
                                    jobDefencing?.cancel()
                                    jobDefencing = lifecycleScope.launch {
                                        withTimeout(61000) {
                                            isActiveDefenceModeOn = true
                                            repeat(60) {
                                                lifecycleScope.launchWhenStarted {
                                                    binding.clShowDefinition.visible()
                                                    if (tabFeatureAdapter.getEnableItem(TAB_ID.FEATURE_DEFENCE) &&
                                                        !tabFeatureAdapter.getStateItem(TAB_ID.FEATURE_DEFENCE)
                                                    ) {
                                                        tabFeatureAdapter.updateStateItem(
                                                            TAB_ID.FEATURE_DEFENCE,
                                                            true
                                                        )
                                                    }
                                                    binding.tvContentDefinition.text = "${60 - it}s"
                                                }
                                                delay(1000)
                                            }
                                            isActiveDefenceModeOn = false
                                            lifecycleScope.launchWhenStarted {
                                                tabFeatureAdapter.updateStateItem(
                                                    TAB_ID.FEATURE_DEFENCE,
                                                    false
                                                )
                                                binding.clShowDefinition.gone()
                                                binding.tvContentDefinition.text = "60s"
                                            }
                                        }
                                    }
                                } else {
                                    tabFeatureAdapter.updateStateItem(tabId, false)
                                }
                            }
                        }
                    }
                } else {
                    tabFeatureAdapter.updateStateItem(TAB_ID.FEATURE_DEFENCE, false)
                }
            }

            else -> {}
        }
    }

    private fun checkPrivateButtonFeature(tabId: TAB_ID, isActived: Boolean) {
        showHideLoading(true)
        if (!isActived) {
            JFCameraManager.setStatusMask(devId, true) { isSuccess ->
                showHideLoading(false)
                if (isSuccess) {
                    tabFeatureAdapter.updateStateItem(tabId, !isActived)
                    enableBtnControl(false)
                    binding.ivViewPrivateMode.visible()
                    binding.groupPrivateMode.visible()
                    setShowHideControlPrivateMode(true)

                    // Hủy đàm thoại khi bật chế độ riêng tư
                    if (stopAudio) {
                        destroyTalk()
                    }
                }
            }
        } else {
            JFCameraManager.setStatusMask(devId, false) { isSuccess ->
                showHideLoading(false)
                if (isSuccess) {

                    tabFeatureAdapter.updateStateItem(tabId, !isActived)

                    requestStream()
                    binding.ivViewPrivateMode.gone()
                    binding.groupPrivateMode.gone()
                    setShowHideControlPrivateMode(false)
                }
            }
        }
    }

    private fun checkCameraIndoor(): Boolean {
        return modelCamera == CAMERA_JF_INDOOR
    }

    companion object {
        var isHasNotify = false
    }

    private fun eventLiveView() {
        viewModel.mediaManager.setOnMediaManagerListener(object : OnMediaManagerListener {
            /**
             * State of play
             * @param attribute Player Properties（Includes State of play reference values, etc）
             * @param state State of play
             */
            override fun onMediaPlayState(attribute: PlayerAttribute<*>?, state: Int) {
                if (context == null) return
                when (state) {
                    PlayerAttribute.E_STATE_PlAY -> {
                        needShowErrorLiveview = true
                        enableBtnControl(true)
                        DebugConfig.log("eventLiveView", "E_STATE_PlAY")
                        lifecycleScope.launchWhenStarted {
                            binding.cameraView.visible()
                            binding.ivRetryStreaming.gone()
                            binding.tvShowStatusConnect.gone()
                            binding.fragmentCameraStreamPgLoading.gone()
                            saveScreenShot()
                        }
                    }

                    PlayerAttribute.E_STATE_PAUSE -> {
                        DebugConfig.log("eventLiveView", "E_STATE_PAUSE")
                    }

                    PlayerAttribute.E_STATE_BUFFER -> {
                        DebugConfig.log("eventLiveView", "E_STATE_BUFFER")
                    }

                    PlayerAttribute.E_STATE_STOP -> {
                        enableBtnControl(false)
                        DebugConfig.log("eventLiveView", "E_STATE_STOP")
                        lifecycleScope.launchWhenStarted {
                            if (needShowErrorLiveview) {
                                binding.cameraView.gone()
                                showErrorLiveStream()
                            }
                        }
                    }

                    PlayerAttribute.E_STATE_RESUME -> {
                        DebugConfig.log("eventLiveView", "E_STATE_RESUME")
                    }

                    PlayerAttribute.E_STATE_MEDIA_SOUND_ON -> {
                        DebugConfig.log("eventLiveView", "E_STATE_MEDIA_SOUND_ON")
                    }

                    PlayerAttribute.E_STATE_MEDIA_SOUND_OFF -> {
                        DebugConfig.log("eventLiveView", "E_STATE_MEDIA_SOUND_OFF")
                    }

                    PlayerAttribute.E_STATE_PLAY_COMPLETED -> {
                        DebugConfig.log("eventLiveView", "E_STATE_PLAY_COMPLETED")
                    }

                    PlayerAttribute.E_STATE_PLAY_SEEK -> {
                        DebugConfig.log("eventLiveView", "E_STATE_PLAY_SEEK")
                    }

                    PlayerAttribute.E_STATE_SAVE_RECORD_FILE_S -> {
                        DebugConfig.log("eventLiveView", "E_STATE_SAVE_RECORD_FILE_S")
                    }

                    PlayerAttribute.E_STATE_SAVE_PIC_FILE_S -> {
                        DebugConfig.log("eventLiveView", "E_STATE_SAVE_PIC_FILE_S")
                    }

                    else -> {
                        DebugConfig.log("eventLiveView", "E_STATE_ELSE")
                    }
                }
            }

            /**
             * Play failure
             * @param attribute Player Properties（Include State of play reference value and so on）
             * @param msgId Message Id
             * @param errorId For the error Id, see EFUN_ERROR
             */
            override fun onFailed(attribute: PlayerAttribute<*>?, msgId: Int, errorId: Int) {
                DebugConfig.log("eventLiveView", "onFailed  msgId: $msgId ,errorId: $errorId")
                if (context == null) return
                enableBtnControl(false)
                //  retryStreaming()
            }

            /**
             * Displays the stream and timestamp
             * @param attribute Player Properties（Include State of play reference value and so on）
             * @param isShowTime Whether to display the timestamp
             * @param time Time stamp
             * @param rate stream
             */
            override fun onShowRateAndTime(
                attribute: PlayerAttribute<*>?,
                isShowTime: Boolean,
                time: String,
                rate: String,
            ) {
                if (context == null) return
                val rateString = UtilsJava.formatBitrate(rate.toLong())
                binding?.let {
                    try {
                        it.tvCameraName?.text = "$devName  -  $rateString"
                        it.tvBitrate?.text = "$rateString"
                    } catch (e: Exception) {
                        DebugConfig.log(
                            "onShowRateAndTime",
                            "onShowRateAndTime ERROR: ${e.message}"
                        )
                    }
                }
                Log.d(TAG, "onShowRateAndTime: rate = $rateString")
            }

            /**
             * End of video buffer
             * @param attribute Player Properties（Include State of play reference value and so on）
             * @param ex
             */
            override fun onVideoBufferEnd(attribute: PlayerAttribute<*>?, ex: MsgContent) {

            }

            override fun onPlayStateClick(p0: View?) {

            }
        })

    }

    private fun setShowHideControlPrivateMode(isShow: Boolean) {
        if (isShow) {
            binding.layoutControl.btnVolumeControl.gone()
            binding.layoutControl.btnDefinition.gone()
            binding.layoutControl.btnZoomControl.gone()
        } else {
            binding.layoutControl.btnVolumeControl.visible()
            binding.layoutControl.btnDefinition.visible()
            binding.layoutControl.btnZoomControl.visible()
        }
    }

    override fun onNetworkConnectionChanged(isConnected: Boolean) {
        Log.d(TAG, "onNetworkConnectionChanged: $isConnected")
        if (!isConnected) {
            forceStopRecord(false)
            showCustomNotificationDialog(
                "Đang mất kết nối, bạn vui lòng kiểm tra lại kết nối và thử lại",
                DialogType.ERROR,
                titleBtnConfirm = com.vht.sdkcore.R.string.notification_understand,
            ) {}
        }
    }

    private fun setUIInformationWifiCam(wifiSignalLevel: Int, isLAN: Boolean) {
        if (isLAN) {
            binding.toolbar.setSubTitle(
                R.drawable.ic_lan_connection,
                getString(com.vht.sdkcore.R.string.wired_network)
            )
        } else when (wifiSignalLevel) {
            in 1..49 -> {
                binding.toolbar.setSubTitle(
                    R.drawable.ic_wifi_live_camera_1,
                    getString(com.vht.sdkcore.R.string.weak_signal)
                )
            }

            in 50..79 -> {
                binding.toolbar.setSubTitle(
                    R.drawable.ic_wifi_live_camera_2,
                    getString(com.vht.sdkcore.R.string.normal_signal)
                )
            }

            in 80..100 -> {
                binding.toolbar.setSubTitle(
                    R.drawable.ic_wifi_live_camera_3,
                    getString(com.vht.sdkcore.R.string.good_signal)
                )
            }

            else -> {
                binding.toolbar.setSubTitle(0, getString(com.vht.sdkcore.R.string.no_signal))
            }
        }
    }

    private fun setUITabServiceFreeCloud() {
        viewModel.dataListPricingCloud.observe(viewLifecycleOwner) {
            when (it.status) {
                Status.SUCCESS -> {
                    if (it.data?.code == 200) {
                        it?.data?.data?.let { frees ->
                            frees.promotions?.free?.let { listFreesData ->
                                val cameraFree = listFreesData.find { it.id == viewModel.deviceId }
                                if (cameraFree != null) {
                                    binding.tabService.clGroupRegister.gone()
                                    binding.tabService.clGroupFreeCloud.visible()
                                    val time = frees.promotions?.cloudTime ?: 0
                                    val contentFree = requireContext().getString(
                                        com.vht.sdkcore.R.string.string_cloud_free_live_view,
                                        frees.promotions?.cloudPeriod ?: 0,
                                        time.div(30)
                                    )
                                    binding.tabService.tvContent.text = getContentFree(contentFree)
                                    binding.tabService.btnRegisterFreeCloud.setOnClickListener {
                                        //   appNavigation.openDetailPromotionFree()
                                    }
                                } else {
                                    binding.tabService.clGroupRegister.visible()
                                    binding.tabService.clGroupFreeCloud.gone()
                                }
                            }
                        }
                    }
                }

                else -> {}
            }
        }
    }

    private fun getContentFree(content: String): SpannableString {
        val spannableString = SpannableString(content)
        return try {
            val startIndex = content.indexOf("MIỄN PHÍ")
            val endIndex = startIndex + "MIỄN PHÍ".length
            val boldSpan = StyleSpan(Typeface.BOLD)
            spannableString.setSpan(boldSpan, startIndex, endIndex, 0)
            spannableString
        } catch (e: Exception) {
            spannableString
        }
    }

    private fun stopActiveDefenceMode() {
        JFCameraManager.setStatusDefence(devId, "Stop") {
            lifecycleScope.launchWhenStarted {
                if (it) {
                    jobDefencing?.cancel()
                    binding.clShowDefinition.gone()
                    binding.tvContentDefinition.text = "60s"
                    isActiveDefenceModeOn = false
                    if (tabFeatureAdapter.getEnableItem(TAB_ID.FEATURE_DEFENCE)) {
                        tabFeatureAdapter.updateStateItem(TAB_ID.FEATURE_DEFENCE, false)
                    }
                }
            }
        }
    }
}

enum class ActionControlPTZ(val action: String) {
    UP("1"),
    DOWN("2"),
    LEFT("3"),
    RIGHT("4"),
    STOP("5"),
}