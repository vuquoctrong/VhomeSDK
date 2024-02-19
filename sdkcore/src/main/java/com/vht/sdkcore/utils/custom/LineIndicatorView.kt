package com.vht.sdkcore.utils.custom

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2

class LineIndicatorView : IndicatorView {
    private var recyclerView: RecyclerView? = null
    private var viewPager: ViewPager2? = null
    private var selectedPosition = 0
    private var isScrubbing = false

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    )

    fun attachTo(recyclerView: RecyclerView?, shouldPage: Boolean) {
        this.recyclerView = recyclerView
        addIndicators(recyclerView)
        if (shouldPage) {
            PagerSnapHelper().attachToRecyclerView(recyclerView)
        }
        this.recyclerView!!.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (recyclerView.scrollState == RecyclerView.SCROLL_STATE_SETTLING) {
                    if (recyclerView.layoutManager is LinearLayoutManager) {
                        val position: Int
                        position = if (dx > 0) {
                            (recyclerView.layoutManager as LinearLayoutManager?)!!.findLastVisibleItemPosition()
                        } else {
                            (recyclerView.layoutManager as LinearLayoutManager?)!!.findFirstVisibleItemPosition()
                        }
                        if (position <= indicators.size - 1) {
                            if (selectedPosition != position) {
                                selectIndicatorAt(position)
                                if (indicatorAnimation !== 0) {
                                    animateIndicator(position)
                                }
                            }
                        }
                    }
                }
            }
        })
    }

    /**
     * Attach ARIndicator to ViewPager to know which is the current position in ViewPager and which indicator to be selected
     *
     * @param viewPager ViewPager to be attached to
     */
    fun attachTo(viewPager: ViewPager2) {
        this.viewPager = viewPager
        this.addIndicators(viewPager)
        this.viewPager!!.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                selectIndicatorAt(position)
                if (indicatorAnimation != 0) {
                    animateIndicator(position)
                }
            }
        })
    }

    private fun addIndicators(recyclerView: RecyclerView?) {
        if (recyclerView != null) {
            val adapter = recyclerView.adapter
            if (adapter != null) {
                for (i in 0 until adapter.itemCount) {
                    drawCircle(i)
                }
            } else {
                throw NullPointerException("RecyclerView Adapter not found or null --> ARIndicatorView")
            }
        } else {
            throw NullPointerException("RecyclerView is null --> ARIndicatorView")
        }
    }

    private fun addIndicators(viewPager: ViewPager2?) {
        if (viewPager != null) {
            val pagerAdapter = viewPager.adapter
            if (pagerAdapter != null) {
                for (i in 0 until pagerAdapter.itemCount) {
                    drawCircle(i)
                }
            } else {
                throw NullPointerException("ViewPager Adapter is null --> ARIndicatorView")
            }
        } else {
            throw NullPointerException("ViewPager is null --> ARIndicatorView")
        }
    }

    private fun unSelectIndicators() {
        for (i in 0 until indicators.size) {
            this.setUnActiveColorTo(indicators[i])
        }
    }

    private fun invalidateIndicators() {
        removeIndicators()
        if (recyclerView != null) {
            addIndicators(recyclerView)
        } else if (viewPager != null) {
            addIndicators(viewPager)
        }
        selectIndicatorAt(selectedPosition)
    }

    private fun selectIndicatorAt(position: Int) {
        selectedPosition = position
        unSelectIndicators()
        this.setActiveColorTo(this.indicators[selectedPosition])
    }
    /**
     * Current indicator selection color
     *
     * @return Integer
     */
    /**
     * Sets the selection color of the indicators
     *
     * @param selectionColor Integer
     */
    var selectionColor: Int = 0
        get() {
            return field
        }
        set(selectionColor) {
            field = selectionColor
            invalidateIndicators()
        }
    /**
     * Current indicator size
     *
     * @return Integer
     */
    /**
     * Change the indicators size.
     *
     * @param indicatorSize Integer
     */
    var indicatorSize: Int = 0
        set(indicatorSize) {
            field = indicatorSize
            invalidateIndicators()
        }
    /**
     * Current indicator animation
     *
     * @return Integer
     */
    /**
     * The animation to play when an indicator is selected.
     *
     * @param indicatorAnimation AnimationId
     */
    var indicatorAnimation: Int = 0
    /**
     * Current indicator shape
     *
     * @return Integer
     */
    /**
     * Sets the indicators shape.
     * <br></br>
     * You need to pass the drawable id from drawable res.
     *
     * @param indicatorShape DrawableId
     */
    var indicatorShape: Int = 0
        set(indicatorShape) {
            field = indicatorShape
            invalidateIndicators()
        }
    /**
     * Current indicator color
     *
     * @return Integer
     */
    /**
     * This is used for setting the color on the indicators when they are not selected
     *
     * @param indicatorColor The color to be set to indicators
     */
    var indicatorColor: Int = 0
        set(indicatorColor) {
            field = indicatorColor
            invalidateIndicators()
        }

    /**
     * Selects the indicator at the given position.
     *
     * @param position Position to select
     */
    fun setSelectedPosition(position: Int) {
        selectedPosition = position
        unSelectIndicators()
        this.setActiveColorTo(this.indicators.get(selectedPosition))
        scrollToPosition(selectedPosition)
    }

    /**
     * Current position of indicator
     *
     * @return Integer
     */
    fun getSelectedPosition(): Int {
        return selectedPosition
    }
    /**
     * Returns numberOfIndicators if you set it via the method setNumberOfDots else 0
     *
     * @return Integer
     */
    /**
     * This is used for adding indicators if you don't want to be attached to RecyclerView or ViewPager
     *
     * @param numberOfIndicators Integer number of indicators to be added
     */
    private var numberOfIndicators: Int = 0
        set(numberOfIndicators) {
            field = numberOfIndicators
            if (!this.indicators.isEmpty()) {
                removeIndicators()
            }
            for (i in 0 until this.numberOfIndicators) {
                drawCircle(i)
            }
        }

    /**
     * Removes all indicators
     */
    fun removeIndicators() {
        for (imageView in indicators) {
            removeView(imageView)
        }
        indicators.clear()
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_DOWN) {
            return this.isScrubbingEnabled
        }
        selectIndicatorWhenScrubbing(ev)
        return this.isScrubbingEnabled
    }

    private fun selectIndicatorWhenScrubbing(ev: MotionEvent) {
        val x = Math.round(ev.x)
        val y = Math.round(ev.y)
        for (i in 0 until getChildCount()) {
            val child = getChildAt(i) as ImageView
            if (x > child.left && x < child.right && y > child.top && y < child.bottom) {
                isScrubbing = true
                selectIndicatorAt(i)
                scrollToPosition(i)
            }
        }
    }

    private fun scrollToPosition(position: Int) {
        if (recyclerView != null) {
            selectedPosition = position
            recyclerView!!.smoothScrollToPosition(position)
        } else if (viewPager != null) {
            selectedPosition = position
            viewPager!!.setCurrentItem(position, true)
        }
    }

    private fun animateIndicator(position: Int) {
        if (this.isScrubbingEnabled && isScrubbing) {
            if (this.isShouldAnimateOnScrubbing) {
                indicators[position]
                    .startAnimation(AnimationUtils.loadAnimation(getContext(), indicatorAnimation))
            }
        } else {
            indicators[position]
                .startAnimation(AnimationUtils.loadAnimation(getContext(), indicatorAnimation))
        }
    }
}