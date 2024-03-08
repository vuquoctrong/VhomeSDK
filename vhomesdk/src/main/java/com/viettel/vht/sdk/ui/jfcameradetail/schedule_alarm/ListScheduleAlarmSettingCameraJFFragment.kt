package com.viettel.vht.sdk.ui.jfcameradetail.schedule_alarm

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.vht.sdkcore.base.BaseFragment
import com.vht.sdkcore.utils.Define
import com.vht.sdkcore.utils.dialog.DialogType
import com.vht.sdkcore.utils.getColorCompat
import com.vht.sdkcore.utils.isNetworkAvailable
import com.viettel.vht.sdk.R
import com.viettel.vht.sdk.databinding.FragmentListScheduleAlarmCameraJfBinding
import com.viettel.vht.sdk.navigation.AppNavigation
import com.viettel.vht.sdk.ui.jfcameradetail.schedule_alarm.AddScheduleAlarmSettingCameraJFFragment.Companion.ARG_ID_SCHEDULE_PARAM
import com.viettel.vht.sdk.ui.jfcameradetail.schedule_alarm.adapter.ScheduleAlarmCameraJFAdapter
import com.viettel.vht.sdk.ui.jfcameradetail.schedule_alarm.model.TimeItem.Companion.TOTAL_SCHEDULE_ALARM
import com.viettel.vht.sdk.utils.DebugConfig
import com.viettel.vht.sdk.utils.showCustomNotificationDialog
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class ListScheduleAlarmSettingCameraJFFragment :
    BaseFragment<FragmentListScheduleAlarmCameraJfBinding, ListScheduleAlarmSettingCameraJFViewModel>() {

    @Inject
    lateinit var appNavigation: AppNavigation

    private val viewModel: ListScheduleAlarmSettingCameraJFViewModel by viewModels()

    private val adapter: ScheduleAlarmCameraJFAdapter by lazy {
        ScheduleAlarmCameraJFAdapter(
            onClickItem = {
                openAddScheduleAlarm(it.position)
            },
            onLongClickItem = {
                viewModel.changeDeleteState(true)
            },
            onSwitchButtonClick = { state, item ->
                viewModel.changeOnOffStateSchedule(state, item)
            },
            onCheckBoxClick = { state, item ->
                viewModel.changeSelectedDeleteStateSchedule(state, item)
            }
        )
    }

    override val layoutId = R.layout.fragment_list_schedule_alarm_camera_jf

    override fun getVM(): ListScheduleAlarmSettingCameraJFViewModel = viewModel

    override fun initView() {
        init()
        initData()
    }

    private fun init() {
        initLayout()
        handleOnBackPress()
    }

    private fun initData() {
        arguments.takeIf { it?.containsKey(Define.BUNDLE_KEY.PARAM_ID) == true }?.let {
            viewModel.devId = it.getString(Define.BUNDLE_KEY.PARAM_ID) ?: ""
        }
        if (context?.isNetworkAvailable() == true) {
            lifecycleScope.launchWhenResumed {
                try {
                    showHideLoading(true)
                    viewModel.getConfig()
                } catch (e: Exception) {
                    DebugConfig.loge(TAG, "getListScheduleAlarmCamera e ${e.message}")
                    showHideLoading(false)
                }
            }
        } else {
            showCustomNotificationDialog(
                title = getString(com.vht.sdkcore.R.string.no_connection),
                type = DialogType.ERROR,
                titleBtnConfirm = com.vht.sdkcore.R.string.text_close
            ) {
                appNavigation.navigateUp()
            }
        }
    }

    private fun initLayout() {
        binding.rvSchedule.adapter = adapter
        binding.btClear.isVisible = viewModel.deleteState.value ?: false
        binding.ivAdd.isVisible = !(viewModel.deleteState.value ?: false)
        binding.tvTitle.isVisible = !(viewModel.deleteState.value ?: false)
        binding.tvSelectedCount.isVisible = viewModel.deleteState.value ?: false
        binding.cbSelectedAll.isVisible = viewModel.deleteState.value ?: false
    }

    private fun handleOnBackPress() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (viewModel.deleteState.value == false) {
                        appNavigation.navigateUp()
                    } else {
                        viewModel.changeDeleteState(false)
                    }
                }
            })
    }

    override fun setOnClick() {
        super.setOnClick()

        binding.toolBar.setOnLeftClickListener {
            if (viewModel.deleteState.value == false) {
                appNavigation.navigateUp()
            } else {
                viewModel.changeDeleteState(false)
            }
        }

        binding.cbSelectedAll.setOnCheckedChangeListener { button, checked ->
            if (button.isPressed && viewModel.deleteState.value == true) {
                viewModel.selectedDeleteStateAllSchedule(checked)
            }
        }

        binding.btClear.setOnClickListener {
            if ((viewModel.selectDeleteStateCount.value ?: 0) <= 0) {
                return@setOnClickListener
            }
            if (context?.isNetworkAvailable() == true) {
                showCustomNotificationDialog(
                    title = "Bạn có chắc muốn xóa lịch trình",
                    type = DialogType.CONFIRM,
                    titleBtnConfirm = com.vht.sdkcore.R.string.btn_ok,
                    negativeTitle = com.vht.sdkcore.R.string.btn_cancel,
                ) {
                    lifecycleScope.launchWhenResumed {
                        try {
                            viewModel.clearSchedule()
                        } catch (e: Exception) {
                            DebugConfig.loge(TAG, "deleteScheduleAlarmCamera e ${e.message}")
                        }
                    }
                }
            } else {
                showCustomNotificationDialog(
                    title = getString(com.vht.sdkcore.R.string.no_connection),
                    type = DialogType.ERROR,
                    titleBtnConfirm = com.vht.sdkcore.R.string.text_close
                ) {}
            }
        }

        binding.ivAdd.setOnClickListener {
            if (context?.isNetworkAvailable() == false) {
                showCustomNotificationDialog(
                    title = getString(com.vht.sdkcore.R.string.no_connection),
                    type = DialogType.ERROR
                ) {}
            } else {
                if ((viewModel.scheduleList.value?.size ?: 0) < TOTAL_SCHEDULE_ALARM) {
                    openAddScheduleAlarm()
                }
            }
        }
        binding.btAddSchedule.setOnClickListener {
            if ((viewModel.scheduleList.value?.size ?: 0) < TOTAL_SCHEDULE_ALARM) {
                openAddScheduleAlarm()
            }
        }
    }

    override fun bindingStateView() {
        super.bindingStateView()
        viewModel.deleteState.observe(viewLifecycleOwner) {
            binding.btClear.isVisible = it
            binding.ivAdd.isVisible = !it
            binding.tvTitle.isVisible = !it
            binding.tvSelectedCount.isVisible = it
            binding.cbSelectedAll.isVisible = it
        }

        viewModel.selectDeleteStateCount.observe(viewLifecycleOwner) {
            @SuppressLint("SetTextI18n")
            binding.tvSelectedCount.text = "Đã chọn $it"
            if (it == 0) {
                binding.textBtnClear.setTextColor(requireContext().getColorCompat(R.color.color_4E4E4E))
                binding.imgBtnClear.setColorFilter(requireContext().getColorCompat(R.color.color_4E4E4E))
            } else {
                binding.textBtnClear.setTextColor(requireContext().getColorCompat(R.color.color_F8214B))
                binding.imgBtnClear.setColorFilter(requireContext().getColorCompat(R.color.color_F8214B))
            }
        }

        viewModel.selectAllDeleteState.observe(viewLifecycleOwner) {
            binding.cbSelectedAll.isChecked = it
        }

        viewModel.scheduleList.observe(viewLifecycleOwner) { result ->
            adapter.submitList(result)
            binding.ivListAlarmNull.isVisible = result.isNullOrEmpty()
            binding.tvListAlarmNull.isVisible = result.isNullOrEmpty()
            binding.btAddSchedule.isVisible = result.isNullOrEmpty()
            binding.rvSchedule.isVisible = result.isNotEmpty()
            if (result.isEmpty()) {
                binding.tvTitle.isVisible = false
            } else {
                binding.tvTitle.isVisible = !(viewModel.deleteState.value ?: false)
            }
            binding.ivAdd.alpha = if (result.size == TOTAL_SCHEDULE_ALARM) 0.5f else 1f
            showHideLoading(false)
        }
    }

    private fun openAddScheduleAlarm(scheduleId: Int = -1) {
        val bundle = Bundle().apply {
            putString(Define.BUNDLE_KEY.PARAM_ID, viewModel.devId)
            putInt(ARG_ID_SCHEDULE_PARAM, scheduleId)
        }
        appNavigation.openAddScheduleAlarmJFCamera(bundle)
    }

    companion object {
        private const val TAG = "ListScheduleAlarmSettingCameraJFFragment"
    }
}
