package com.viettel.vht.sdk.ui.cardsetting

import android.annotation.SuppressLint
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.basic.G
import com.lib.SDKCONST.SDK_FileSystemDriverTypes
import com.lib.sdk.bean.StorageInfoBean
import com.vht.sdkcore.base.BaseFragment
import com.vht.sdkcore.utils.Define
import com.vht.sdkcore.utils.dialog.CommonAlertDialogNotification
import com.vht.sdkcore.utils.dialog.DialogType
import com.viettel.vht.sdk.R
import com.viettel.vht.sdk.databinding.FragmentCardSettingCamJfBinding
import com.viettel.vht.sdk.navigation.AppNavigation
import com.viettel.vht.sdk.utils.gone
import com.viettel.vht.sdk.utils.visible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject


@AndroidEntryPoint
class CardSettingJFFragment :
    BaseFragment<FragmentCardSettingCamJfBinding, CardSettingJFViewModel>() {

    private var jobFormatting: Job? = null

    @Inject
    lateinit var appNavigation: AppNavigation

    private val viewModel: CardSettingJFViewModel by viewModels()

    override val layoutId = R.layout.fragment_card_setting_cam_jf

    override fun getVM(): CardSettingJFViewModel = viewModel

    override fun initView() {
        super.initView()
        arguments?.let {
            if (it.containsKey(Define.BUNDLE_KEY.PARAM_ID)) {
                viewModel.devId = it.getString(Define.BUNDLE_KEY.PARAM_ID) ?: ""
            }
            viewModel.getConfig()
        }
    }

    override fun setOnClick() {
        super.setOnClick()

        binding.toolBar.setOnLeftClickListener {
            appNavigation.navigateUp()
        }

        binding.cbRecordModeAlways.setOnCheckedChangeListener { buttonView, isChecked ->
            if (buttonView.isPressed) {
                if (isChecked) {
                    viewModel.saveRecordModeSet(CardSettingJFViewModel.RecordMode.Normal)
                    binding.cbRecordModeMotion.isChecked = false
                } else {
                    binding.cbRecordModeAlways.isChecked = true
                }
            }
        }

        binding.cbRecordModeMotion.setOnCheckedChangeListener { buttonView, isChecked ->
            if (buttonView.isPressed) {
                if (isChecked) {
                    viewModel.saveRecordModeSet(CardSettingJFViewModel.RecordMode.Alarm)
                    binding.cbRecordModeAlways.isChecked = false
                } else {
                    binding.cbRecordModeMotion.isChecked = true
                }
            }
        }

        binding.btFormat.setOnClickListener {
            CommonAlertDialogNotification.getInstanceCommonAlertdialog(requireContext())
                .showDialog()
                .setDialogTitleWithString("Xác nhận định dạng thẻ nhớ")
                .setContent("Sau khi định dạng, toàn bộ dữ liệu sẽ bị xóa khỏi thẻ nhớ của bạn.")
                .setTextPositiveButtonWithString("OK")
                .setTextNegativeButtonWithString("ĐÓNG")
                .showCenterImage(DialogType.CONFIRM)
                .setOnNegativePressed { dialog ->
                    dialog.dismiss()
                }
                .setOnPositivePressed { dialog ->
                    dialog.dismiss()
                    viewModel.formatStorage()
                }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun bindingStateView() {
        super.bindingStateView()
        viewModel.storageInfoBeanListLiveData.observe(viewLifecycleOwner) {
            if (it.isNullOrEmpty()) {
                binding.apply {
                    tvTotalData.text = "0 GB"
                    tvTotalUsed.text = "0 GB"
                    tvTotalAvail.text = "0 GB"
                    tvTimeOldSave.text = "--:--:-- --/--/----"
                    containerRecordMode.alpha = 0.5f
                    tvError.visible()
                }
            } else {
                showStorageInfo(it)
                binding.apply {
                    containerRecordMode.alpha = 1f
                    tvError.gone()
                }
            }
        }
        viewModel.formatState.observe(viewLifecycleOwner) {
            when (it) {
                CardSettingJFViewModel.FORMAT_START -> {
                    jobFormatting?.cancel()
                    jobFormatting = lifecycleScope.launchWhenStarted {
                        binding.loadingFormatting.startAnimation(
                            RotateAnimation(
                                0f, 360f,
                                Animation.RELATIVE_TO_SELF, 0.5f,
                                Animation.RELATIVE_TO_SELF, 0.5f
                            ).apply {
                                duration = 1200
                                repeatCount = Animation.INFINITE
                            }
                        )
                        binding.containerInfo.gone()
                        binding.containerRecordMode.gone()
                        binding.containerFormatting.visible()
                        withTimeout(120000) {
                            repeat(120) {
                                withContext(Dispatchers.Main) {
                                    binding.tvProgress.text = String.format(
                                        "%s:%s",
                                        ((120 - it) / 60).toString(),
                                        if ((120 - it) % 60 < 10) "0${(120 - it) % 60}" else "${(120 - it) % 60}"
                                    )
                                }
                                delay(1000)
                            }
                        }
                    }
                }

                CardSettingJFViewModel.FORMAT_SUCCESS -> {
                    jobFormatting?.cancel()
                    binding.loadingFormatting.clearAnimation()
                    binding.containerInfo.visible()
                    binding.containerRecordMode.visible()
                    binding.containerFormatting.gone()
                    CommonAlertDialogNotification.getInstanceCommonAlertdialog(requireContext())
                        .showDialog()
                        .showCenterImage(DialogType.SUCCESS)
                        .setDialogTitleWithString("Định dạng thẻ nhớ thành công")
                        .hideOptionButton()
                    lifecycleScope.launchWhenStarted {
                        delay(3000)
                        CommonAlertDialogNotification
                            .getInstanceCommonAlertdialog(requireContext())
                            .dismiss()
                    }
                }

                CardSettingJFViewModel.FORMAT_FAIL -> {
                    jobFormatting?.cancel()
                    binding.loadingFormatting.clearAnimation()
                    binding.containerInfo.visible()
                    binding.containerRecordMode.visible()
                    binding.containerFormatting.gone()
                    CommonAlertDialogNotification.getInstanceCommonAlertdialog(requireContext())
                        .showDialog()
                        .setDialogTitleWithString("Định dạng thất bại")
                        .setTextPositiveButtonWithString("THỬ LẠI")
                        .setTextNegativeButtonWithString("THOÁT")
                        .showCenterImage(DialogType.ERROR)
                        .setOnNegativePressed { dialog ->
                            dialog.dismiss()
                        }
                        .setOnPositivePressed { dialog ->
                            dialog.dismiss()
                            viewModel.formatStorage()
                        }
                        .showNegativeAndPositiveButton()
                }
            }
        }
        viewModel.recordModeState.observe(viewLifecycleOwner) {
            binding.apply {
                when (it) {
                    is CardSettingJFViewModel.RecordMode.Normal -> {
                        cbRecordModeAlways.isChecked = true
                        cbRecordModeMotion.isChecked = false
                    }

                    CardSettingJFViewModel.RecordMode.Alarm -> {
                        cbRecordModeAlways.isChecked = false
                        cbRecordModeMotion.isChecked = true
                    }

                    CardSettingJFViewModel.RecordMode.Closed -> {
                        cbRecordModeAlways.isChecked = false
                        cbRecordModeMotion.isChecked = false
                    }
                }
            }
        }
    }

    private fun showStorageInfo(storageInfoBeans: List<StorageInfoBean>) {
        var sumRemainSize: Long = 0
        var sumTotalSize: Long = 0
        var videoSize: Long = 0
        var picSize: Long = 0
        var oldStartTime = ""
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val sdfShow = SimpleDateFormat("HH:mm:ss dd/MM/yyyy", Locale.getDefault())
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
                    videoSize += totalSize
                } else if (partition.DirverType == SDK_FileSystemDriverTypes.SDK_DRIVER_SNAPSHOT.toLong()) {
                    sumRemainSize += remainSize
                    sumTotalSize += totalSize
                    picSize += totalSize
                }
                try {
                    if (partition.OldStartTime != "2000-00-00 00:00:00") {
                        if (oldStartTime.isEmpty()) {
                            oldStartTime = partition.OldStartTime
                        } else {
                            if (
                                sdf.parse(oldStartTime)
                                    ?.after(sdf.parse(partition.OldStartTime)) == true
                            ) {
                                oldStartTime = partition.OldStartTime
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        binding.tvTotalData.text = String.format("%.1f GB", sumTotalSize / 1024.0)
        binding.tvTotalUsed.text = String.format("%.1f GB", (sumTotalSize - sumRemainSize) / 1024.0)
        binding.tvTotalAvail.text = String.format("%.1f GB", sumRemainSize / 1024.0)
        try {
            binding.tvTimeOldSave.text =
                sdfShow.format(sdf.parse(oldStartTime) ?: "") ?: "--:--:-- --/--/----"
        } catch (e: Exception) {
            e.printStackTrace()
            binding.tvTimeOldSave.text = "--:--:-- --/--/----"
        }
    }
}
