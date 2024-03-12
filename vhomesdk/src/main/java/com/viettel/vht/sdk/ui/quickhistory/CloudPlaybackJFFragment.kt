package com.viettel.vht.sdk.ui.quickhistory

import android.annotation.SuppressLint
import android.app.ActionBar
import android.app.DatePickerDialog
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.Message
import android.os.SystemClock
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Chronometer
import android.widget.FrameLayout
import androidx.activity.OnBackPressedCallback
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.lib.EUIMSG
import com.lib.FunSDK
import com.lib.IFunSDKResult
import com.lib.MsgContent
import com.lib.SDKCONST
import com.lib.cloud.CloudDirectory
import com.lib.sdk.bean.StringUtils
import com.lib.sdk.struct.H264_DVR_FILE_DATA
import com.manager.db.DownloadInfo
import com.manager.device.DeviceManager
import com.manager.device.media.MediaManager
import com.manager.device.media.MediaManager.PLAY_CLOUD_PLAYBACK
import com.manager.device.media.attribute.PlayerAttribute
import com.manager.device.media.download.DownloadManager
import com.manager.device.media.download.DownloadManager.OnDownloadListener
import com.manager.device.media.playback.CloudRecordManager
import com.manager.device.media.playback.RecordManager
import com.utils.TimeUtils
import com.vht.sdkcore.base.BaseFragment
import com.vht.sdkcore.utils.AppLog
import com.vht.sdkcore.utils.Constants
import com.vht.sdkcore.utils.Define
import com.vht.sdkcore.utils.Utils
import com.vht.sdkcore.utils.Utils.Companion.folderThumbnailInternalStoragePath
import com.vht.sdkcore.utils.dialog.DialogType
import com.vht.sdkcore.utils.toast
import com.vht.sdkcore.utils.toastMessage
import com.viettel.vht.sdk.R
import com.viettel.vht.sdk.databinding.FragmentHistoryPlaybackJfBinding
import com.viettel.vht.sdk.model.camera_cloud.CloudStorageRegistered
import com.viettel.vht.sdk.navigation.AppNavigation
import com.viettel.vht.sdk.ui.jfcameradetail.view.dialog.ConfirmSaveMediaDialog
import com.viettel.vht.sdk.utils.DebugConfig
import com.viettel.vht.sdk.utils.ImageUtils
import com.viettel.vht.sdk.utils.NetworkChangeReceiver
import com.viettel.vht.sdk.utils.NetworkManager
import com.viettel.vht.sdk.utils.convertDateToStartDate
import com.viettel.vht.sdk.utils.custom.EventHistory
import com.viettel.vht.sdk.utils.disable
import com.viettel.vht.sdk.utils.enable
import com.viettel.vht.sdk.utils.getMidnightCalendar
import com.viettel.vht.sdk.utils.gone
import com.viettel.vht.sdk.utils.isNotEmpty
import com.viettel.vht.sdk.utils.showCustomNotificationDialog
import com.viettel.vht.sdk.utils.toDate
import com.viettel.vht.sdk.utils.visible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.internal.toImmutableList
import java.io.File
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

