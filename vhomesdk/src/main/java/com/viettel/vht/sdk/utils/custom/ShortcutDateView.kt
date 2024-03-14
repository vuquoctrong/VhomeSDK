package com.viettel.vht.sdk.utils.custom

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import com.viettel.vht.sdk.databinding.ItemDateShortcutBinding
import com.viettel.vht.sdk.databinding.LayoutShortcutDateBinding
import com.viettel.vht.sdk.utils.convertDateToStartDate
import com.viettel.vht.sdk.utils.gone
import com.viettel.vht.sdk.utils.visible

import java.text.SimpleDateFormat
import java.util.*

const val TIME_ONE_DAY = 86400000

class ShortcutDateView(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {
    private var binding: LayoutShortcutDateBinding =
        LayoutShortcutDateBinding.inflate(LayoutInflater.from(context), this, true)
    private var listDate = mutableListOf<Date>()
    var selectedDate = Date()
    var onSelectDate: ((Date) -> Unit)? = null
    var onSelectShowDialogDate: (() -> Unit)? = null
    val listDateCloudDayAlso = mutableListOf<Date>()
    init {
        initDate()
    }

    private fun initDate() {
        binding.containerDate.removeAllViews()
        listDate.clear()
        val currentTime = selectedDate.convertDateToStartDate()
        for (i in 6 downTo 0) {
            val time = currentTime - i * TIME_ONE_DAY
            listDate.add(Date(time))
        }
        initData()
        binding.imvDate.setOnClickListener {
            onSelectShowDialogDate?.invoke()
        }
    }

    private fun setDate(mDate: Date) {
        binding.containerDate.removeAllViews()
        selectedDate = mDate
        initData()
    }

    fun changeDate(selectedDate: Date) {
        val selectedTime = selectedDate.convertDateToStartDate()
        val minTime = listDate.first().convertDateToStartDate()
        val maxTime = listDate.last().convertDateToStartDate()
        if (selectedTime <= minTime - TIME_ONE_DAY || maxTime <= selectedTime - TIME_ONE_DAY) {
            this.selectedDate = selectedDate
            binding.containerDate.removeAllViews()
            listDate.clear()
            for (i in 6 downTo 0) {
                val time = selectedTime - i * TIME_ONE_DAY
                listDate.add(Date(time))
            }
            initData()
            binding.imvDate.setOnClickListener {
                onSelectShowDialogDate?.invoke()
            }
        } else {
            setDate(selectedDate)
        }
    }

    private fun checkIsToDay(dateToCheck: Date): Boolean{
        val dateFormat = SimpleDateFormat("dd/MM/yyyy")
        // Lấy ngày hiện tại
        val currentDate = Date()
        return dateFormat.format(currentDate) == dateFormat.format(dateToCheck)
    }
    fun initData() {
        var preSelectedItem: ItemDateShortcutBinding? = null
        val dateFormat = SimpleDateFormat("dd/MM/yyyy")
        listDate.forEach { date ->
            val item = ItemDateShortcutBinding.inflate(LayoutInflater.from(context))
//            if(checkIsToDay(date)){
//                item.tvDate.text = "Today"
//            }else{
//                item.tvDate.text = SimpleDateFormat("dd/MM").format(date)
//            }
            item.tvDate.text = SimpleDateFormat("dd/MM").format(date)
            // hiển thị ngày lưu trữ cloud
            item.selectedDate.gone()
            listDateCloudDayAlso.forEach {  dateSaveCloud ->
                if(dateFormat.format(date) == dateFormat.format(dateSaveCloud)){
                    item.selectedDate.visible()
                    return@forEach
                }
            }
            if (SimpleDateFormat("dd/MM").format(date) == SimpleDateFormat("dd/MM").format(
                    selectedDate
                )
            ) {
                preSelectedItem = item
                item.tvDate.setTextColor(Color.parseColor("#F8214B"))
            } else {
                item.tvDate.setTextColor(Color.parseColor("#000000"))
            }
            item.root.setOnClickListener {
                if (SimpleDateFormat("dd/MM").format(date) == SimpleDateFormat("dd/MM").format(
                        selectedDate
                    )
                ) {
                    return@setOnClickListener
                }
                selectedDate = date
                onSelectDate?.invoke(selectedDate)
                Log.d("TAG", "initData: selectedDate = $selectedDate")
                preSelectedItem?.tvDate?.setTextColor(Color.parseColor("#000000"))
                preSelectedItem?.selectedDate?.gone()
                item.tvDate.setTextColor(Color.parseColor("#F8214B"))
                item.selectedDate.visible()
                preSelectedItem = item
            }
            binding.containerDate.addView(item.root)
            val params = item.root.layoutParams as LayoutParams
            params.weight = 1.0f
            item.root.layoutParams = params
        }
    }
}