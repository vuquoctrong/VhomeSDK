package com.vht.sdkcore.utils.custom

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import com.vht.sdkcore.R
import com.vht.sdkcore.databinding.LayoutDelaySwitchBinding
import java.util.concurrent.atomic.AtomicBoolean

class DelaySwitch @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : ConstraintLayout(context, attrs, defStyle) {

    private val delayHandler by lazy { Handler(Looper.getMainLooper()) }

    private lateinit var binding: LayoutDelaySwitchBinding

    private var canTouch = AtomicBoolean(true)

    init {
        initView()
    }

    private fun initView() {
        binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.layout_delay_switch, this, true)
    }

    fun setOnDelayClickListener(delay: Long, action: (Boolean) -> Unit) {
        binding.view.setOnClickListener {
            if (canTouch.compareAndSet(true, false)) {
                action.invoke(!binding.mSwitch.isChecked)
                delayHandler.postDelayed({
                    canTouch.set(true)
                }, delay)
            }
        }
    }

    fun setIsChecked(isChecked: Boolean) {
        binding.mSwitch.isChecked = isChecked
    }

    override fun onDetachedFromWindow() {
        delayHandler.removeCallbacksAndMessages(null)
        super.onDetachedFromWindow()
    }

}