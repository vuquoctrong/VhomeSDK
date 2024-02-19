package com.vht.sdkcore.utils.custom

import android.content.Context
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import com.vht.sdkcore.R

class SettingVHomeItemView : RelativeLayout {

    lateinit var rlSettingItem: RelativeLayout
    lateinit var tvTitleSetting: TextView
    lateinit var tvValueSetting: TextView
    lateinit var toggleButton: SwitchCompat
    lateinit var icArrowLeft: ImageView
    private var stringTitle: String? = ""
    private var stringValue: String? = ""
    private var isShowButtonSwitch = false
    private var isShowArrow = true
    private var trailingIcon: Drawable? = null


    constructor(context: Context) : super(context) {
        initView()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        val a =
            context.theme.obtainStyledAttributes(attrs, R.styleable.SettingItem, 0, 0)
        stringTitle = a.getString(R.styleable.SettingItem_string_title_setting)
        stringValue = a.getString(R.styleable.SettingItem_string_value)
        isShowButtonSwitch = a.getBoolean(R.styleable.SettingItem_is_show_switch, false)
        isShowArrow = a.getBoolean(R.styleable.SettingItem_is_show_arrow, false)
        trailingIcon = a.getDrawable(R.styleable.SettingItem_trailingIcon)
        initView()
    }

    private fun initView() {
        val view = inflate(context, R.layout.item_common_setting_vhome, null)
        addView(view)
        rlSettingItem = findViewById(R.id.rl_setting_item)
        tvTitleSetting = findViewById(R.id.tv_setting_item)
        tvValueSetting = findViewById(R.id.tv_value)
        icArrowLeft = findViewById(R.id.ic_arrow_left)
        toggleButton = findViewById(R.id.toggleButton)
        tvTitleSetting.text = stringTitle
        icArrowLeft.visibility = if (isShowArrow) View.VISIBLE else View.INVISIBLE
        if (trailingIcon != null) {
            icArrowLeft.setImageDrawable(trailingIcon)
        } else {
            icArrowLeft.setImageDrawable(context.getDrawable(R.drawable.ic_next))
        }
        toggleButton.visibility = if (isShowButtonSwitch) View.VISIBLE else View.GONE
    }

    private fun setValueButtonSwitch(enable: Boolean) {
        toggleButton.isChecked = enable
    }

    fun getValueButtonSwitch(): Boolean {
        return toggleButton.isChecked
    }

    fun setTvValue(title: String?) {
        tvValueSetting.text = if (!TextUtils.isEmpty(title)) title else ""
    }

    fun getTvValue(): String {
        return tvValueSetting.text.toString()
    }

    fun setColorTvValue(colorId: Int) {
        tvValueSetting.setTextColor(context.getColor(colorId))
    }
}