@AndroidEntryPoint
class CloudPlaybackJFFragment :
    BaseFragment<FragmentHistoryPlaybackJfBinding, HistoryDetailLiveViewModel>(),
    MediaManager.OnRecordManagerListener,
    OnDownloadListener, IFunSDKResult, NetworkChangeReceiver.ConnectivityReceiverListener {
    private lateinit var pathVideo: String
    private val TAG: String = CloudPlaybackJFFragment::class.java.simpleName
    private val viewModel: HistoryDetailLiveViewModel by viewModels()
    override val layoutId: Int
        get() = R.layout.fragment_history_playback_jf

    override fun getVM(): HistoryDetailLiveViewModel = viewModel

    @Inject
    lateinit var appNavigation: AppNavigation

    private lateinit var recordManager: RecordManager
    lateinit var downloadManager: DownloadManager
    var devId: String? = null
    var devName: String? = null
    var listFile = mutableListOf<PlaybackFile>()
    var currentItemFile: PlaybackFile? = null
    private var isRegistedCloud: Boolean? = null
    private var mUserId = 0
    private var mThumbDownloadId = 0
    var isRecord = false
    private var mCutPlayTimes: Int = 0
    private var isNoEvent = false

    private val isShareCameraJF: Boolean
        get() = requireArguments().getBoolean(Define.BUNDLE_KEY.PARAM_IS_SHARE_CAMERA_JF, false)
            ?: false

    private var playSpeed = 0
    private val listenerChangePlaySpeed =
        object : BottomSheetChangePlaybackRateJF.OnSelectPlaySpeed {
            override fun onSelectPlaySpeed(playSpeed: Int) {
                selectPlaySpeed(playSpeed)
            }
        }
    private val receiverNetwork = NetworkChangeReceiver()

    override fun initView() {
        super.initView()
        arguments?.let {
//            if (it.containsKey(Define.BUNDLE_KEY.PARAM_TIME)) {
//                viewModel.dateLiveData.value = it.getLong(Define.BUNDLE_KEY.PARAM_TIME)
//            }
            if (it.containsKey(Define.BUNDLE_KEY.PARAM_START_TIME)) {
                viewModel.dateLiveData.value = it.getLong(Define.BUNDLE_KEY.PARAM_START_TIME)
            }
        }
        initClick()
        initObserver()
        initLayout()
        initRecordManager()
    }

    private fun initRecordManager() {
        DebugConfig.log(TAG, "initRecordManager")
        downloadManager = DownloadManager.getInstance(this)
        arguments?.let {
            if (it.containsKey(Define.BUNDLE_KEY.PARAM_DEVICE_SERIAL)) {
                devId = it.getString(Define.BUNDLE_KEY.PARAM_DEVICE_SERIAL)
                devName = it.getString(Define.BUNDLE_KEY.PARAM_NAME)
                binding.tvCameraName.text = devName
                binding.layoutTimeLine.devJFId = devId
                DebugConfig.log(TAG, "devId = $devId")
                recordManager =
                    DeviceManager.getInstance()
                        .createRecordPlayer(binding.layoutPlayWnd, devId, PLAY_CLOUD_PLAYBACK)
                recordManager.setChnId(0)
                recordManager.setOnMediaManagerListener(this)
                recordManager.openVoiceBySound()
                selectPlaySpeed(0)
                viewModel.getListCloudStorageRegistered(
                    devId ?: "",
                    AppLog.LogPlayBackCloud.TRACKING_SELECT_DATE_CLOUD.screenID,
                    AppLog.LogPlayBackCloud.TRACKING_SELECT_DATE_CLOUD.actionID
                )
                viewModel.cloudRegistered.observe(viewLifecycleOwner) {
                    setUpCloudStatus(it)
                }
            }
        }
    }

    private fun setUpCloudStatus(listCloudPackage: List<CloudStorageRegistered>?) {
        if (!NetworkManager.isOnline(requireContext())) {
            binding.tvMessage.gone()
            showErrorLiveStream()
            return
        } else {
            hideErrorLiveStream()
        }

        if (listCloudPackage != null && listCloudPackage.isNotEmpty()) {
            binding.tvMessage.text = getString(com.vht.sdkcore.R.string.no_event_selected_time)
            binding.tvMessage.visible()
            enableLayoutControl(false)
            isRegistedCloud = true
            binding.layoutTimeLine.visible()
            binding.containerDate.visible()
            binding.containerEmpty.gone()
            if (isShareCameraJF) {
                binding.layoutExpries.gone()
            } else {
                binding.layoutExpries.visible()
            }
            binding.layoutExpries.setOnClickRenew {
                if (isShareCameraJF) return@setOnClickRenew
                val bundle = Bundle().apply {
                    putString(
                        Define.BUNDLE_KEY.PARAM_ID,
                        arguments?.getString(Define.BUNDLE_KEY.PARAM_ID) ?: ""
                    )
                    putString(Define.BUNDLE_KEY.PARAM_NAME, devName ?: devId)
                    putString(Define.BUNDLE_KEY.PARAM_DEVICE_SERIAL, devId)
                    putString(Define.BUNDLE_KEY.PARAM_DEVICE_TYPE, Define.TYPE_DEVICE.CAMERA_JF)
                }
                appNavigation.openCloudStorageCamera(bundle)
            }
            listCloudPackage.forEach {
                if (it.serviceStatus == 1) {
                    binding.layoutExpries.gone()
                    binding.tvMessage.gone()
                    return@forEach
                }
            }
            viewModel.searchVideoCloud(recordManager) {
                onSearchResult(it)
            }
        } else {
            isRegistedCloud = false
            binding.layoutTimeLine.gone()
            binding.containerDate.gone()
            binding.containerEmpty.visible()
            if (isShareCameraJF) {
                binding.tvDescriptionEmpty.gone()
            }
            binding.tvMessage.text = getString(com.vht.sdkcore.R.string.no_data_to_show)
            binding.tvMessage.visible()
            enableLayoutControl(false)
        }
    }

    private fun initObserver() {

        mUserId = FunSDK.RegUser(this)

        viewModel.dateLiveData.observe(viewLifecycleOwner) { longDate ->
            arguments?.let {
                if (it.containsKey(Define.BUNDLE_KEY.PARAM_START_TIME)) {
                    it.putLong(Define.BUNDLE_KEY.PARAM_START_TIME, longDate)
                }
            }
            binding.layoutTimeLine.setList(mutableListOf())
            selectPlaySpeed(0)
            recordManager.destroyPlay()
            if (getMidnightCalendar(longDate).timeInMillis == getMidnightCalendar(System.currentTimeMillis()).timeInMillis) {
                binding.ivNextDay.disable()
            } else {
                binding.ivNextDay.enable()
            }
            val minCalendar = Calendar.getInstance().apply {
                add(Calendar.DATE, -29)
            }
            if (getMidnightCalendar(longDate).timeInMillis == getMidnightCalendar(minCalendar.timeInMillis).timeInMillis) {
                binding.ivPreviousDay.disable()
            } else {
                binding.ivPreviousDay.enable()
            }
            binding.textViewDate.text = longDate.toDate()
            binding.tvMessage.gone()
            if (isRegistedCloud != null) {
                if (isRegistedCloud == true) {
                    enableLayoutControl(false)
                    viewModel.searchVideoCloud(recordManager) {
                        onSearchResult(it)
                    }
                } else {
                    binding.tvMessage.text = getString(com.vht.sdkcore.R.string.no_data_to_show)
                    binding.tvMessage.visible()
                    binding.layoutTimeLine.gone()
                    binding.containerDate.gone()
                    binding.containerEmpty.visible()
                    if (isShareCameraJF) {
                        binding.tvDescriptionEmpty.gone()
                    }
                }
            }
        }
    }


    private fun initLayout() {
        displayMinimalScreenVideo()
        binding.layoutBtnControl.layoutAudio.gone()
        binding.layoutBtnControl.layoutHistory.gone()
        binding.layoutControl.btnZoomControl.setImageResource(R.drawable.ic_live_view_full_screen)
        binding.layoutControl.btnRate.visible()
        binding.layoutRightSidePlayback.btnRate.visible()
        binding.layoutRightSidePlayback.btnZoomControl.setImageResource(R.drawable.ic_live_view_full_screen)
        binding.layoutRightSidePlayback.btnZoomControl.setImageResource(R.drawable.ic_live_view_full_screen)
        binding.layoutRightSidePlayback.btnRecordVideo.setBackgroundResource(R.drawable.bg_control_live_view)
        binding.layoutRightSidePlayback.btnRecordVideo.setImageResource(R.drawable.ic_un_recording)
        binding.layoutRightSidePlayback.btnCaptureImage.setImageResource(R.drawable.ic_take_picuture_snap_shot)
        enableLayoutControl(false)
    }


    fun initClick() {
        binding.textViewDate.setOnClickListener {
            if (checkStopRecord()) return@setOnClickListener
            //  showDatePickerDialog()
            BottomSheetDatePickerFragment().show(
                childFragmentManager,
                "BottomSheetDatePickerFragment"
            )
        }

        binding.ivPreviousDay.setOnClickListener {
            if (checkStopRecord()) return@setOnClickListener
            lifecycleScope.launch {
                viewModel.previousDay()
                viewModel.sendLogForCloudPackage(
                    screenID = AppLog.LogPlayBackCloud.TRACKING_SELECT_DATE_CLOUD.screenID,
                    actionID = AppLog.LogPlayBackCloud.TRACKING_SELECT_DATE_CLOUD.actionID
                )
            }
        }
        binding.ivNextDay.setOnClickListener {
            if (checkStopRecord()) return@setOnClickListener
            lifecycleScope.launch {
                viewModel.nextDay()
                viewModel.sendLogForCloudPackage(
                    screenID = AppLog.LogPlayBackCloud.TRACKING_SELECT_DATE_CLOUD.screenID,
                    actionID = AppLog.LogPlayBackCloud.TRACKING_SELECT_DATE_CLOUD.actionID
                )
            }
        }
        binding.tvCameraName.setOnClickListener {
            toggleFullScreenMode()
        }

        binding.layoutToolbarFullscreen.btnLeft.setOnClickListener {
            toggleFullScreenMode()
        }

        binding.layoutControl.btnZoomControl.setOnClickListener {
            toggleFullScreenMode()
        }

        binding.layoutRightSidePlayback.btnZoomControl.setOnClickListener {
            binding.layoutRightSidePlayback.layoutRate.root.gone()
            toggleFullScreenMode()
        }
        binding.layoutBtnControl.btnRecord.setOnClickListener {
            requestDownloadVideo()
        }
        binding.layoutRightSidePlayback.btnRecordVideo.setOnClickListener {
            binding.layoutRightSidePlayback.layoutRate.root.gone()
            requestDownloadVideo()
        }
        binding.layoutBtnControl.btnSnapShot.setOnClickListener {
            requestScreenShot()
        }
        binding.layoutRightSidePlayback.btnCaptureImage.setOnClickListener {
            binding.layoutRightSidePlayback.layoutRate.root.gone()
            requestScreenShot()
        }
        binding.tvMore.setOnClickListener {
            if (checkStopRecord()) return@setOnClickListener
            bundleOf().apply {
                putString(Define.BUNDLE_KEY.PARAM_DEVICE_ID, devId)
              //  putInt(Define.BUNDLE_KEY.PARAM_TYPE, CLOUD_TYPE)
                putString(
                    Define.BUNDLE_KEY.PARAM_ID,
                    arguments?.getString(Define.BUNDLE_KEY.PARAM_ID) ?: ""
                )
                putLong(
                    Define.BUNDLE_KEY.PARAM_TIME,
                    viewModel.dateLiveData.value ?: System.currentTimeMillis()
                )
                putBoolean(Define.BUNDLE_KEY.PARAM_IS_SHARE_CAMERA_JF, isShareCameraJF)
                //todo open list video cloud
                putString(Define.BUNDLE_KEY.PARAM_NAME, devName)
              //  appNavigation.openListVideoCloudFragment(this)
            }
        }
        binding.layoutTimeLine.getViewDisable().setOnClickListener {
            checkStopRecord()
        }
        binding.recordTimer.onChronometerTickListener =
            Chronometer.OnChronometerTickListener {
                val time = (SystemClock.elapsedRealtime() - binding.recordTimer.base).toInt()
                if ((time / 1000 - 300) == 1) {
                    // 5 phút
                    startStopRecord()
                }
            }
        binding.layoutTimeLine.onValueChange = {
            lifecycleScope.launchWhenStarted {
                isNoEvent = false
                enableLayoutControl(false)
                binding.layoutRightSidePlayback.layoutRate.root.gone()
                if (!listFile.isNullOrEmpty()) {
                    recordManager.stopPlay()
                }
            }
        }
        binding.layoutTimeLine.onNoEvent = {
            lifecycleScope.launchWhenStarted {
                isNoEvent = true
                enableLayoutControl(false)
                recordManager.stopPlay()
                binding.layoutPlayWnd.gone()
                if (NetworkManager.isOnline(requireContext())) {
                    binding.tvMessage.visible()
                    binding.tvMessage.text = getString(com.vht.sdkcore.R.string.all_event_played)
                }
            }
        }
        binding.layoutTimeLine.onClickThumb = { event ->
            lifecycleScope.launchWhenStarted {
                if (!listFile.isNullOrEmpty()) {
                    enableLayoutControl(false)
                    binding.tvMessage.gone()
                    binding.layoutPlayWnd.visible()
                    recordManager.stopPlay()
                    currentItemFile =
                        listFile.find { it.startTime == event?.timeStart }
                    val searchTime = Calendar.getInstance()
                    searchTime.time = Date(viewModel.dateLiveData.value ?: 0)
                    searchTime.set(Calendar.HOUR_OF_DAY, 0)
                    searchTime.set(Calendar.MINUTE, 0)
                    searchTime.set(Calendar.SECOND, 0)
                    viewModel.seekToTime(
                        recordManager,
                        (event?.timeStart?.minus(searchTime.time.time))?.toInt()?.div(1000) ?: 0
                    )
                    recordManager.setPlaySpeed(playSpeed)
                }
            }
        }
        binding.layoutTimeLine.onJFSeekTimeChange = { currentVideo, currentTime ->
            lifecycleScope.launchWhenStarted {
                if (!listFile.isNullOrEmpty() && currentVideo != null) {
                    enableLayoutControl(false)
                    binding.tvMessage.gone()
                    binding.layoutPlayWnd.visible()
                    currentItemFile =
                        listFile.find { it.startTime == currentVideo?.timeStart }
                    DebugConfig.log(TAG, "onSeekTimeChange: time = $currentTime")
                    recordManager.stopPlay()
                    viewModel.seekToTime(recordManager, currentTime)
                    recordManager.setPlaySpeed(playSpeed)
                }
            }
        }
        binding.layoutControl.btnVolumeControl.setOnClickListener {
            viewModel.isDisableVolume.value = !viewModel.isDisableVolume.value!!
            if (viewModel.isDisableVolume.value == false) {
                recordManager.openVoiceBySound()
                binding.layoutControl.btnVolumeControl.setImageResource(R.drawable.ic_volume_on)
            } else {
                recordManager.closeVoiceBySound()
                binding.layoutControl.btnVolumeControl.setImageResource(R.drawable.ic_volume_off)
            }
        }
        binding.tvDescriptionEmpty.setOnClickListener {
            val bundle = Bundle().apply {
                putString(
                    Define.BUNDLE_KEY.PARAM_ID,
                    arguments?.getString(Define.BUNDLE_KEY.PARAM_ID) ?: ""
                )
                putString(Define.BUNDLE_KEY.PARAM_NAME, devName ?: devId)
                putString(Define.BUNDLE_KEY.PARAM_DEVICE_SERIAL, devId)
                putString(Define.BUNDLE_KEY.PARAM_DEVICE_TYPE, Define.TYPE_DEVICE.CAMERA_JF)
            }
            appNavigation.openCloudStorageCamera(bundle)
        }

        binding.layoutControl.btnRate.setOnClickListener {
            BottomSheetChangePlaybackRateJF
                .newInstance(listenerChangePlaySpeed, playSpeed)
                .show(childFragmentManager, BottomSheetChangePlaybackRateJF.TAG)
        }

        binding.layoutRightSidePlayback.btnRate.setOnClickListener {
            binding.layoutRightSidePlayback.layoutRate.root.isVisible =
                !binding.layoutRightSidePlayback.layoutRate.root.isVisible
        }
        binding.layoutRightSidePlayback.layoutRate.btn4X.setOnClickListener {
            selectPlaySpeed(2)
        }
        binding.layoutRightSidePlayback.layoutRate.btn2X.setOnClickListener {
            selectPlaySpeed(1)
        }
        binding.layoutRightSidePlayback.layoutRate.btn1X.setOnClickListener {
            selectPlaySpeed(0)
        }
        binding.layoutRightSidePlayback.layoutRate.btn05X.setOnClickListener {
            selectPlaySpeed(-1)
        }
        binding.layoutRightSidePlayback.layoutRate.btn025X.setOnClickListener {
            selectPlaySpeed(-2)
        }

        binding.layoutRightSidePlayback.btnVolumeControl.setOnClickListener {
            binding.layoutRightSidePlayback.layoutRate.root.gone()
            viewModel.isDisableVolume.value = !viewModel.isDisableVolume.value!!
            if (viewModel.isDisableVolume.value == false) {
                recordManager.openVoiceBySound()
                binding.layoutRightSidePlayback.btnVolumeControl.setImageResource(R.drawable.ic_volume_on)
            } else {
                recordManager.closeVoiceBySound()
                binding.layoutRightSidePlayback.btnVolumeControl.setImageResource(R.drawable.ic_volume_off)
            }
        }
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


        binding.view.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                when (event?.action) {
                    MotionEvent.ACTION_DOWN -> {
                        if (binding.layoutRightSidePlayback.layoutRate.root.isVisible) {
                            binding.layoutRightSidePlayback.layoutRate.root.gone()
                            return false
                        }
                        activity?.resources?.configuration?.orientation?.let { orientation ->
                            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                                if (binding.layoutRightSidePlayback.root.isVisible) {
                                    binding.layoutTimeLine.gone()
                                    binding.layoutRightSidePlayback.root.gone()
                                    binding.tvCameraName.gone()
                                } else {
                                    binding.layoutTimeLine.visible()
                                    binding.layoutRightSidePlayback.root.visible()
                                    binding.tvCameraName.visible()
                                }
                            }
                        }


                    }
                }
                return false
            }

        })

        binding.ivRetryStreaming.setOnClickListener {
            if (!NetworkManager.isOnline(requireContext())) {
                binding.tvMessage.gone()
                showCustomNotificationDialog(
                    requireContext().getString(com.vht.sdkcore.R.string.no_connection),
                    DialogType.ERROR,
                    titleBtnConfirm = com.vht.sdkcore.R.string.notification_understand,
                ) {}
            } else {
                hideErrorLiveStream()
                if (recordManager.playState.equals(PlayerAttribute.E_STATE_PAUSE)) {
                    recordManager.rePlay()
                } else if (mCutPlayTimes > 0) {
                    binding.layoutPlayWnd.visible()
                    viewModel.seekToTime(recordManager, mCutPlayTimes)
                }
            }
        }

    }

    fun checkStopRecord(): Boolean {
        if (isRecord) {
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
        return isRecord
    }

    private fun forceStopRecording() {
        val time = SystemClock.elapsedRealtime() - binding.recordTimer.base
        if (time < 5000L) {
            toastMessage(getString(com.vht.sdkcore.R.string.string_error_recording_live))
            return
        }
        binding.layoutTimeLine.enableTimeline(true)
        recordManager.stopRecord()
        isRecord = false
        DebugConfig.log(TAG, "Stop record")
        setUIRecording(false)
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

    @SuppressLint("SetTextI18n")
    private fun selectPlaySpeed(speed: Int) {
        if (!this::recordManager.isInitialized) return
        lifecycleScope.launchWhenStarted {
            binding.layoutRightSidePlayback.layoutRate.root.gone()
            binding.layoutRightSidePlayback.layoutRate.checkboxRate025x.gone()
            binding.layoutRightSidePlayback.layoutRate.checkboxRate05x.gone()
            binding.layoutRightSidePlayback.layoutRate.checkboxRate1x.gone()
            binding.layoutRightSidePlayback.layoutRate.checkboxRate2x.gone()
            binding.layoutRightSidePlayback.layoutRate.checkboxRate4x.gone()
            when (speed) {
                -2 -> {
                    binding.layoutControl.btnRate.text = "0.25x"
                    binding.layoutRightSidePlayback.btnRate.text = "0.25x"
                    binding.layoutRightSidePlayback.layoutRate.checkboxRate025x.visible()
                }

                -1 -> {
                    binding.layoutControl.btnRate.text = "0.5x"
                    binding.layoutRightSidePlayback.btnRate.text = "0.5x"
                    binding.layoutRightSidePlayback.layoutRate.checkboxRate05x.visible()
                }

                0 -> {
                    binding.layoutControl.btnRate.text = "1x"
                    binding.layoutRightSidePlayback.btnRate.text = "1x"
                    binding.layoutRightSidePlayback.layoutRate.checkboxRate1x.visible()
                }

                1 -> {
                    binding.layoutControl.btnRate.text = "2x"
                    binding.layoutRightSidePlayback.btnRate.text = "2x"
                    binding.layoutRightSidePlayback.layoutRate.checkboxRate2x.visible()
                }

                2 -> {
                    binding.layoutControl.btnRate.text = "4x"
                    binding.layoutRightSidePlayback.btnRate.text = "4x"
                    binding.layoutRightSidePlayback.layoutRate.checkboxRate4x.visible()
                }
            }
            recordManager.setPlaySpeed(speed)
            playSpeed = speed
        }
    }

    private fun setUIRecording(startRecording: Boolean = false) {
        if (startRecording) {
            binding.layoutBtnControl.btnRecord.setBackgroundResource(R.drawable.icon_bgr_button_actived)
            binding.layoutBtnControl.btnRecord.setImageResource(R.drawable.ic_stop_record)
            binding.layoutRightSidePlayback.btnRecordVideo.setBackgroundResource(R.drawable.bg_camera_control_inside_3)
            binding.layoutRightSidePlayback.btnRecordVideo.setImageResource(R.drawable.ic_stop_record)
            binding.recordTimer.base = SystemClock.elapsedRealtime()
            binding.recordTimer.start()
            binding.recordTimer.visibility = View.VISIBLE
        } else {
            binding.layoutBtnControl.btnRecord.setBackgroundResource(R.drawable.icon_bgr_button_normal)
            binding.layoutBtnControl.btnRecord.setImageResource(R.drawable.ic_camera_record_selector)
            binding.layoutRightSidePlayback.btnRecordVideo.setBackgroundResource(R.drawable.bg_control_live_view)
            binding.layoutRightSidePlayback.btnRecordVideo.setImageResource(R.drawable.ic_un_recording)
            binding.recordTimer.base = SystemClock.elapsedRealtime()
            binding.recordTimer.stop()
            binding.recordTimer.visibility = View.GONE
        }
    }

    private fun screenShot() {
        val dir = Utils.folderImageInternalStoragePath(requireContext())
        viewModel.capture(dir, recordManager) { path ->
            showCustomNotificationDialog(
                getString(com.vht.sdkcore.R.string.string_setting_notificaiton_alarm),
                DialogType.CONFIRM,
                com.vht.sdkcore.R.string.dialog_confirm_save_photo,
                com.vht.sdkcore.R.string.dialog_btn_save,
                com.vht.sdkcore.R.string.dialog_btn_cancel,
            ) {
                if (path.isNotEmpty()) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        ImageUtils.saveImageToGalleryJFCamera(requireContext(), path,devId?:"")
                    }
                }
            }
            DebugConfig.log(TAG, "screenShot: path = $path")
        }
    }

    private fun showDatePickerDialog() {
        val onDateSetListener =
            DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                val mCalendar = Calendar.getInstance()
                mCalendar.set(Calendar.YEAR, year)
                mCalendar.set(Calendar.MONTH, monthOfYear)
                mCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                viewModel.dateLiveData.value = mCalendar.timeInMillis
            }

        val mCalendar = Calendar.getInstance().apply {
            time = Date(viewModel.dateLiveData.value!!)
        }
        val datePickerDialog = DatePickerDialog(
            requireContext(), onDateSetListener, mCalendar.get(Calendar.YEAR),
            mCalendar.get(Calendar.MONTH), mCalendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
        //Must call show before getButton
        datePickerDialog.getButton(DatePickerDialog.BUTTON_NEGATIVE)
            .setTextColor(resources.getColor(com.vht.sdkcore.R.color.colorAccent, null))
        datePickerDialog.getButton(DatePickerDialog.BUTTON_POSITIVE)
            .setTextColor(resources.getColor(com.vht.sdkcore.R.color.colorAccent, null))
        datePickerDialog.datePicker.maxDate = Date().time
    }

    private fun requestDownloadVideo() {
        if (checkReadStoragePermission(true)) {
            startStopRecord()
        }

    }

    private fun requestScreenShot() {
        if (checkReadStoragePermission(true)) {
            screenShot()
        }
    }

    fun getCurrentTime() = viewModel.dateLiveData.value

    private fun startStopRecord() {
        devId?.let {
            if (isRecord) {
                binding.layoutTimeLine.enableTimeline(true)
                val time = SystemClock.elapsedRealtime() - binding.recordTimer.base
                if (time < 5000L) {
                    toastMessage(getString(com.vht.sdkcore.R.string.string_error_recording_live))
                    return
                }
                recordManager.stopRecord()
                isRecord = false
                DebugConfig.log(TAG, "Stop record")
                setUIRecording(false)
                val folder = File(requireContext().filesDir, "${Constants.PHOTO_FOLDER}/${devId}")
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
                                viewModel.saveVideoToGallery(path, devId!!)
                                context?.toast(getString(com.vht.sdkcore.R.string.string_recording_success))
                            }
                        } else {
                            DebugConfig.log(TAG, "Stop record fail")
                            context?.toast(getString(com.vht.sdkcore.R.string.record_fail))
                        }
                    }
                }
            } else {
                if (recordManager.playState == PlayerAttribute.E_STATE_PlAY) {
                    binding.layoutTimeLine.enableTimeline(false)
                    val folder =
                        File(requireContext().filesDir, "${Constants.PHOTO_FOLDER}/${devId}")
                    if (!folder.exists()) folder.mkdirs()
                    recordManager.startRecord(folder.path)
                    DebugConfig.log(TAG, "Start record")
                    isRecord = true
                    setUIRecording(true)
                } else {
                    DebugConfig.log(TAG, "Start record fail")
                    context?.toast(getString(com.vht.sdkcore.R.string.record_fail))
                }
            }
        }
