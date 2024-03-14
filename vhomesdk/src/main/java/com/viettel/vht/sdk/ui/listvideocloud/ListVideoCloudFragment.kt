package com.viettel.vht.sdk.ui.listvideocloud

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.os.Bundle
import android.os.Message
import android.util.Log
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.lib.*
import com.lib.Mps.MpsClient
import com.lib.sdk.struct.H264_DVR_FILE_DATA
import com.manager.db.DownloadInfo
import com.manager.device.DeviceManager
import com.manager.device.media.MediaManager
import com.manager.device.media.download.DownloadManager
import com.manager.device.media.playback.RecordManager
import com.vht.sdkcore.base.BaseFragment
import com.vht.sdkcore.pref.RxPreferences
import com.vht.sdkcore.utils.AppLog
import com.vht.sdkcore.utils.Define
import com.vht.sdkcore.utils.Utils
import com.vht.sdkcore.utils.dialog.CommonAlertDialogNotification
import com.vht.sdkcore.utils.dialog.DialogType
import com.vht.sdkcore.utils.getColorCompat
import com.vht.sdkcore.utils.toastMessage
import com.viettel.vht.sdk.R
import com.viettel.vht.sdk.databinding.FragmentListVideoCloudBinding
import com.viettel.vht.sdk.model.camera_cloud.CloudStorageRegistered
import com.viettel.vht.sdk.navigation.AppNavigation
import com.viettel.vht.sdk.utils.DebugConfig
import com.viettel.vht.sdk.utils.gone
import com.viettel.vht.sdk.utils.showCustomNotificationDialog
import com.viettel.vht.sdk.utils.visible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

const val EVENT_TYPE = 0
const val PLAYBACK_TYPE = 1
const val CLOUD_TYPE = 2

