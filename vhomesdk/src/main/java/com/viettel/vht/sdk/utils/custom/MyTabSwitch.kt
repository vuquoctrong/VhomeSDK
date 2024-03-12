package com.viettel.vht.sdk.utils.custom

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.viettel.vht.sdk.R
import com.viettel.vht.sdk.databinding.LayoutTabSwitchBinding


class MyTabSwitch(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {
    private lateinit var onChanged: OnTabSwitched
    private var isTab1 = true
    private var binding: LayoutTabSwitchBinding
    private var tabChecked = R.drawable.background_color_button
    private var tabUnchecked = R.drawable.bgr_switch_tab
    private var textCheckedColor = ContextCompat.getColor(context, R.color.color_white)
    private var textUnCheckedColor = ContextCompat.getColor(context, R.color.color_4E4E4E)

    init {
        binding = LayoutTabSwitchBinding.inflate(LayoutInflater.from(context), this, true)
        if (attrs != null) {
            val styledAttrs =
                context.theme.obtainStyledAttributes(attrs, R.styleable.MyTabSwitch, 0, 0)
            // Tab checked
            isTab1 = styledAttrs.getBoolean(R.styleable.MyTabSwitch_check_tab1, true)

            // Tab text
            val titleTab1 = styledAttrs.getString(R.styleable.MyTabSwitch_tab1_title)
            val titleTab2 = styledAttrs.getString(R.styleable.MyTabSwitch_tab2_title)
            isTab1 = styledAttrs.getBoolean(R.styleable.MyTabSwitch_tab1_check,false)
            binding.tab1.text = titleTab1
            binding.tab2.text = titleTab2

            // Tab text color
            textCheckedColor = styledAttrs.getColor(
                R.styleable.MyTabSwitch_text_checked_color,
                ContextCompat.getColor(context, R.color.color_white)
            )
            textUnCheckedColor = styledAttrs.getColor(
                R.styleable.MyTabSwitch_text_unchecked_color,
                ContextCompat.getColor(context, R.color.color_4E4E4E)
            )

            // Tab background
            tabChecked =
                styledAttrs.getResourceId(
                    R.styleable.MyTabSwitch_tab_checked_background,
                    R.drawable.background_color_button
                )
            tabUnchecked = styledAttrs.getResourceId(
                R.styleable.MyTabSwitch_tab_unchecked_background,
                R.drawable.bgr_switch_tab
            )
        }
        if (isTab1) {
            tab1Checked()
        } else {
            tab2Checked()
        }
        binding.tab1.setOnClickListener {
            if (isTab1) return@setOnClickListener
            tab1Checked()
            if (::onChanged.isInitialized) {
                onChanged.onChange(isTab1)
            }
        }
        binding.tab2.setOnClickListener {
            if (!isTab1) return@setOnClickListener
            tab2Checked()
            if (::onChanged.isInitialized) {
                onChanged.onChange(isTab1)
            }
        }
    }
    fun disableClickTab2(){
        binding.tab2.isClickable = false
    }

    fun tab1Checked() {
        isTab1 = true
        binding.tab1.setBackgroundResource(tabChecked)
        binding.tab1.setTextColor(textCheckedColor)
        binding.tab2.setBackgroundResource(tabUnchecked)
        binding.tab2.setTextColor(textUnCheckedColor)
    }

    fun tab2Checked() {
        isTab1 = false
        binding.tab2.setBackgroundResource(tabChecked)
        binding.tab2.setTextColor(textCheckedColor)
        binding.tab1.setBackgroundResource(tabUnchecked)
        binding.tab1.setTextColor(textUnCheckedColor)
    }

    fun addOnTabSwitched(onChanged: OnTabSwitched) {
        this.onChanged = onChanged
    }

    fun disableClick(){
        binding.tab1.isClickable = false
        binding.tab2.isClickable = false
    }

    fun isClickableClickTab2(isClickable:Boolean){
        binding.tab2.isClickable = isClickable
    }

    fun isCheckedtab1() = isTab1

    interface OnTabSwitched {
        fun onChange(isTab1: Boolean)
    }
}