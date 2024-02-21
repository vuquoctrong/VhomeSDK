package com.viettel.vht.sdk.utils.custom

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.annotation.ColorRes
import androidx.core.view.isVisible
import androidx.core.widget.ImageViewCompat
import androidx.databinding.DataBindingUtil
import com.vht.sdkcore.utils.gone
import com.vht.sdkcore.utils.invisible
import com.vht.sdkcore.utils.setGradientColor
import com.vht.sdkcore.utils.visible
import com.viettel.vht.sdk.R
import com.viettel.vht.sdk.databinding.LayoutAppToolbarBinding

class AppToolbar @JvmOverloads constructor(
    context: Context,
    private val attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle) {

    private lateinit var binding: LayoutAppToolbarBinding

    init {
        initView()
    }

    private fun initView() {
        binding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.layout_app_toolbar,
            this,
            true
        )

        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.AppToolbar, 0, 0)

        elevation = resources.getDimensionPixelSize(R.dimen.dimen_2).toFloat()

        val srcLeft = a.getResourceId(R.styleable.AppToolbar_src_left, -1)
        setSrcLeft(srcLeft)

        val srcRight = a.getResourceId(R.styleable.AppToolbar_src_right, -1)
        setSrcRight(srcRight)

        val srcBackground = a.getResourceId(R.styleable.AppToolbar_src_background, -1)
        setSrcBackground(srcBackground)

        val stringRight = a.getString(R.styleable.AppToolbar_string_right)
        setStringRight(stringRight)

        val srcRightColor = a.getString(R.styleable.AppToolbar_src_right_color) ?: ""
        setSrcRightColor(srcRightColor)

        val stringTitle = a.getString(R.styleable.AppToolbar_string_title)
        setStringTitle(stringTitle)

        val showElevation = a.getBoolean(R.styleable.AppToolbar_show_elevation, true)
        showElevation(showElevation)
    }

    fun setSrcLeft(resId: Int) {
        if (resId != -1) {
            binding.ivLeft.setImageResource(resId)
        } else {
            binding.ivLeft.invisible()
        }
    }

    fun setSrcRight(resId: Int) {
        if (resId != -1) {
            binding.ivRight.visible()
            binding.ivRight.setImageResource(resId)
            binding.tvRight.gone()
        } else {
            binding.ivRight.gone()
        }
    }

    fun setRightClickEnabled(isEnabled: Boolean) {
        binding.ivRight.isEnabled = isEnabled
        binding.tvRight.isEnabled = isEnabled
    }

    fun setSrcRightColor(color: String) {
        val mColor = try {
            Color.parseColor(color)
        } catch (e: Exception) {
            -1
        }
        if (mColor != -1) {
            ImageViewCompat.setImageTintList(binding.ivRight, ColorStateList.valueOf(mColor))
            binding.tvRight.setTextColor(mColor)
        }
    }

    fun setSrcRightColor(color: Int) {
        if (color != -1) {
            ImageViewCompat.setImageTintList(binding.ivRight, ColorStateList.valueOf(color))
            binding.tvRight.setTextColor(color)
        }
    }

    fun setRightGradientTextColor(@ColorRes start: Int, @ColorRes end: Int) {
        binding.tvRight.setGradientColor(start, end)
    }

    private fun setSrcBackground(resId: Int) {
        if (resId != -1) {
            binding.ivBackground.setImageResource(resId)
        } else {
            binding.ivBackground.setImageResource(com.vht.sdkcore.R.drawable.ic_toolbar_top)
        }
    }

    fun showBackGroundTop(isShow: Boolean) {
        binding.ivBackground.isVisible = isShow
        binding.ivBackgroundBottom.isVisible = isShow
    }

    fun setSubTitle(resId: Int, text: String) {
        binding.tvSubTitle.visible()
        binding.tvSubTitle.text = text
        binding.tvSubTitle.setCompoundDrawablesWithIntrinsicBounds(resId, 0, 0, 0)
    }

    fun showTextRight() {
        binding.tvRight.visible()
    }

    fun hideTextRight() {
        binding.tvRight.gone()
    }

    fun setStringRight(text: String?) {
        if (!text.isNullOrBlank()) {
            binding.tvRight.visible()
            binding.tvRight.text = text
            binding.ivRight.gone()
        } else {
            binding.tvRight.gone()
        }
    }

    fun setStringTitle(text: String?) {
        if (!text.isNullOrBlank()) {
            binding.tvTitle.text = text
        }
    }

    fun showElevation(isShow: Boolean) {
        if (isShow){
            binding.ivBackgroundBottom.visible()
        }else{
            binding.ivBackgroundBottom.gone()
        }
    }

    fun setTitleMaxLines(maxLines: Int) {
        binding.tvTitle.maxLines = maxLines
    }

    fun setOnLeftClickListener(action: () -> Unit) {
        binding.ivLeft.setOnClickListener {
            if (it.isVisible) action.invoke()
        }
    }

    fun setOnRightClickListener(action: () -> Unit) {
        binding.ivRight.setOnClickListener {
            if (it.isVisible) action.invoke()
        }
        binding.tvRight.setOnClickListener {
            if (it.isVisible) action.invoke()
        }
    }

    fun getStringTitle() = binding.tvTitle.text.toString()

    fun showHiddenBack(isShow: Boolean) {
        if (isShow) {
            binding.ivLeft.visible()
        } else {
            binding.ivLeft.invisible()
        }
    }

    fun showButtonRight(isShow: Boolean) {
        if (isShow) {
            if (!binding.tvRight.text.isNullOrBlank()) {
                binding.tvRight.visible()
            } else {
                binding.ivRight.visible()
            }
        } else {
            binding.tvRight.gone()
            binding.ivRight.gone()
        }
    }

    fun setColorTextTextRight(color: String){
        binding.tvRight.setTextColor(Color.parseColor(color))
    }

    fun setEnableTexRight(isEnable: Boolean){
        if(isEnable){
            binding.tvRight.isEnabled = true
            binding.tvRight.isClickable = true
            binding.tvRight.alpha = 1f
        }else{
            binding.tvRight.isEnabled = false
            binding.tvRight.isClickable = false
            binding.tvRight.alpha = 0.5f
        }
    }
    fun showIconShareDevice(isShow: Boolean){
        if (isShow) {
            binding.ivShareDevice.visible()
        } else {
            binding.ivShareDevice.gone()
        }
    }

    fun getButtonRight() = binding.ivRight

    fun setDrawableTitle(img: Int?) {
        if (img!=null) {
            binding.tvTitle.setCompoundDrawablesWithIntrinsicBounds(img,0,0,0)
            binding.tvTitle.setCompoundDrawablePadding(20)
        }
    }
}