//        currentItemFile?.let {
//            viewModel.downloadFile(
//                requireContext(),
//                downloadManager,
//                PLAY_CLOUD_PLAYBACK,
//                devId!!,
//                it
//            )
//        }
    }

    private fun toggleFullScreenMode() {
        //toggle full screen mode
        activity?.resources?.configuration?.orientation?.let { orientation ->
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                exitFullScreenModeJF()
                binding.tvCameraName.visibility = View.GONE
                binding.layoutToolbarFullscreen.root.visibility = View.GONE
                binding.layoutRightSidePlayback.root.visibility = View.GONE
                binding.layoutTimeLine.visible()
                if (isNoEvent) {
                    binding.layoutControl.root.gone()
                } else {
                    binding.layoutControl.root.visible()
                }
                activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                binding.layoutControl.btnZoomControl.setImageResource(R.drawable.ic_live_view_full_screen)
                binding.layoutRightSidePlayback.btnZoomControl.setImageResource(R.drawable.ic_live_view_full_screen)
                binding.layoutTimeLine.visible()
            } else {
                initFullscreenModeJF()
                binding.tvCameraName.visibility = View.VISIBLE
                binding.layoutRightSidePlayback.root.visibility = View.VISIBLE
                binding.layoutToolbarFullscreen.root.visibility = View.GONE
                binding.layoutControl.root.gone()
                activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                binding.layoutControl.btnZoomControl.setImageResource(R.drawable.ic_live_view_exit_full_screen_jf)
                binding.layoutRightSidePlayback.btnZoomControl.setImageResource(R.drawable.ic_live_view_exit_full_screen_jf)
            }
            if (viewModel.isDisableVolume.value == false) {
                recordManager.openVoiceBySound()
                binding.layoutRightSidePlayback.btnVolumeControl.setImageResource(R.drawable.ic_volume_on)
                binding.layoutControl.btnVolumeControl.setImageResource(R.drawable.ic_volume_on)
            } else {
                recordManager.closeVoiceBySound()
                binding.layoutRightSidePlayback.btnVolumeControl.setImageResource(R.drawable.ic_volume_off)
                binding.layoutControl.btnVolumeControl.setImageResource(R.drawable.ic_volume_off)
            }
        }
    }

    private fun displayFullScreenTimeline() {
        binding.layoutTimeLine.alpha = 0.5f
        val constraintSet = ConstraintSet()
        constraintSet.clone(binding.container)
        constraintSet.clear(R.id.layoutTimeLine, ConstraintSet.TOP)
        constraintSet.connect(
            R.id.layoutTimeLine,
            ConstraintSet.BOTTOM,
            ConstraintSet.PARENT_ID,
            ConstraintSet.BOTTOM,
            0
        )
        constraintSet.applyTo(binding.container)
        binding.layoutTimeLine.hideListThumb()
        binding.layoutTimeLine.showTextTime(false)
    }

    private fun displayMinimalScreenTimeline() {
        binding.layoutTimeLine.alpha = 1f
        val constraintSet = ConstraintSet()
        constraintSet.clone(binding.container)
        constraintSet.clear(R.id.layoutTimeLine, ConstraintSet.BOTTOM)
        constraintSet.connect(
            R.id.layoutTimeLine,
            ConstraintSet.TOP,
            R.id.containerDate,
            ConstraintSet.BOTTOM,
            0
        )
        constraintSet.applyTo(binding.container)
        binding.layoutTimeLine.showListThumb()
        binding.layoutTimeLine.showTextTime(true)
    }

    private fun displayFullScreenVideo() {
        val layoutVideoPlayer = binding.cameraLayout
        val textureView = binding.layoutPlayWnd
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
    }


    private fun displayMinimalScreenVideo() {
        val width = getMinimalScreenWidth()
        val height = getMinimalScreenHeight()
        val layoutVideoPlayer = binding.cameraLayout
        val textureView = binding.layoutPlayWnd
        layoutVideoPlayer.layoutParams = ConstraintLayout.LayoutParams(width, height)

        textureView.layoutParams = FrameLayout.LayoutParams(width, height)
        binding.view.layoutParams = FrameLayout.LayoutParams(width, height)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            displayFullScreenVideo()
            displayFullScreenTimeline()
//            recordManager.isVideoFullScreen = true

        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            displayMinimalScreenVideo()
            displayMinimalScreenTimeline()
//            recordManager.isVideoFullScreen = false
        }
    }

    override fun onResume() {
        if (recordManager.playState.equals(PlayerAttribute.E_STATE_PAUSE)) {
            recordManager.rePlay()
        } else if (mCutPlayTimes > 0) {
            viewModel.seekToTime(recordManager, mCutPlayTimes)
        }
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
        forceStopRecord()
        recordManager.pausePlay()
    }

    override fun onStop() {
        super.onStop()
        selectPlaySpeed(0)
        recordManager.stopPlay()
        context?.unregisterReceiver(receiverNetwork)
    }

    override fun onDestroy() {
        super.onDestroy()
        //resetPlaySpeed()
        recordManager.destroyPlay()
    }

    override fun onMediaPlayState(p0: PlayerAttribute<*>?, p1: Int) {
        DebugConfig.log(TAG, "onMediaPlayState: ${p0?.playState}")
        lifecycleScope.launchWhenStarted {
            if (p0?.playState == PlayerAttribute.E_STATE_PlAY) {
//                if (!isScreenOrientationLandscape()) {
//                    enableLayoutControl(true)
//                }
                enableLayoutControl(true)
                if (viewModel.isDisableVolume.value == false) {
                    recordManager.openVoiceBySound()
                    binding.layoutRightSidePlayback.btnVolumeControl.setImageResource(R.drawable.ic_volume_on)
                    binding.layoutControl.btnVolumeControl.setImageResource(R.drawable.ic_volume_on)
                } else {
                    recordManager.closeVoiceBySound()
                    binding.layoutRightSidePlayback.btnVolumeControl.setImageResource(R.drawable.ic_volume_off)
                    binding.layoutControl.btnVolumeControl.setImageResource(R.drawable.ic_volume_off)
                }
            } else if (p0?.playState == PlayerAttribute.E_STATE_PLAY_COMPLETED) {
                binding.layoutTimeLine.onNoEvent?.invoke()
                if (isRecord) {
                    startStopRecord()
                }
            }
        }
    }

    override fun onFailed(p0: PlayerAttribute<*>?, p1: Int, errorId: Int) {
        DebugConfig.log(TAG, "onFailed: $errorId")
    }

    override fun onShowRateAndTime(
        p0: PlayerAttribute<*>?,
        p1: Boolean,
        time: String?,
        p3: String?
    ) {
        if (!StringUtils.isStringNULL(time)) {
            lifecycleScope.launchWhenStarted {
                val playCalendar = TimeUtils.getNormalFormatCalender(time)
                if (playCalendar != null) {
                    val currentCalendar = Calendar.getInstance()
                    currentCalendar.time = Date(viewModel.dateLiveData.value ?: 0)
                    val currentDay = currentCalendar.get(Calendar.DAY_OF_MONTH)
                    val videoDay = playCalendar[Calendar.DAY_OF_MONTH]
                    val hour = playCalendar[Calendar.HOUR_OF_DAY]
                    val minute = playCalendar[Calendar.MINUTE]
                    val second = playCalendar[Calendar.SECOND]
                    Log.d(TAG, "DEBUG_TIME1: $hour:$minute:$second")
                    val time = hour * 60 * 60 + minute * 60 + second
                    if (currentDay == videoDay) {
                        val currentindex = binding.layoutTimeLine.getCurrentIndexJF()
                        if (currentindex != -1) {
                            val item = listFile[currentindex]
                            if (item.startTime != currentItemFile?.startTime) {
                                currentItemFile = item
                                binding.layoutTimeLine.setSelectedThumb(currentindex)
                            }
                        }
                        binding.layoutTimeLine.updatevalueJF(time.toLong())
                        mCutPlayTimes = time
                    }
                }
            }
        }
    }

    override fun onVideoBufferEnd(p0: PlayerAttribute<*>?, p1: MsgContent?) {
    }

    override fun onPlayStateClick(p0: View?) {
    }

    override fun searchResult(p0: PlayerAttribute<*>?, data: Any?) {

    }

    fun onSearchResult(data: MutableList<H264_DVR_FILE_DATA>) {
        lifecycleScope.launchWhenStarted {
            if (data.isNotEmpty()) {
                if (recordManager is CloudRecordManager) {
                    listFile.clear()
                    val currentCalendar = Calendar.getInstance()
                    currentCalendar.time = Date(viewModel.dateLiveData.value ?: 0)
                    val currentDay = currentCalendar.get(Calendar.DAY_OF_MONTH)
                    val list = data.toImmutableList().toMutableList().filter {
                        it.st_3_beginTime.st_2_day == currentDay || it.st_4_endTime.st_2_day == currentDay
                    }
                    val listVideo = list.distinctBy { it.longStartTime }.map {
                        if (it.st_3_beginTime.st_2_day != it.st_4_endTime.st_2_day) {
                            if (it.st_3_beginTime.st_2_day != currentDay) {
                                PlaybackFile(
                                    startTime = Date(
                                        viewModel.dateLiveData.value ?: 0
                                    ).convertDateToStartDate(),
                                    endTime = it.st_4_endTime.date.time,
                                    it
                                )
                            } else if (it.st_4_endTime.st_2_day != currentDay) {
                                PlaybackFile(
                                    startTime = it.st_3_beginTime.date.time,
                                    endTime = Date(
                                        viewModel.dateLiveData.value ?: 0
                                    ).convertDateToStartDate() + 86399999,
                                    it
                                )
                            } else {
                                PlaybackFile(
                                    startTime = it.st_3_beginTime.date.time,
                                    endTime = it.st_4_endTime.date.time,
                                    it
                                )
                            }
                        } else {
                            PlaybackFile(
                                startTime = it.st_3_beginTime.date.time,
                                endTime = it.st_4_endTime.date.time,
                                it
                            )
                        }
                    }

                    listFile.addAll(listVideo)
                    if (listFile.isNotEmpty()) {
                        currentItemFile = listFile[0]
                    }
                    val newList = listFile.map {
                        EventHistory(
                            id = it.item.currentPos.toString(),
                            eventID = it.item.currentPos.toString(),
                            eventType = 3,
                            videoID = "",
                            timeStart = it.startTime,
                            timeEnd = it.endTime,
                            type = 0,
                            thumbnailUrl = "",
                            deviceID = ""
                        )
                    }.toMutableList()
                    for (i in listFile.indices) {
                        var path: String = ""
                        context?.let { context ->
                            path = (folderThumbnailInternalStoragePath(context)
                                    + File.separator
                                    + listFile[i].item.st_3_beginTime.date.time + "_" + listFile[i].item.st_4_endTime.date.time + "_thumb.jpg")
                        }

                        val file = File(path)
                        if (file.exists()) {
                            newList[i].thumbnailUrl = file.path
                            Log.d("OnFunSDKResult1 2 cloud", "poss = " + i + " path = " + path)
                        } else {
                            val sTime = FunSDK.ToTimeType(
                                intArrayOf(
                                    listFile[i].item.st_3_beginTime.st_0_year,
                                    listFile[i].item.st_3_beginTime.st_1_month,
                                    listFile[i].item.st_3_beginTime.st_2_day,
                                    listFile[i].item.st_3_beginTime.st_4_hour,
                                    listFile[i].item.st_3_beginTime.st_5_minute,
                                    listFile[i].item.st_3_beginTime.st_6_second
                                )
                            )
                            val eTime = FunSDK.ToTimeType(
                                intArrayOf(
                                    listFile[i].item.st_4_endTime.st_0_year,
                                    listFile[i].item.st_4_endTime.st_1_month,
                                    listFile[i].item.st_4_endTime.st_2_day,
                                    listFile[i].item.st_4_endTime.st_4_hour,
                                    listFile[i].item.st_4_endTime.st_5_minute,
                                    listFile[i].item.st_4_endTime.st_6_second
                                )
                            )
                            mThumbDownloadId = CloudDirectory.DownloadThumbnailByTime(
                                mUserId,
                                devId,
                                path,
                                "Main",
                                "MSG_ALARM_VIDEO_QUERY_REQ",
                                0,
                                sTime,
                                eTime,
                                160,
                                90,
                                SDKCONST.Switch.Close,
                                i
                            )
                            Log.d("OnFunSDKResult1 2 cloud down", "poss = " + i + " path = " + path)
                        }
                    }
                    if (newList.isNullOrEmpty()) {
                        binding.tvMessage.text = getString(com.vht.sdkcore.R.string.no_event_selected_time)
                        binding.tvMessage.visible()
                        enableLayoutControl(false)
                    } else {
                        binding.tvMessage.gone()
                        enableLayoutControl(true)
                    }
                    arguments?.let {
                        if (it.containsKey(Define.BUNDLE_KEY.PARAM_START_TIME) && !it.containsKey(
                                Define.BUNDLE_KEY.PARAM_TIME_PLAN
                            )
                        ) {
                            val startTime = it.getLong(Define.BUNDLE_KEY.PARAM_START_TIME)
                            binding.layoutTimeLine.setList(
                                newList,
                                startTime
                            )
                        } else {
                            binding.layoutTimeLine.setList(newList)
                        }
                    }
                }
            } else {
                binding.tvMessage.text = getString(com.vht.sdkcore.R.string.no_event_selected_time)
                binding.tvMessage.visible()
            }
        }
    }

    private fun enableLayoutControl(isEnable: Boolean = false) {
        lifecycleScope.launchWhenStarted {
            if (isEnable) {
                if (!isScreenOrientationLandscape()) {
                    binding.layoutControl.root.visible()
                }
                binding.layoutRightSidePlayback.btnRecordVideo.visible()
                binding.layoutRightSidePlayback.btnCaptureImage.visible()
                binding.layoutRightSidePlayback.btnVolumeControl.visible()
                binding.layoutRightSidePlayback.btnRate.visible()
                binding.layoutBtnControl.btnRecord.isEnabled = true
                binding.layoutBtnControl.btnSnapShot.isEnabled = true

                if (isRecord) {
                    binding.layoutBtnControl.btnRecord.setBackgroundResource(R.drawable.icon_bgr_button_actived)
                    binding.layoutBtnControl.btnRecord.setImageResource(R.drawable.ic_stop_record)
                } else {
                    binding.layoutBtnControl.btnRecord.setImageResource(R.drawable.ic_record_start)
                    binding.layoutBtnControl.btnRecord.setBackgroundResource(R.drawable.btn_control_camera_bg)
                }
                binding.layoutBtnControl.tvRecord.isEnabled = true

                binding.layoutBtnControl.btnSnapShot.setImageResource(R.drawable.ic_camera_capture_image)
                binding.layoutBtnControl.btnSnapShot.setBackgroundResource(R.drawable.btn_control_camera_bg)
                binding.layoutBtnControl.tvSnapshot.isEnabled = true

            } else {
                if (!isScreenOrientationLandscape()) {
                    binding.layoutControl.root.gone()
                }
                binding.layoutRightSidePlayback.btnRecordVideo.gone()
                binding.layoutRightSidePlayback.btnCaptureImage.gone()
                binding.layoutRightSidePlayback.btnVolumeControl.gone()
                binding.layoutRightSidePlayback.btnRate.gone()
                binding.layoutBtnControl.btnRecord.isEnabled = false
                binding.layoutBtnControl.btnSnapShot.isEnabled = false

                binding.layoutBtnControl.btnRecord.setImageResource(R.drawable.ic_record_start_disable)
                binding.layoutBtnControl.btnRecord.setBackgroundResource(R.drawable.btn_control_disable_camera_bg)
                binding.layoutBtnControl.tvRecord.isEnabled = false

                binding.layoutBtnControl.btnSnapShot.setImageResource(R.drawable.ic_camera_capture_image_disable)
                binding.layoutBtnControl.btnSnapShot.setBackgroundResource(R.drawable.btn_control_disable_camera_bg)
                binding.layoutBtnControl.tvSnapshot.isEnabled = false
            }
        }
    }

    override fun onDownload(p0: DownloadInfo?) {
        DebugConfig.log(TAG, "onDownload: state = ${p0?.downloadState}")
        when (p0?.downloadState) {
            DownloadManager.DOWNLOAD_STATE_FAILED -> {
                showHideLoading(false)
            }

            DownloadManager.DOWNLOAD_STATE_START -> {
                showHideLoading(true)
                toastMessage(getString(com.vht.sdkcore.R.string.downloading))
            }

            DownloadManager.DOWNLOAD_STATE_COMPLETE_ALL -> {
                showHideLoading(false)
                showCustomNotificationDialog(
                    title = getString(com.vht.sdkcore.R.string.download_video_success),
                    type = DialogType.CONFIRM,
                    message = com.vht.sdkcore.R.string.download_video_message,
                    titleBtnConfirm = com.vht.sdkcore.R.string.string_save,
                    negativeTitle = com.vht.sdkcore.R.string.string_cancel
                ) {
                    Utils.saveVideoToGallery(requireContext(), p0.saveFileName)
//                    binding.layoutTimeLine.updateThumbnailURL()

                }
            }
        }
    }

    override fun OnFunSDKResult(msg: Message?, ex: MsgContent?): Int {
        when (msg!!.what) {
            EUIMSG.DOWN_RECODE_BPIC_START -> {
                val message = Message.obtain()
                message.what = 1
                message.arg1 = ex!!.seq
//                mHandler.sendMessage(message)
            }

            EUIMSG.MC_DownloadMediaThumbnail, EUIMSG.DOWN_RECODE_BPIC_FILE -> if (mThumbDownloadId != 0) {
                if (ex!!.seq < listFile.size) {
                    lifecycleScope.launchWhenStarted {
                        val info: PlaybackFile = listFile.get(ex.seq)
                        info.item.downloadStatus = 3
                        var path: String = ""
                        context?.let { context ->
                            path = (folderThumbnailInternalStoragePath(context)
                                    + File.separator
                                    + listFile[ex.seq].startTime + "_" + listFile[ex.seq].endTime + "_thumb.jpg")
                        }
                        Log.d("OnFunSDKResult1 cloud", "pos = " + ex.seq + " path = " + path)
                        binding.layoutTimeLine.updateThumbnailURL(ex.seq, path)
                    }
                }
            }

            EUIMSG.DOWN_RECODE_BPIC_COMPLETE -> {}
            else -> {}
        }
        return 0
    }

    override fun onStart() {
        super.onStart()
        context?.registerReceiver(
            receiverNetwork,
            IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        )
        NetworkChangeReceiver.connectivityReceiverListener = this
    }

    fun forceStopRecord() {
        if (isRecord) {
            val time = SystemClock.elapsedRealtime() - binding.recordTimer.base
            isRecord = false
            binding.layoutTimeLine.enableTimeline(true)
            setUIRecording(false)
            recordManager.stopRecord()
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
                    showCustomNotificationDialog(
                        getString(com.vht.sdkcore.R.string.recording_success),
                        DialogType.CONFIRM,
                        com.vht.sdkcore.R.string.save_video_success_content,
                        com.vht.sdkcore.R.string.dialog_btn_save,
                        com.vht.sdkcore.R.string.dialog_btn_cancel,
                    ) {
                        viewModel.saveVideoToGallery(fileVideo.path, devId ?: "")
                        context?.toast(getString(com.vht.sdkcore.R.string.string_recording_success))
                    }
                } else {
                    fileVideo.delete()
                }
            }
        }
    }

    override fun onNetworkConnectionChanged(isConnected: Boolean) {
        if (!isConnected && isRecord) {
            binding.tvMessage.gone()
            showErrorLiveStream()

            forceStopRecord()
            showCustomNotificationDialog(
                "Đang mất kết nối, bạn vui lòng kiểm tra lại kết nối và thử lại",
                DialogType.ERROR,
                titleBtnConfirm = com.vht.sdkcore.R.string.notification_understand,
            ) {}
        } else if (!isConnected) {
            binding.tvMessage.gone()
            showErrorLiveStream()

            showCustomNotificationDialog(
                "Đang mất kết nối, bạn vui lòng kiểm tra lại kết nối và thử lại",
                DialogType.ERROR,
                titleBtnConfirm = com.vht.sdkcore.R.string.notification_understand,
            ) {}
        }
    }

    private fun showErrorLiveStream() {
        binding.ivRetryStreaming.visible()
        binding.tvShowStatusConnect.visible()
        binding.fragmentCameraStreamPgLoading.gone()

        enableLayoutControl(false)
        recordManager.stopPlay()
        binding.layoutPlayWnd.gone()

    }

    private fun hideErrorLiveStream() {
        binding.ivRetryStreaming.gone()
        binding.tvShowStatusConnect.gone()
        binding.fragmentCameraStreamPgLoading.gone()
    }

}

