package com.viettel.vht.sdk.ui.jfcameradetail.alarm_voice_period

import android.annotation.SuppressLint
import androidx.fragment.app.viewModels
import com.vht.sdkcore.base.BaseFragment
import com.vht.sdkcore.utils.showCustomToast
import com.viettel.vht.sdk.R
import com.viettel.vht.sdk.databinding.FragmentAddScheduleAlarmCameraJfBinding
import com.viettel.vht.sdk.navigation.AppNavigation
import com.viettel.vht.sdk.ui.jfcameradetail.schedule_alarm.dialog.BottomSheetSettingTimeScheduleFragment
import com.viettel.vht.sdk.ui.jfcameradetail.schedule_alarm.model.TimeItem
import com.viettel.vht.sdk.ui.jfcameradetail.schedule_alarm.model.TimeItem.Companion.FRIDAY_POSITION
import com.viettel.vht.sdk.ui.jfcameradetail.schedule_alarm.model.TimeItem.Companion.MONDAY_POSITION
import com.viettel.vht.sdk.ui.jfcameradetail.schedule_alarm.model.TimeItem.Companion.SATURDAY_POSITION
import com.viettel.vht.sdk.ui.jfcameradetail.schedule_alarm.model.TimeItem.Companion.SUNDAY_POSITION
import com.viettel.vht.sdk.ui.jfcameradetail.schedule_alarm.model.TimeItem.Companion.THURSDAY_POSITION
import com.viettel.vht.sdk.ui.jfcameradetail.schedule_alarm.model.TimeItem.Companion.TUESDAY_POSITION
import com.viettel.vht.sdk.ui.jfcameradetail.schedule_alarm.model.TimeItem.Companion.WEDNESDAY_POSITION
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class AddAlarmVoicePeriodJFFragment :
    BaseFragment<FragmentAddScheduleAlarmCameraJfBinding, AddAlarmVoicePeriodJFViewModel>() {

    @Inject
    lateinit var appNavigation: AppNavigation

    private val viewModel: AddAlarmVoicePeriodJFViewModel by viewModels()

    override val layoutId = R.layout.fragment_add_schedule_alarm_camera_jf

    override fun getVM(): AddAlarmVoicePeriodJFViewModel = viewModel

    override fun initView() {
        super.initView()
        initData()
    }

    private fun initData() {
        viewModel.getConfig()
    }

    override fun setOnClick() {
        super.setOnClick()

        binding.toolBar.setOnLeftClickListener {
            appNavigation.navigateUp()
        }

        binding.toolBar.setOnRightClickListener {
            viewModel.saveSchedule()
        }

        binding.contentTime.setOnClickListener {
            BottomSheetSettingTimeScheduleFragment.newInstance(
                object : BottomSheetSettingTimeScheduleFragment.OnBottomSheetTimeSchedule {
                    override fun onSubmit(time: List<String>) {
                        viewModel.selectTime(time)
                    }

                    override fun onCancel(time: List<String>) {}
                },
                viewModel.timeListData.value
                    .takeIf { !it.isNullOrEmpty() }
                    ?.let { listOf(it[0], it[1], it[3], it[4]) } ?: listOf()
            ).show(childFragmentManager, BottomSheetSettingTimeScheduleFragment.TAG)
        }

        binding.cbCN.setOnClickListener { viewModel.selectDayOfWeek(SUNDAY_POSITION) }
        binding.cbT2.setOnClickListener { viewModel.selectDayOfWeek(MONDAY_POSITION) }
        binding.cbT3.setOnClickListener { viewModel.selectDayOfWeek(TUESDAY_POSITION) }
        binding.cbT4.setOnClickListener { viewModel.selectDayOfWeek(WEDNESDAY_POSITION) }
        binding.cbT5.setOnClickListener { viewModel.selectDayOfWeek(THURSDAY_POSITION) }
        binding.cbT6.setOnClickListener { viewModel.selectDayOfWeek(FRIDAY_POSITION) }
        binding.cbT7.setOnClickListener { viewModel.selectDayOfWeek(SATURDAY_POSITION) }
    }

    @SuppressLint("SetTextI18n")
    override fun bindingStateView() {
        super.bindingStateView()
        viewModel.dayOfWeekSelected.observe(viewLifecycleOwner) {
            binding.cbCN.isChecked = it[SUNDAY_POSITION]
            binding.cbT2.isChecked = it[MONDAY_POSITION]
            binding.cbT3.isChecked = it[TUESDAY_POSITION]
            binding.cbT4.isChecked = it[WEDNESDAY_POSITION]
            binding.cbT5.isChecked = it[THURSDAY_POSITION]
            binding.cbT6.isChecked = it[FRIDAY_POSITION]
            binding.cbT7.isChecked = it[SATURDAY_POSITION]
            binding.tvSelectedCount.text = TimeItem.getWeeks(it)
        }

        viewModel.timeListData.observe(viewLifecycleOwner) {
            binding.tvTime.text = String.format("%s:%s - %s:%s", it[0], it[1], it[3], it[4])
        }

        viewModel.saveScheduleState.observe(viewLifecycleOwner) {
            showCustomToast(
                title = if (it) "Cập nhật thành công" else "Cập nhật thất bại",
                onFinish = { appNavigation.navigateUp() }
            )
        }

        viewModel.getScheduleState.observe(viewLifecycleOwner) {
            if (!it) {
                showCustomToast(
                    title = "Lịch trình này không tồn tại",
                    onFinish = { appNavigation.navigateUp() }
                )
            }
        }

        viewModel.error.observe(viewLifecycleOwner) {
            showCustomToast(title = it, onFinish = {})
        }
    }

    companion object {
        const val ARG_ID_SCHEDULE_PARAM = "ARG_ID_SCHEDULE_PARAM"
    }
}
