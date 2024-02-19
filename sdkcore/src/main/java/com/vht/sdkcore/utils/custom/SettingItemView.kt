package com.vht.sdkcore.utils.custom

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.constraintlayout.widget.ConstraintLayout
import com.vht.sdkcore.R
import com.vht.sdkcore.utils.visible

class SettingItemView : ConstraintLayout {

    lateinit var rlSettingItem: ConstraintLayout
    lateinit var tvTitleSetting: TextView
    lateinit var tvValueSetting: TextView
    lateinit var toggleButton: SwitchCompat
    lateinit var icArrowLeft: ImageView
    private var stringTitle: String? = ""
    private var stringValue: String? = ""
    private var isShowButtonSwitch = false
    private var isShowArrow = true

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
        initView()
    }

    private fun initView() {
        val view = inflate(context, R.layout.item_common_info, null)
        addView(view)
        rlSettingItem = findViewById(R.id.rl_setting_item)
        tvTitleSetting = findViewById(R.id.tv_setting_item)
        tvValueSetting = findViewById(R.id.tv_value)
        icArrowLeft = findViewById(R.id.ic_arrow_left)
        toggleButton = findViewById(R.id.toggleButton)
        tvTitleSetting.text = stringTitle
        icArrowLeft.visibility = if (isShowArrow) View.VISIBLE else View.INVISIBLE
        toggleButton.visibility = if (isShowButtonSwitch) View.VISIBLE else View.GONE
    }

    private fun setValueButtonSwitch(enable: Boolean) {
        toggleButton.isChecked = enable
    }

    fun getValueButtonSwitch(): Boolean {
        return toggleButton.isChecked
    }

    fun setTvValue(title: String?) {
        tvValueSetting.visible()
        tvValueSetting.text = if (!TextUtils.isEmpty(title)) title else ""
    }

    fun getTvValue(): String {
        return tvValueSetting.text.toString()
    }
}