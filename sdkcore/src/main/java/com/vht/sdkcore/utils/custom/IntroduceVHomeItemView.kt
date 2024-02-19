package com.vht.sdkcore.utils.custom

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.widget.RelativeLayout
import android.widget.TextView
import com.vht.sdkcore.R

class IntroduceVHomeItemView : RelativeLayout {

    lateinit var rlSettingItem: RelativeLayout
    lateinit var tvTitleSetting: TextView
    lateinit var tvValueSetting: TextView
    private var stringTitle: String? = ""
    private var stringValue: String? = ""

    constructor(context: Context) : super(context) {
        initView()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        val a =
            context.theme.obtainStyledAttributes(attrs, R.styleable.SettingItem, 0, 0)
        stringTitle = a.getString(R.styleable.SettingItem_string_title_setting)
        stringValue = a.getString(R.styleable.SettingItem_string_value)
        initView()
    }

    private fun initView() {
        val view = inflate(context, R.layout.item_common_introduct_vhome, null)
        addView(view)
        rlSettingItem = findViewById(R.id.rl_introduce_item)
        tvTitleSetting = findViewById(R.id.tv_introduce_item)
        tvValueSetting = findViewById(R.id.tv_introduce_value)
        tvTitleSetting.text = stringTitle
        tvValueSetting.text = stringValue
    }

    fun setTitle(title: String) {
        tvValueSetting.text = if (!TextUtils.isEmpty(title)) title else ""
    }

    fun getTitle() : String {
        return tvTitleSetting.text.toString()
    }

    fun setTvValue(value: String?) {
        tvValueSetting.text = if (!TextUtils.isEmpty(value)) value else ""
    }

    fun getTvValue(): String {
        return tvValueSetting.text.toString()
    }
}