package com.viettel.vht.sdk.utils.custom

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.viettel.vht.sdk.R
import com.viettel.vht.sdk.databinding.LayoutIconFeatureEzvizBinding


class IconFeatureJFCamera(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {
    private var binding: LayoutIconFeatureEzvizBinding
    private var action: OnAction? = null
    private var isActived = false
    var state = false
    private var isDeviceShare = false
    var onActiveChanged: ((Boolean) -> Unit)? = null

    init {
        binding = LayoutIconFeatureEzvizBinding.inflate(LayoutInflater.from(context), this, true)
        if (attrs != null) {
            val styledAttrs =
                context.theme.obtainStyledAttributes(attrs, R.styleable.IconFeature, 0, 0)
            val text = styledAttrs.getString(R.styleable.IconFeature_text)
            val icon = styledAttrs.getDrawable(R.styleable.IconFeature_icon)
            state = styledAttrs.getBoolean(R.styleable.IconFeature_state_active, false)
            binding.tv.text = text
            binding.imvIcon.setImageDrawable(icon)
        }

        binding.root.setOnClickListener {
            if (state) {
                onActiveChanged?.invoke(isActived)
                isActived = !isActived
                active(isActived)
            } else {
                action?.onClick()
            }
        }
    }

    fun setText(text:String){
        binding.tv.text = text
    }

    fun setIcon(icon:Int){
        binding.imvIcon.setImageResource(icon)
    }
    fun setIsDeviceShare(isDeviceShare: Boolean = false){
        this.isDeviceShare = isDeviceShare
    }

    fun enable(isEnabled: Boolean) {
        if (isEnabled) {
            binding.tv.setTextColor(Color.parseColor("#000000"))
            binding.imvIcon.setBackgroundResource(R.drawable.bgr_normal_icon_feature)
            binding.root.isClickable = true
            binding.root.isEnabled = true
            binding.tv.alpha = 1f
            binding.imvIcon.alpha = 1f
        } else {
            binding.root.isClickable = false
            binding.root.isEnabled = false
            binding.tv.alpha = 0.5f
            binding.imvIcon.alpha = 0.5f
        }
    }

    fun active(isActived: Boolean) {
        this.isActived = isActived
        if(!isDeviceShare){
            if (isActived) {
                binding.imvIcon.setBackgroundResource(R.drawable.bgr_actived_icon_feature)
            } else {
                binding.imvIcon.setBackgroundResource(R.drawable.bgr_normal_icon_feature)
            }
        }

    }

    fun setOnAction(action: OnAction) {
        this.action = action
    }

    interface OnAction {
        fun onClick()
    }
}