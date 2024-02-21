package com.vht.sdkcore.utils.custom

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.vht.sdkcore.R

open class IndicatorView : LinearLayout {
    private var indicatorColor = 0
    private var selectionColor = 0
    private var numberOfIndicators = 0
    private var indicatorSizeWidth = 0
    private var indicatorSizeHeight = 0
    private var paddingSize = 0
    private var indicatorAnimation = 0
    private var indicatorShape = 0
    var isShouldAnimateOnScrubbing = false
    var isScrubbingEnabled = false
    protected var indicators: MutableList<ImageView> = ArrayList()

    constructor(context: Context?) : super(context) {
        init(null, 0)
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init(attrs, 0)
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(attrs, defStyleAttr)
    }

    private fun init(attrs: AttributeSet?, defStyle: Int) {
        // Load attributes
        val styledAttributes = context.theme.obtainStyledAttributes(
            attrs, R.styleable.ARIndicatorView, defStyle, 0
        )
        indicatorColor =
            styledAttributes.getColor(R.styleable.ARIndicatorView_indicator_color, Color.LTGRAY)
        selectionColor =
            styledAttributes.getColor(R.styleable.ARIndicatorView_selected_color_sdk, Color.BLACK)
        numberOfIndicators =
            styledAttributes.getInteger(R.styleable.ARIndicatorView_number_of_indicators, 0)
        indicatorSizeWidth = styledAttributes.getInteger(R.styleable.ARIndicatorView_indicator_size_width, 32)
        indicatorSizeHeight = styledAttributes.getInteger(R.styleable.ARIndicatorView_indicator_size_height, 4)
        paddingSize = styledAttributes.getInteger(R.styleable.ARIndicatorView_padding_size, 0)
        indicatorAnimation =
            styledAttributes.getResourceId(R.styleable.ARIndicatorView_indicator_animation, 0)
        indicatorShape = styledAttributes.getResourceId(
            R.styleable.ARIndicatorView_indicator_shape,
            R.drawable.bg_indicator
        )
        isScrubbingEnabled =
            styledAttributes.getBoolean(R.styleable.ARIndicatorView_indicator_scrubbing, false)
        isShouldAnimateOnScrubbing = styledAttributes.getBoolean(
            R.styleable.ARIndicatorView_animate_indicator_scrubbing,
            false
        )
        styledAttributes.recycle()
        if (isInEditMode) {
            for (i in 0 until numberOfIndicators) {
                drawCircle(i)
            }
        }
    }

    protected fun drawCircle(position: Int) {
        val imageView = ImageView(context)
        imageView.background = ContextCompat.getDrawable(context, R.drawable.bg_indicator)
        val layoutParams = LayoutParams(
            LayoutParams.WRAP_CONTENT,
            LayoutParams.WRAP_CONTENT
        )
        if (indicatorShape == 0) {
            imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.bg_indicator))
        } else {
            imageView.setImageDrawable(ContextCompat.getDrawable(context, indicatorShape))
        }
        setupColors(imageView, position)
        layoutParams.setMargins(paddingSize, 10, 0, 10)
        layoutParams.width = indicatorSizeWidth
        layoutParams.height = indicatorSizeHeight
        addView(imageView, layoutParams)
        indicators.add(imageView)
    }

    private fun setupColors(imageView: ImageView, position: Int) {
        if (position == 0) {
            setActiveColorTo(imageView)
        } else {
            setUnActiveColorTo(imageView)
        }
    }

    protected fun setActiveColorTo(imageView: ImageView) {
        imageView.setColorFilter(selectionColor)
        imageView.requestLayout()
    }

    protected fun setUnActiveColorTo(imageView: ImageView) {
        imageView.setColorFilter(indicatorColor)
        imageView.invalidate()
    }
}