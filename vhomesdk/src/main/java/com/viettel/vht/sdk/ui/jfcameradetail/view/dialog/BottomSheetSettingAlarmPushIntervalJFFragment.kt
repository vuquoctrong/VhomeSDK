package com.viettel.vht.sdk.ui.jfcameradetail.view.dialog

import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.vht.sdkcore.adapter.BaseAdapter
import com.viettel.vht.sdk.R
import com.viettel.vht.sdk.databinding.FragmentBottomSheetSettingAlarmPushIntervalJfBinding
import com.viettel.vht.sdk.databinding.ItemSettingAlarmPushIntervalJfBinding
import com.viettel.vht.sdk.ui.jfcameradetail.SettingAlarmFeatureCameraJFViewModel
import com.viettel.vht.sdk.utils.BaseBottomSheetTimePickerFragment

class BottomSheetSettingAlarmPushIntervalJFFragment :
    BaseBottomSheetTimePickerFragment<FragmentBottomSheetSettingAlarmPushIntervalJfBinding>(),
    AlarmPushIntervalAdapter.OnAlarmPushIntervalListener {

    private val viewModel: SettingAlarmFeatureCameraJFViewModel by viewModels(
        ownerProducer = { requireParentFragment() }
    )

    private var alarmPushIntervalAdapter: AlarmPushIntervalAdapter? = null

    override val layoutId: Int = R.layout.fragment_bottom_sheet_setting_alarm_push_interval_jf

    override fun initView() {
        alarmPushIntervalAdapter = AlarmPushIntervalAdapter(this)
        binding.rvAlarmPushInterval.adapter = alarmPushIntervalAdapter
        alarmPushIntervalAdapter?.submitList(generateListAlarmPushInterval())
    }

    override fun initControl() {
        super.initControl()
        binding.layoutRoot.setOnClickListener {
            dismiss()
        }
    }

    override fun listTimePicker(): List<View> = listOf(binding.rvAlarmPushInterval)

    override fun onAlarmPushIntervalClick(item: AlarmPushInterval) {
        if (!item.isChecked) {
            viewModel.setAlarmInterval(item.interval)
        }
        dismiss()
    }

    private fun generateListAlarmPushInterval(): List<AlarmPushInterval> {
        val list = listAlarmPushInterval.map { AlarmPushInterval(it.title, it.value, it.interval) }
        list.forEach {
            if (it.interval == viewModel.alarmPushInterval.value) {
                it.isChecked = true
            }
        }
        return list
    }

    companion object {
        const val TAG = "BottomSheetSettingAlarmPushIntervalJFFragment"
        val listAlarmPushInterval = listOf(
            AlarmPushInterval("10 giây", "10s", 10),
            AlarmPushInterval("20 giây", "20s", 20),
            AlarmPushInterval("30 giây", "30s", 30),
            AlarmPushInterval("1 phút", "1m", 60),
            AlarmPushInterval("5 phút", "5m", 300),
            AlarmPushInterval("10 phút", "10m", 600),
            AlarmPushInterval("15 phút", "15m", 900),
            AlarmPushInterval("20 phút", "20m", 1200),
            AlarmPushInterval("25 phút", "25m", 1500),
            AlarmPushInterval("30 phút", "30m", 1800),
        )

        fun newInstance() = BottomSheetSettingAlarmPushIntervalJFFragment()
    }
}

class AlarmPushIntervalAdapter(
    private val onAlarmPushIntervalListener: OnAlarmPushIntervalListener
) : BaseAdapter<AlarmPushInterval, ItemSettingAlarmPushIntervalJfBinding>() {
    override val itemLayoutRes = R.layout.item_setting_alarm_push_interval_jf

    override fun bindMultiTime(holder: BaseViewHolder, position: Int) {
        val item = getItem(position)
        holder.itemBinding.apply {
            tvTitle.text = item.title
            checkBox.isVisible = item.isChecked
            root.setOnClickListener {
                onAlarmPushIntervalListener.onAlarmPushIntervalClick(item)
            }
        }
    }

    override fun bindSingleTime(holder: BaseViewHolder, position: Int) {}

    interface OnAlarmPushIntervalListener {
        fun onAlarmPushIntervalClick(item: AlarmPushInterval)
    }
}

class AlarmPushInterval(
    val title: String,
    val value: String,
    val interval: Int,
    var isChecked: Boolean = false
)
