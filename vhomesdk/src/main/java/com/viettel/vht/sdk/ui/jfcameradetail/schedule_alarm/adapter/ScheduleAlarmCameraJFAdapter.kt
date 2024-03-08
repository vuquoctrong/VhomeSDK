package com.viettel.vht.sdk.ui.jfcameradetail.schedule_alarm.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.viettel.vht.sdk.ui.jfcameradetail.schedule_alarm.model.TimeItem
import com.viettel.vht.sdk.R
import com.viettel.vht.sdk.databinding.ItemScheduleAlarmCameraJfBinding

class ScheduleAlarmCameraJFAdapter(
    private val onClickItem: (TimeItem) -> Unit,
    private val onLongClickItem: (TimeItem) -> Unit,
    private val onSwitchButtonClick: (Boolean, TimeItem) -> Unit,
    private val onCheckBoxClick: (Boolean, TimeItem) -> Unit,
) : ListAdapter<TimeItem, ScheduleAlarmCameraJFAdapter.ScheduleAlarmHolder>(ChatDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ScheduleAlarmHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_schedule_alarm_camera_jf,
                parent, false
            ),
            onClickItem, onLongClickItem, onSwitchButtonClick, onCheckBoxClick
        )

    override fun onBindViewHolder(holder: ScheduleAlarmHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun submitList(list: List<TimeItem>?) {
        val result = arrayListOf<TimeItem>()
        list?.forEach { result.add(it.copy()) }
        super.submitList(result)
    }

    class ScheduleAlarmHolder(
        private val binding: ItemScheduleAlarmCameraJfBinding,
        private val onClickItem: (TimeItem) -> Unit,
        private val onLongClickItem: (TimeItem) -> Unit,
        private val onSwitchButtonClick: (Boolean, TimeItem) -> Unit,
        private val onCheckBoxClick: (Boolean, TimeItem) -> Unit,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: TimeItem) {
            binding.cbSelectedDelete.isVisible = item.stateDelete
            binding.cbSelectedDelete.isChecked = item.isSelectDelete
            binding.tvTime.text = item.time
            binding.tvDay.text = TimeItem.getWeeks(item.dayOfWeek)
            binding.tvDay.isSelected = true
            binding.swStatusOnOff.isChecked = item.isOpen
            binding.content.setOnClickListener {
                if (!item.stateDelete) {
                    onClickItem.invoke(item)
                }
            }
            binding.swStatusOnOff.setOnCheckedChangeListener { button, checked ->
                if (button.isPressed) {
                    onSwitchButtonClick.invoke(checked, item)
                }
            }
            binding.cbSelectedDelete.setOnCheckedChangeListener { button, checked ->
                if (button.isPressed && item.stateDelete) {
                    onCheckBoxClick.invoke(checked, item)
                }
            }
            binding.content.setOnLongClickListener {
                if (!item.stateDelete) {
                    onLongClickItem.invoke(item)
                }
                true
            }
        }
    }
}

class ChatDiffCallback : DiffUtil.ItemCallback<TimeItem>() {
    override fun areItemsTheSame(oldItem: TimeItem, newItem: TimeItem): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: TimeItem, newItem: TimeItem): Boolean {
        return oldItem == newItem
    }
}