@AndroidEntryPoint
class ListVideoCloudFragment :
    BaseFragment<FragmentListVideoCloudBinding, ListVideoCloudViewModel>(),
    DownloadManager.OnDownloadListener, IFunSDKResult {

    @Inject
    lateinit var rxPreferences: RxPreferences

    @Inject
    lateinit var appNavigation: AppNavigation

    override val layoutId: Int
        get() = R.layout.fragment_list_video_cloud

    private val viewModel: ListVideoCloudViewModel by viewModels()
    private val videoDownloadViewModel: VideoDownloadViewModel by activityViewModels()

    override fun getVM(): ListVideoCloudViewModel = viewModel

    private lateinit var recordManager: RecordManager
    private lateinit var downloadManager: DownloadManager

    private var mUserId = 0
    private var mThumbDownloadId = 0

    private val mAlarmIds: ArrayList<String> = arrayListOf()

    private val deviceId: String
        get() = requireArguments().getString(Define.BUNDLE_KEY.PARAM_DEVICE_ID) ?: ""
    private val typeScreen: Int
        get() = requireArguments().getInt(Define.BUNDLE_KEY.PARAM_TYPE)

    private val devName: String get() =  requireArguments().getString(Define.BUNDLE_KEY.PARAM_NAME)?:""

    private var isRegistedCloud: Boolean? = null
    private val isShareCameraJF: Boolean
        get() = requireArguments().getBoolean(Define.BUNDLE_KEY.PARAM_IS_SHARE_CAMERA_JF, false)

    private val adapterVideo: ItemVideoCloudAdapter by lazy {
        ItemVideoCloudAdapter(binding.rcvItem,requireContext(), deviceId)
    }

    override fun initView() {
        super.initView()
        if (typeScreen == EVENT_TYPE) {
            binding.toolbar.setStringTitle("vCloud")
        } else {
            binding.toolbar.setStringTitle("Lưu trữ Cloud")
        }
        mUserId = FunSDK.RegUser(this)
        arguments?.let {
            if (it.containsKey(Define.BUNDLE_KEY.PARAM_TIME)) {
                viewModel.date.value =
                    it.getLong(Define.BUNDLE_KEY.PARAM_TIME, System.currentTimeMillis())
                it.remove(Define.BUNDLE_KEY.PARAM_TIME)
            }
        }
        initRcv()
        initRecordManager()
        initObserver()
        viewModel.getListCloudStorageRegistered(deviceId)
        viewModel.cloudRegistered.observe(viewLifecycleOwner) {
            setUpCloudStatus(it)

        }
    }

    private fun searchVideo() {
        viewModel.isLoading.value = true
        if (isRegistedCloud != null) {
            if (isRegistedCloud == true) {
                viewModel.searchVideo(recordManager){
                    onSearchResult(it)
                }
                binding.tvRegisterCloud.gone()
                binding.imvRegister.gone()
                binding.btnRegister.gone()
            } else {
                viewModel.isLoading.value = false
                showRegisterCloud()
            }
        } else {
            viewModel.isLoading.value = false
        }
    }

    private fun setUpCloudStatus(listCloudPackage: List<CloudStorageRegistered>?) {
        if (!listCloudPackage.isNullOrEmpty()) {
            isRegistedCloud = true
            if (isShareCameraJF) {
                binding.layoutExpries.gone()
            } else {
                binding.layoutExpries.visible()
            }
            binding.layoutExpries.setOnClickRenew {
                val bundle = Bundle().apply {
                    putString(Define.BUNDLE_KEY.PARAM_ID, arguments?.getString(Define.BUNDLE_KEY.PARAM_ID)?:"")
                    putString(Define.BUNDLE_KEY.PARAM_NAME, deviceId)
                    putString(Define.BUNDLE_KEY.PARAM_DEVICE_SERIAL, deviceId)
                    putString(Define.BUNDLE_KEY.PARAM_DEVICE_TYPE, Define.TYPE_DEVICE.CAMERA_JF)
                    putString(Define.BUNDLE_KEY.PARAM_NAME,devName)
                }
                appNavigation.openCloudStorageCamera(bundle)
            }
            listCloudPackage.forEach {
                if (it.serviceStatus == 1) {
                    binding.layoutExpries.gone()
                    return@forEach
                }
            }
            searchVideo()
        } else {
            isRegistedCloud = false
            binding.imvNodata.gone()
            binding.tvNodata.gone()
            binding.rcvItem.gone()
            showRegisterCloud()
        }
    }

    private fun showRegisterCloud() {
        if (!isShareCameraJF) {
            binding.tvRegisterCloud.visible()
            binding.imvRegister.visible()
            binding.btnRegister.visible()
            binding.tvGuide.gone()
        } else {
            binding.tvRegisterCloud.gone()
            binding.imvRegister.gone()
            binding.btnRegister.gone()
            binding.tvGuide.gone()
        }
    }

    private fun initRecordManager() {
        DebugConfig.log(TAG, "initRecordManager")
        downloadManager = DownloadManager.getInstance(this)
        DebugConfig.log(TAG, "devId = $deviceId")
        recordManager =
            DeviceManager.getInstance()
                .createRecordPlayer(
                    binding.swipeLayout, deviceId,
                    MediaManager.PLAY_CLOUD_PLAYBACK
                )
        recordManager.setChnId(0)
    }

    private fun initRcv() {
        adapterVideo.actionCheckedMode = {
            binding.tvGuide.gone()
            binding.tvSelectAll.visible()
            binding.tvCancel.visible()
            binding.tvCount.visible()
            binding.tvDelete.visible()
            binding.clDownload.visible()
            binding.tvCount.text =
                getString(com.vht.sdkcore.R.string.count_selected, adapterVideo.getListChecked().size.toString())
            adapterVideo.changeModeChecked()
        }
        adapterVideo.onItemCheckedChange = {
            binding.tvSelectAll.isChecked = adapterVideo.isSelectAll()
            binding.tvCount.text =
                getString(com.vht.sdkcore.R.string.count_selected, adapterVideo.getListChecked().size.toString())
            if (adapterVideo.getListChecked().isEmpty()) {
                binding.textBtnDelete.setTextColor(requireContext().getColorCompat(R.color.color_4E4E4E))
                binding.imgBtnDelete.setColorFilter(requireContext().getColorCompat(R.color.color_4E4E4E))
                binding.textBtnDownload.setTextColor(requireContext().getColorCompat(R.color.color_4E4E4E))
                binding.imgBtnDownload.setColorFilter(requireContext().getColorCompat(R.color.color_4E4E4E))
            } else {
                binding.textBtnDelete.setTextColor(requireContext().getColorCompat(R.color.color_F8214B))
                binding.imgBtnDelete.setColorFilter(requireContext().getColorCompat(R.color.color_F8214B))
                binding.textBtnDownload.setTextColor(requireContext().getColorCompat(R.color.color_F8214B))
                binding.imgBtnDownload.setColorFilter(requireContext().getColorCompat(R.color.color_F8214B))
            }
        }
        adapterVideo.onItemClick = {
            lifecycleScope.launchWhenStarted {
                arguments?.remove(Define.BUNDLE_KEY.PARAM_TIME)
                val bundle = Bundle()
                val hasRegisterCloud =
                    viewModel.checkCloudStorageRegistered(deviceId) ?: false
                bundle.putBoolean(
                    Define.BUNDLE_KEY.PARAM_VALUE,
                    hasRegisterCloud
                )
                bundle.putString(Define.BUNDLE_KEY.PARAM_DEVICE_SERIAL, deviceId)
                bundle.putString(Define.BUNDLE_KEY.PARAM_ID, arguments?.getString(Define.BUNDLE_KEY.PARAM_ID)?:"")
                bundle.putLong(Define.BUNDLE_KEY.PARAM_START_TIME, it.timeStart)
                bundle.putString(Define.BUNDLE_KEY.PARAM_NAME,devName)
                appNavigation.openQuickPlaybackJFFragment(bundle)
            }
        }
//        binding.rcvItem.adapter = adapter
//        binding.swipeLayout.setOnRefreshListener {
//            searchVideo()
//            binding.swipeLayout.isRefreshing = false
//        }
        val layoutManager = GridLayoutManager(
            requireContext(),
            3,
            LinearLayoutManager.VERTICAL,
            false
        )
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int) =
                when (adapterVideo.getItemViewType(position)) {
                    ItemVideoCloudAdapter.TYPE_VIDEO -> 1
                    ItemVideoCloudAdapter.TYPE_TITLE -> 3
                    else -> 1
                }
        }
        binding.rcvItem.layoutManager = layoutManager
        binding.rcvItem.adapter = adapterVideo
    }

    private fun initObserver() {
        viewModel.date.observe(viewLifecycleOwner) { longDate ->
            binding.tvSelectAll.gone()
            binding.tvCancel.gone()
            binding.tvCount.gone()
            binding.tvDelete.gone()
            binding.clDownload.gone()
            adapterVideo.doUnchecked()
            binding.shortcutDate.changeDate(Date(longDate))
            initRecordManager()
            searchVideo()
        }
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            showHideLoading(isLoading)
        }
        videoDownloadViewModel.isDownloading.observe(viewLifecycleOwner) {
            binding.clDownloadInfo.isVisible = it
        }
        videoDownloadViewModel.downloadProgress.observe(viewLifecycleOwner) {
            binding.tvDownloadInfo.text = it.toString()
        }

        viewModel.listDateCloudDayAlso.observe(viewLifecycleOwner){
           it?.let {
               binding.shortcutDate.listDateCloudDayAlso.clear()
               binding.shortcutDate.listDateCloudDayAlso.addAll(it)
               binding.shortcutDate.changeDate(Date(viewModel.date.value?:0L))
           }
        }
    }

    @SuppressLint("SourceLockedOrientationActivity")
    override fun setOnClick() {
        super.setOnClick()

        binding.toolbar.setOnLeftClickListener {
            if (isDoubleClick) return@setOnLeftClickListener
            findNavController().navigateUp()
        }
        binding.btnRegister.setOnClickListener {
            val bundle = Bundle().apply {
                putString(Define.BUNDLE_KEY.PARAM_ID, arguments?.getString(Define.BUNDLE_KEY.PARAM_ID)?:"")
                putString(Define.BUNDLE_KEY.PARAM_NAME, deviceId)
                putString(Define.BUNDLE_KEY.PARAM_DEVICE_SERIAL, deviceId)
                putString(Define.BUNDLE_KEY.PARAM_DEVICE_TYPE, Define.TYPE_DEVICE.CAMERA_JF)
                putString(Define.BUNDLE_KEY.PARAM_NAME,devName)
            }
            appNavigation.openCloudStorageCamera(bundle)
        }
        binding.shortcutDate.onSelectDate = {
            viewModel.date.value = it.time
            viewModel.sendLogForCloudPackage(
                screenID = AppLog.ScreenID.CLOUD_LIST,
                actionID = AppLog.LogPlayBackCloud.TRACKING_SELECT_DATE_CLOUD.actionID
            )
            adapterVideo.submitList(mutableListOf())
        }
        binding.shortcutDate.onSelectShowDialogDate = {
         //   showDatePickerDialog()
            BottomSheetDatePickerListVideoJFFragment().show( childFragmentManager,
                "BottomSheetDatePickerListVideoJFFragment")
        }
        binding.tvSelectAll.setOnCheckedChangeListener { checkbox, isChecked ->
            if (checkbox.isPressed) {
                if (isChecked) {
                    adapterVideo.selectAll()
                } else {
                    adapterVideo.unSelectAll()
                }
            }
        }
        binding.tvCancel.setOnClickListener {
            binding.shortcutDate.visible()
            binding.tvGuide.visible()
            binding.tvSelectAll.gone()
            binding.tvCancel.gone()
            binding.tvCount.gone()
            binding.tvDelete.gone()
            binding.clDownload.gone()
            adapterVideo.doUnchecked()
        }
        binding.tvDelete.setOnClickListener {
            if (adapterVideo.getListChecked().isNotEmpty()) {
                deleteEvent()
            }
        }
        binding.clDownload.setOnClickListener {
            if (adapterVideo.getListChecked().isNotEmpty()) {
                val current = videoDownloadViewModel.downloadProgress.value ?: 0
                if ((adapterVideo.getListChecked().size + current) > 10) {
                    showCustomNotificationDialog(
                        title = "Bạn chỉ có thể tải tối đa 10 video cùng lúc",
                        type = DialogType.ERROR,
                        titleBtnConfirm = com.vht.sdkcore.R.string.btn_ok
                    ) {}
                } else {
                    CommonAlertDialogNotification.getInstanceCommonAlertdialog(requireContext())
                        .showDialog()
                        .setTextNegativeButtonWithString("KHÔNG")
                        .setTextPositiveButtonWithString("CÓ")
                        .setContent("Nếu chọn không, video sẽ được lưu trong bộ sưu tập của camera.")
                        .showCenterImage(DialogType.CONFIRM)
                        .setDialogTitleWithString("Bạn có muốn lưu video vào thư viện của điện thoại?")
                        .setOnNegativePressed {
                            it.dismiss()
                            videoDownloadViewModel.startDownloadVideo(
                                adapterVideo.getListChecked(),
                                deviceId,
                                false
                            )
                            binding.tvCancel.performClick()
                        }
                        .setOnPositivePressed {
                            it.dismiss()
                            videoDownloadViewModel.startDownloadVideo(
                                adapterVideo.getListChecked(),
                                deviceId,
                                true
                            )
                            binding.tvCancel.performClick()
                        }
                }
            }
        }
    }

    private fun deleteEvent() {
        showCustomNotificationDialog(
            title = getString(com.vht.sdkcore.R.string.confirm_remove),
            type = DialogType.CONFIRM,
            message = com.vht.sdkcore.R.string.confirm_remove_event,
            titleBtnConfirm = com.vht.sdkcore.R.string.regiter_dialog_button_done,
            negativeTitle = com.vht.sdkcore.R.string.dialog_button_cancel
        ) {
            lifecycleScope.launchWhenStarted {
//                val listId = adapter.getListChecked().map { it.id }
//                val selectedList = adapter.getListChecked()


                var alarmIds = StringBuffer()

                for (i in adapterVideo.getListChecked().indices) {
                    val data: ItemVideo = adapterVideo.getListChecked()[i]
                    if (i > 0 && i % 50 == 0) {
                        mAlarmIds.add(alarmIds.toString())
                        alarmIds = StringBuffer()
                    }
                    alarmIds.append(data.fileName)
                    alarmIds.append(";")
                }

                if (alarmIds.isNotEmpty()) {
                    MpsClient.DeleteMediaFile(
                        mUserId,
                        deviceId,
                        "VIDEO",
                        alarmIds.toString(),
                        -1
                    )
                    showHideLoading(true)
                }

            }
        }
    }

    private fun showDatePickerDialog() {
        val onDateSetListener =
            DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                val mCalendar = Calendar.getInstance()
                mCalendar.set(Calendar.YEAR, year)
                mCalendar.set(Calendar.MONTH, monthOfYear)
                mCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                viewModel.date.value = mCalendar.timeInMillis
                binding.shortcutDate.changeDate(Date(mCalendar.timeInMillis))
                adapterVideo.submitList(mutableListOf())
            }

        val mCalendar = Calendar.getInstance().apply {
            time = Date(viewModel.date.value!!)
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
                }
            }
        }
    }


    @SuppressLint("SimpleDateFormat")
    fun onSearchResult(data:MutableList<H264_DVR_FILE_DATA>) {
        lifecycleScope.launchWhenStarted {
            viewModel.isLoading.value = false
            if (data.isNotEmpty()) {
                val list = withContext(Dispatchers.IO) {
                    val currentCalendar = Calendar.getInstance()
                    currentCalendar.time = Date(viewModel.date.value ?: 0)
                    val currentDay = currentCalendar.get(Calendar.DAY_OF_MONTH)
                    DebugConfig.logd(TAG, "searchResult: listFile size = ${data.size}")
                    data.removeAll {
                        it.st_3_beginTime.st_2_day != currentDay
                    }
                    val list = data.distinctBy { it.longStartTime }
                    DebugConfig.log(
                        TAG, "Day = $currentDay --- list = ${
                            data.map {
                                it.st_3_beginTime.st_2_day
                            }
                        }"
                    )
                    val newList = list.map {
                        ItemVideo(
                            id = it.currentPos.toString(),
                            timeStart = it.st_3_beginTime.date.time,
                            timeEnd = it.st_4_endTime.date.time,
                            thumbnail = "",
                            isSelected = false,
                            isModeChecked = false,
                            fileName = it.getFileName() ?: "",
                            timeTitle = "",
                            video = it
                        )
                    }.groupBy {
                        SimpleDateFormat("HH").format(Date(it.timeStart))
                    }.map {
                        GroupVideo(it.key, it.value.toMutableList())
                    }
                    newList
                }
                val result = mutableListOf<ItemVideo>()
                list.forEach {
                    result.add(
                        ItemVideo(
                            id = "",
                            timeStart = 0,
                            timeEnd = 0,
                            thumbnail = "",
                            isSelected = false,
                            isModeChecked = false,
                            fileName = "",
                            timeTitle = it.time,
                            type = ItemVideoCloudAdapter.TYPE_TITLE
                        )
                    )
                    result.addAll(it.listItem)
                }
                DebugConfig.logd(TAG, "searchResult: result size = $result")
                adapterVideo.submitList(result)
                if (list.isEmpty()) {
                    binding.tvNodata.visible()
                    binding.imvNodata.visible()
                    binding.tvGuide.gone()
                    binding.rcvItem.gone()
                } else {
                    binding.tvNodata.gone()
                    binding.imvNodata.gone()
                    binding.tvGuide.visible()
                    binding.rcvItem.visible()
                }
            } else {
                binding.tvNodata.visible()
                binding.imvNodata.visible()
                binding.tvGuide.gone()
                binding.rcvItem.gone()
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
//                if (ex!!.seq < listFile.size) {
//                    val info: H264_DVR_FILE_DATA = listFile[ex.seq]
//                    info.downloadStatus = 3
//                    var path: String = ""
//                    context?.let { context ->
//                        path = (Utils.folderThumbnailInternalStoragePath(context)
//                                + File.separator
//                                + listFile[ex.seq].st_3_beginTime.date.time + "_" + listFile[ex.seq].st_4_endTime.date.time + "_thumb.jpg")
//                    }
//                    Log.d("OnFunSDKResult1 list", "pos = " + ex.seq + " path = " + path)
//                    adapter.setThumbnail(info.st_3_beginTime.date.time, path)
//                }
            }

            EUIMSG.DOWN_RECODE_BPIC_COMPLETE -> {}
            EUIMSG.MC_DeleteAlarm -> {
                showHideLoading(false)
                if (msg.arg1 < 0) {
                    mAlarmIds.clear()
                    showCustomNotificationDialog(
                        getString(com.vht.sdkcore.R.string.delete_event_failed),
                        DialogType.ERROR,
                        titleBtnConfirm = com.vht.sdkcore.R.string.text_close,
                    ) {
                        binding.tvCancel.performClick()
                    }
                } else {
                    if (mAlarmIds.isNotEmpty()) {
                        MpsClient.DeleteMediaFile(mUserId, deviceId, "VIDEO", mAlarmIds[0], -1)
                        mAlarmIds.removeAt(0)
                    } else {
                        toastMessage(getString(com.vht.sdkcore.R.string.delete_event_success))
                        adapterVideo.deleteListChecked()
                        binding.tvCancel.performClick()
                    }
                }
            }

            EUIMSG.SYS_GET_DEV_CAPABILITY_SET -> {

            }

            EUIMSG.SYS_BATCH_GET_DEV_CAPABILITY_SET -> {

            }

            else -> {}
        }
        return 0
    }

    companion object {
        private val TAG: String = ListVideoCloudFragment::class.java.simpleName
    }
}